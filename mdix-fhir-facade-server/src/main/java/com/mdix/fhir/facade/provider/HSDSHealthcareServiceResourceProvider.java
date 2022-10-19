package com.mdix.fhir.facade.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdix.fhir.facade.hsds.HsdsClient;
import com.mdix.fhir.facade.servlet.MDMISettings;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.util.BundleUtil;

/**
 * This is a simple resource provider which only implements "read/GET" methods, but
 * which uses a custom subclassed resource definition to add statically bound
 * extensions.
 *
 * See the MyOrganization definition to see how the custom resource
 * definition works.
 */

public class HSDSHealthcareServiceResourceProvider extends MDMIProvider implements IResourceProvider {

	HsdsClient hsdsClient;

	/**
	 * @param context2
	 * @param terminologySettings2
	 * @param hsdsClient2
	 */
	public HSDSHealthcareServiceResourceProvider(ServletContext context2, MDMISettings terminologySettings2,
			HsdsClient hsdsClient2) {
		super(context2, terminologySettings2);
		hsdsClient = hsdsClient2;
	}

	private static Logger logger = LoggerFactory.getLogger(HSDSHealthcareServiceResourceProvider.class);

	/**
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<HealthcareService> getResourceType() {
		return HealthcareService.class;
	}

	/**
	 * The "@Read" annotation indicates that this method supports the read operation. It takes one argument, the Resource type being returned.
	 *
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 * @throws Exception
	 * @throws IOException
	 */
	@Read()
	public HealthcareService getResourceById(@IdParam IdType theId) throws IOException, Exception {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		String healthcareServiceResult = runTransformation(
			"HSDSJSON.ServicesComplete",
			this.hsdsClient.executeGet(theId.getValue().replace("HealthcareService", "service")),
			"FHIRR4JSON.MasterBundle");
		Bundle healthcareServiceBundle = parser.parseResource(Bundle.class, healthcareServiceResult);
		List<HealthcareService> healthcareServices = BundleUtil.toListOfResourcesOfType(
			ctx, healthcareServiceBundle, HealthcareService.class);
		return healthcareServices.get(0);
	}

	private void addTableField(JSONObject json, JSONObject query) {
		if (!json.has("$table.field")) {
			JSONArray array = new JSONArray();
			json.put("$table.field", array);
		}
		JSONArray array = (JSONArray) json.get("$table.field");
		array.put(query);

	}

	@Search()
	public List<DomainResource> searchHealthcareService(@OptionalParam(name = HealthcareService.SP_NAME) String name,
			@OptionalParam(name = HealthcareService.SP_PROGRAM) String program,
			@OptionalParam(name = HealthcareService.SP_SERVICE_TYPE) String service_type,
			@OptionalParam(name = "availableTime.daysOfWeek") String availableTime_daysOfWeek,
			@OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) String postalCode,
			@OptionalParam(name = Location.SP_ADDRESS_STATE) String state,
			@OptionalParam(name = "communication") String communication,
			@OptionalParam(name = "accessibility") String accessibility,
			@OptionalParam(name = HealthcareService.SP_SERVICE_CATEGORY) String category,
			@OptionalParam(name = "_include") String _include) throws Exception {

		JSONObject json = new JSONObject();
		if (!StringUtils.isEmpty(state)) {
			JSONObject stateQuery = new JSONObject();
			stateQuery.put("physical_address.state_province", state);
			addTableField(json, stateQuery);
		}

		if (!StringUtils.isEmpty(postalCode)) {
			JSONObject postalCodeQuery = new JSONObject();
			postalCodeQuery.put("physical_address.postal_code", postalCode);
			addTableField(json, postalCodeQuery);
		}

		if (!StringUtils.isEmpty(service_type)) {

			JSONObject serviceTypeQuery = new JSONObject();
			serviceTypeQuery.put("service_taxonomy.taxonomy_detail", service_type);
			addTableField(json, serviceTypeQuery);
		}

		if (!StringUtils.isEmpty(category)) {

			JSONObject categoryTypeQuery = new JSONObject();
			categoryTypeQuery.put("service_taxonomy.category", category);
			addTableField(json, categoryTypeQuery);
		}

		if (!StringUtils.isEmpty(name)) {
			JSONObject nameQuery = new JSONObject();
			nameQuery.put("$like", name);
			json.put("name", nameQuery);
		}

		if (!StringUtils.isEmpty(communication)) {
			json.put("language", communication);
		}

		if (!StringUtils.isEmpty(accessibility)) {
			JSONObject accessibilityTypeQuery = new JSONObject();
			accessibilityTypeQuery.put("location.accessibility_for_disabilities", accessibility);
			addTableField(json, accessibilityTypeQuery);
		}

		if (!StringUtils.isEmpty(availableTime_daysOfWeek)) {
			JSONObject availableTimeDaysOfWeekQuery = new JSONObject();
			availableTimeDaysOfWeekQuery.put("regular_schedule.service_hours", availableTime_daysOfWeek);
			addTableField(json, availableTimeDaysOfWeekQuery);
		}

		String hsds = this.hsdsClient.executeQuery("services", json);
		String result = runTransformation("HSDSJSON.ServicesComplete", hsds, "FHIRR4JSON.MasterBundle");
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Bundle bundle = parser.parseResource(Bundle.class, result);
		List<DomainResource> retVal = new ArrayList<>();
		List<HealthcareService> services = BundleUtil.toListOfResourcesOfType(ctx, bundle, HealthcareService.class);
		HashSet<String> references = new HashSet<>();

		retVal.addAll(services);
		if (_include != null) {
			try {
				for (HealthcareService service : services) {
					for (Reference location : service.getLocation()) {
						references.add(location.getReference());
					}
					if (service.hasProvidedBy()) {
						Reference organization = service.getProvidedBy();
						references.add(organization.getReference());
					}
				}

				for (String reference : references) {
					if (_include.equals("*") && reference.startsWith("Organization")) {
						String organizationResult = runTransformation(
							"HSDSJSON.OrganizationComplete", this.hsdsClient.executeGet(reference),
							"FHIRR4JSON.MasterBundle");
						Bundle organizationBundle = parser.parseResource(Bundle.class, organizationResult);
						retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, organizationBundle, Organization.class));
					}
					if (_include.equals("*") || reference.contains("Location")) {
						String locationResult = runTransformation(
							"HSDSJSON.LocationComplete", this.hsdsClient.executeGet(reference),
							"FHIRR4JSON.MasterBundle");
						Bundle organizationBundle = parser.parseResource(Bundle.class, locationResult);
						retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, organizationBundle, Location.class));
					}

				}
			} catch (Throwable exception) {
				System.err.println(exception.getMessage());
			}

		}

		return retVal;
	}

}
