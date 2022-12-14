package com.mdix.fhir.facade.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class HSDSOrganizationResourceProvider extends MDMIProvider implements IResourceProvider {

	HsdsClient hsdsClient;

	/**
	 * @param context
	 * @param terminologySettings
	 */
	public HSDSOrganizationResourceProvider(ServletContext context, MDMISettings terminologySettings,
			HsdsClient hsdsClient2) {
		super(context, terminologySettings);
		hsdsClient = hsdsClient2;
	}

	@Autowired
	MDMISettings terminologySettings;

	@Autowired
	ServletContext context;

	static Boolean loaded = Boolean.FALSE;

	private HashMap<String, Properties> mapProperties = new HashMap<>();

	private static Logger logger = LoggerFactory.getLogger(HSDSHealthcareServiceResourceProvider.class);

	/**
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<Organization> getResourceType() {
		return Organization.class;
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
	public Organization getResourceById(@IdParam IdType theId) throws IOException, Exception {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		String organizationeResult = runTransformation(
			"HSDSJSON.OrganizationComplete",
			this.hsdsClient.executeGet(theId.getValue().replace("HealthcareService", "organization")),
			"FHIRR4JSON.MasterBundle");
		Bundle organizationBundle = parser.parseResource(Bundle.class, organizationeResult);
		List<Organization> organizations = BundleUtil.toListOfResourcesOfType(
			ctx, organizationBundle, Organization.class);
		return organizations.get(0);
	}

	private void addTableField(JSONObject json, JSONObject query) {
		if (!json.has("$table.field:like")) {
			JSONArray array = new JSONArray();
			json.put("$table.field:like", array);
		}
		JSONArray array = (JSONArray) json.get("$table.field:like");
		array.put(query);

	}

	private Bundle removeResources(Bundle bundle, String name) {
		ArrayList<BundleEntryComponent> removelist = new ArrayList<>();
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {

			if (bundleEntry.getResource().isEmpty()) {
				removelist.add(bundleEntry);
			}

			if (bundleEntry.getResource().fhirType().equals("Organization")) {
				Organization organization = (Organization) bundleEntry.getResource();
				if (!StringUtils.containsIgnoreCase(organization.getName(), name)) {
					removelist.add(bundleEntry);
				}
			}
		}
		bundle.getEntry().removeAll(removelist);
		return bundle;
	}
	/*
	 * Organization
	 * {"$table.field": [{"physical_address.postal_code": "49085"}]} - Results Returned, be sure that the table.field has quotes around it.
	 * {"$table.field": [{"physical_address.state_province": "MI"}]}- Results Returned
	 *
	 */

	/*
	 * "detail":
	 * "Key '0' is not a field for table 'organization'. Valid fields: 'id,name,alternate_name,organization_type,source_id,source,description,email,url,tax_status,tax_id,year_incorporated,legal_status,contact,region'"
	 * ,
	 */
	@Search()
	public List<Organization> searchOrganization(@OptionalParam(name = Organization.SP_NAME) String name,
			@OptionalParam(name = Organization.SP_ADDRESS_POSTALCODE) String postalCode,
			@OptionalParam(name = Organization.SP_ADDRESS_STATE) String state,
			@OptionalParam(name = "program") String program, @OptionalParam(name = "language") String languagek)
			throws Exception {

		JSONObject json = new JSONObject();
		Boolean flag = false;
		if (!StringUtils.isEmpty(name)) {
			json = new JSONObject();
			JSONObject nameQuery = new JSONObject();
			nameQuery.put("$like", name);
			json.put("name", nameQuery);
		}

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

		if (!StringUtils.isEmpty(program)) {

			JSONObject categoryTypeQuery = new JSONObject();
			categoryTypeQuery.put("program.name", program);
			addTableField(json, categoryTypeQuery);
		}

		if (!StringUtils.isEmpty(languagek)) {
			JSONObject communicationQuery = new JSONObject();
			communicationQuery.put("service_view.language", languagek);
			addTableField(json, communicationQuery);
		}

		if (json.has("$table.field:like") && json.has("name")) {
			json.remove("name");
			flag = true;
		}

		String hsds = this.hsdsClient.executeQuery("organizations", json);
		String result = runTransformation("HSDSJSON.OrganizationComplete", hsds, "FHIRR4JSON.MasterBundle");
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Bundle bundle = parser.parseResource(Bundle.class, result);
		if (flag) {
			bundle = removeResources(bundle, name);
		}
		// List<BundleEntryComponent> entries = bundle.getEntry();
		List<Organization> retVal = new ArrayList<>();
		retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, Organization.class));
		return retVal;
	}

}
