package com.mdix.fhir.facade.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
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

	private Bundle removeResourcesByName(Bundle bundle, String name) {
		ArrayList<BundleEntryComponent> removelist = new ArrayList<>();
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

			if (bundleEntry.getResource().isEmpty()) {
				removelist.add(bundleEntry);
			}

			if (bundleEntry.getResource().fhirType().equals("HealthcareService")) {
				HealthcareService hs = (HealthcareService) bundleEntry.getResource();
				if (!hs.getName().contains(name)) {
					removelist.add(bundleEntry);
				}
			}
		}
		bundle.getEntry().removeAll(removelist);
		return bundle;
	}

	private Bundle removeResourcesByLanguage(Bundle bundle, String language) {
		ArrayList<BundleEntryComponent> removelist = new ArrayList<>();
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

			if (bundleEntry.getResource().isEmpty()) {
				removelist.add(bundleEntry);
			}

			if (bundleEntry.getResource().fhirType().equals("HealthcareService")) {
				HealthcareService hs = (HealthcareService) bundleEntry.getResource();
				Boolean check = false;
				if (!hs.hasCommunication()) {
					removelist.add(bundleEntry);
				} else {
					for (CodeableConcept cc : hs.getCommunication()) {
						for (Coding c : cc.getCoding()) {
							if (c.getCode().contains(language)) {
								check = true;
							}
						}
					}
					if (!check) {
						removelist.add(bundleEntry);
					}
				}
			}
		}
		bundle.getEntry().removeAll(removelist);
		return bundle;
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
			@OptionalParam(name = "availableTime") String availableTime_daysOfWeek,
			@OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) String postalCode,
			@OptionalParam(name = Location.SP_ADDRESS_STATE) String state,
			@OptionalParam(name = "language") String language,
			@OptionalParam(name = "accessibility") String accessibility,
			@OptionalParam(name = HealthcareService.SP_SERVICE_CATEGORY) String category,
			@OptionalParam(name = "_include") String _include) throws Exception {

		JSONObject json = new JSONObject();
		Boolean nameFlag = false;
		Boolean languageFlag = false;
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

		if (!StringUtils.isEmpty(language)) {
			json.put("language", language);
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

		if (json.has("$table.field")) {
			if (json.has("name")) {
				json.remove("name");
				nameFlag = true;
			}
			if (json.has("language")) {
				json.remove("language");
				languageFlag = true;
			}
		}

		String hsds = this.hsdsClient.executeQuery("services", json);
		String result = runTransformation("HSDSJSON.ServicesComplete", hsds, "FHIRR4JSON.MasterBundle");
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Bundle bundle = parser.parseResource(Bundle.class, result);

		if (nameFlag) {
			bundle = removeResourcesByName(bundle, name);
		}

		if (languageFlag) {
			bundle = removeResourcesByLanguage(bundle, language);
		}

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
					if ((_include.equals("*") || _include.equals("organization")) &&
							reference.startsWith("Organization")) {
						String organizationResult = runTransformation(
							"HSDSJSON.OrganizationComplete", this.hsdsClient.executeGet(reference),
							"FHIRR4JSON.MasterBundle");
						Bundle organizationBundle = parser.parseResource(Bundle.class, organizationResult);
						retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, organizationBundle, Organization.class));
					}
					if ((_include.equals("*") || _include.equals("location")) && reference.contains("Location")) {
						String locationResult = runTransformation(
							"HSDSJSON.LocationComplete", this.hsdsClient.executeGet(reference),
							"FHIRR4JSON.MasterBundle");
						Bundle LocationBundle = parser.parseResource(Bundle.class, locationResult);
						retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, LocationBundle, Location.class));
					}

				}
			} catch (Throwable exception) {
				logger.error("Provider", exception);
			}

		}

		return retVal;
	}

}
