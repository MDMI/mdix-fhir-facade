package com.mdix.fhir.facade.test;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Location;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.mdix.fhir.facade.Application;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TestHealthCareServices {

	@Autowired
	private TestRestTemplate template;

	@Test
	void testSearcHealthCareServices() throws Exception {

		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		ctx.getRestfulClientFactory().setConnectTimeout(1000000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		ctx.getRestfulClientFactory().setConnectionRequestTimeout(1000000);

		ctx.getRestfulClientFactory().setSocketTimeout(100000000);

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		new Include("*");
		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(HealthcareService.class).returnBundle(Bundle.class).execute();

		serializeResult("testSearcHealthCareServices", parser.setPrettyPrint(true).encodeResourceToString(bundle));

		assertFalse("testSearcHealthCareServices", bundle.getEntry().isEmpty());

	}

	@Test
	void testGetHealthCareServices() throws Exception {

		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		ctx.getRestfulClientFactory().setConnectTimeout(1000000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		ctx.getRestfulClientFactory().setConnectionRequestTimeout(1000000);

		ctx.getRestfulClientFactory().setSocketTimeout(100000000);

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		serializeResult(
			"testGetHealthCareServices", parser.setPrettyPrint(true).encodeResourceToString(
				client.read().resource(HealthcareService.class).withId("4").execute()));
		// validate(ctx, bundle);

	}

	@Test
	void testSearcHealthCareServicesWithInclude() throws Exception {

		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		ctx.getRestfulClientFactory().setConnectTimeout(1000000);
		ctx.getRestfulClientFactory().setConnectionRequestTimeout(1000000);
		ctx.getRestfulClientFactory().setSocketTimeout(100000000);

		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		new Include("*");

		// Map<String, List<String>> includes = new HashMap<String, List<String>>();
		// includes.put("_include", null);
		//
		client.search().byUrl(serverBase);

		Bundle bundle = client.search().byUrl("HealthcareService?_include=*").returnBundle(Bundle.class).execute();

		// Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(includes).returnBundle(
		// Bundle.class).execute();

		serializeResult(
			"testSearcHealthCareServicesWithInclude", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearcHealthCareServicesWithInclude", bundle.getEntry().isEmpty());

	}

	void serializeResult(String test, String result) throws IOException {
		// Path sourcePath = Paths.get(test);
		String testName = test;

		Path testPath = Paths.get("target/test-output/" + testName);
		if (!Files.exists(testPath)) {
			Files.createDirectories(testPath);
		}

		Path path = Paths.get("target/test-output/" + testName + "/" + testName + ".json");
		byte[] strToBytes = result.getBytes();

		Files.write(path, strToBytes);
	}

	@Test
	void testSearchHealthCareServicesByName() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(HealthcareService.SP_NAME, new ArrayList<String>());
		query.get(HealthcareService.SP_NAME).add("COUNSELING");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByName", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByName", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchHealthCareServicesByState() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_STATE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_STATE).add("MI");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByState", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByState", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchHealthCareServicesByZip() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_POSTALCODE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_POSTALCODE).add("49085");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByZip", bundle.getEntry().isEmpty());

	}

	/*
	 * Service Type
	 * {"$table.field": [{"service_taxonomy.taxonomy_detail": "Child Abuse Prevention"}]}
	 * Service Category
	 * {"$table.field": [{"service_taxonomy.category": "Child Care"}]}
	 * Language
	 * {"language": "English, Spanish"}
	 */

	@Test
	void testSearchHealthCareServicesByLanguage() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put("communication", new ArrayList<String>());
		query.get("communication").add("English");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByLanguage", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByLanguage", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchHealthCareServicesByType() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(HealthcareService.SP_SERVICE_TYPE, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_TYPE).add("Child Abuse Prevention");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByType", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByType", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchHealthCareServicesByCategory() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(HealthcareService.SP_SERVICE_CATEGORY, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_CATEGORY).add("Child Care");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByCategory", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByCategory", bundle.getEntry().isEmpty());

	}

	/*
	 * Accesibility
	 * {"$table.field": [{"location.accessibility_for_disabilities": "Accessible Parking"}]}
	 */
	@Test
	void testSearchHealthCareServicesByAccessibility() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put("accessibility", new ArrayList<String>());
		query.get("accessibility").add("Accessible Parking");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByAccessibility", parser.setPrettyPrint(true).encodeResourceToString(bundle));

		assertFalse("testSearchHealthCareServicesByAccessibility", bundle.getEntry().isEmpty());

	}

	/*
	 * * Test 01 (HCS_TC01)
	 * Query - Search for FHIR HealthCareServices with matching zipcode and service type 
	 *
	 *
	 */
	@Test
	void testHCS_TC01() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_POSTALCODE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_POSTALCODE).add("49085");
		query.put(HealthcareService.SP_SERVICE_TYPE, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_TYPE).add("Child Abuse Prevention");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testHCS_TC01", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 02 (HCS_TC02)
	 * Query - Search for FHIR HealthCareServices with matching zipode and service category 
	 *
	 */
	@Test
	void testHCS_TC02() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_POSTALCODE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_POSTALCODE).add("49085");
		query.put(HealthcareService.SP_SERVICE_CATEGORY, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_CATEGORY).add("Child/Adolescent");

		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testHCS_TC02", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 03 (HCS_TC03)
	 * Query - Search for FHIR HealthCareServices with matching state code, language and service type
	 */

	@Test
	void testHCS_TC03() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_STATE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_STATE).add("MI");
		// query.put("communication", new ArrayList<String>());
		// query.get("communication").add("English");
		query.put(HealthcareService.SP_SERVICE_TYPE, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_TYPE).add("Mental Health Related Support Groups");

		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testHCS_TC03", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchHealthCareServicesByState", bundle.getEntry().isEmpty());

	}

	/*
	 *
	 * Test 04 (HCS_TC04)
	 * Query - Search for FHIR HealthCareServices with matching state code, service type and available time
	 */
	@Disabled("Disabled until available time query is available!")
	@Test
	void testHCS_TC04() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_STATE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_STATE).add("MI");
		query.put(HealthcareService.SP_SERVICE_TYPE, new ArrayList<String>());
		query.get(HealthcareService.SP_SERVICE_TYPE).add("Child Abuse Prevention");

		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByState", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testHCS_TC04", bundle.getEntry().isEmpty());

	}

	/*
	 *
	 * Test 01 (HCS_TC01)
	 * Query - Search for FHIR HealthCareServices with matching zipcode and service type 
	 * Test 02 (HCS_TC02)
	 * Query - Search for FHIR HealthCareServices with matching zipode and service category 
	 * Test 03 (HCS_TC03)
	 * Query - Search for FHIR HealthCareServices with matching state code, language and service type
	 * Test 04 (HCS_TC04)
	 * Query - Search for FHIR HealthCareServices with matching state code, service type and available time
	 * Test 05 (HCS_TC05)
	 * Query - Search for FHIR HealthCareServices with matching postal code, service category and accessibility
	 */

	/*
	 *
	 * Test 05 (HCS_TC05)
	 * Query - Search for FHIR HealthCareServices with matching postal code, service category and accessibility
	 *
	 */
	@Test
	void testHCS_TC05() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_ADDRESS_POSTALCODE, new ArrayList<String>());
		query.get(Location.SP_ADDRESS_POSTALCODE).add("49085");
		Bundle bundle = client.search().forResource(HealthcareService.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult(
			"testSearchHealthCareServicesByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testHCS_TC05", bundle.getEntry().isEmpty());

	}

}
