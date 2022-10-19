package com.mdix.fhir.facade.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
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
public class HSDSLocationResourceProvider extends MDMIProvider implements IResourceProvider {

	HsdsClient hsdsClient;

	private void addTableField(JSONObject json, JSONObject query) {
		if (!json.has("$table.field")) {
			JSONArray array = new JSONArray();
			json.put("$table.field", array);
		}
		JSONArray array = (JSONArray) json.get("$table.field");
		array.put(query);

	}

	/**
	 * @param context
	 * @param terminologySettings
	 */
	public HSDSLocationResourceProvider(ServletContext context, MDMISettings terminologySettings,
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
	public Class<Location> getResourceType() {
		return Location.class;
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
	public Location getResourceById(@IdParam IdType theId) throws IOException, Exception {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		String healthcareServiceResult = runTransformation(
			"HSDSJSON.LocationComplete",
			this.hsdsClient.executeGet(theId.getValue().replace("HealthcareService", "location")),
			"FHIRR4JSON.MasterBundle");
		Bundle locationBundle = parser.parseResource(Bundle.class, healthcareServiceResult);
		List<Location> locations = BundleUtil.toListOfResourcesOfType(ctx, locationBundle, Location.class);
		return locations.get(0);
	}

	/*
	 * {"$table.field": [{"regular_schedule.site_hours": "Mon-Fri 8am-4pm"}]}
	 */
	@Search()
	public List<Location> searchLocation(@OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) String postalCode,
			@OptionalParam(name = Location.SP_NAME) String name,
			@OptionalParam(name = "hoursofoperation") String hoursofoperation,
			@OptionalParam(name = Location.SP_ADDRESS_STATE) String state,
			@OptionalParam(name = "language") String languagek) throws Exception {

		JSONObject json = new JSONObject();

		if (!StringUtils.isEmpty(name)) {
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

		if (!StringUtils.isEmpty(hoursofoperation)) {

			JSONObject hoursOfOperationQuery = new JSONObject();
			hoursOfOperationQuery.put("regular_schedule.site_hours", hoursofoperation);
			addTableField(json, hoursOfOperationQuery);
		}

		String hsds = this.hsdsClient.executeQuery("locations", json);
		String result = runTransformation("HSDSJSON.LocationComplete", hsds, "FHIRR4JSON.MasterBundle");
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Bundle bundle = parser.parseResource(Bundle.class, result);

		List<Location> retVal = new ArrayList<>();
		retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, Location.class));

		return retVal;
	}

}
