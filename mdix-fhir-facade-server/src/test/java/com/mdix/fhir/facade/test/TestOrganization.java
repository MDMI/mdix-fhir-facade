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

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.mdix.fhir.facade.Application;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TestOrganization {

	@Autowired
	private TestRestTemplate template;

	@Test
	void testGetOrganization() throws Exception {

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
			"testGetOrganization", parser.setPrettyPrint(true).encodeResourceToString(
				client.read().resource(Organization.class).withId("969").execute()));
		// validate(ctx, bundle);

	}

	@Test
	void testSearchOrganization() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> organizations = new ArrayList<>();

		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(Organization.class).returnBundle(Bundle.class).execute();
		serializeResult("testSearchOrganization", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchOrganization", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchOrganizationByName() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> organizations = new ArrayList<>();

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put("name", new ArrayList<String>());
		query.get("name").add("CHILDREN");
		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchOrganizationByName", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchOrganizationByName", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchOrganizationByState() throws DataFormatException, IOException {
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
		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchOrganizationByState", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchOrganizationByState", bundle.getEntry().isEmpty());

	}

	@Test
	void testSearchOrganizationByZip() throws DataFormatException, IOException {
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
		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchOrganizationByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testSearchOrganizationByZip", bundle.getEntry().isEmpty());

	}

	/*
	 *
	 * Test 01 (ORG_TC01)
	 * Query - Search for FHIR Organizations with matching postal code and part of the name 
	 *
	 */
	@Test
	void testORG_TC01() throws DataFormatException, IOException {
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
		query.put("name", new ArrayList<String>());
		query.get("name").add("SERVICE");
		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testORG_TC01", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testORG_TC01", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 02 (ORG_TC02)
	 * Query - Search for FHIR Organizations with matching postal code and supporting program 
	 *
	 */
	@Test
	void testORG_TC02() throws DataFormatException, IOException {
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
		query.put("program", new ArrayList<String>());
		query.get("program").add("CHILDREN'S ADVOCACY CENTER");

		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testORG_TC02", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testORG_TC02", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 03 (ORG_TC03)
	 * Query - Search for FHIR Organizations with matching state code and language spoken 
	 */

	@Test
	void testORG_TC03() throws DataFormatException, IOException {
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
		query.put("language", new ArrayList<String>());
		query.get("language").add("English");

		Bundle bundle = client.search().forResource(Organization.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testORG_TC03", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testORG_TC03", bundle.getEntry().isEmpty());

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

}
