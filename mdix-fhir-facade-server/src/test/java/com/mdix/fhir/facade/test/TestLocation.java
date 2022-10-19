package com.mdix.fhir.facade.test;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
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
import ca.uhn.fhir.util.BundleUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TestLocation {

	@Autowired
	private TestRestTemplate template;

	String testStr = null;

	@Test
	void testGetLocation() throws Exception {

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
				client.read().resource(Location.class).withId("972").execute()));
		// validate(ctx, bundle);

	}

	@Test
	void testSearchLocation() {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> locations = new ArrayList<>();

		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(Location.class).returnBundle(Bundle.class).execute();
		locations.addAll(BundleUtil.toListOfResources(ctx, bundle));
		IParser parser = ctx.newJsonParser();
		for (IBaseResource resoure : locations) {
			System.out.println(parser.setPrettyPrint(true).encodeResourceToString(resoure));
		}

	}

	@Test
	void testSearchLocationByName() throws DataFormatException, IOException {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list

		Map<String, List<String>> query = new HashMap<String, List<String>>();
		query.put(Location.SP_NAME, new ArrayList<String>());
		query.get(Location.SP_NAME).add("CHILDREN");
		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchLocationByName", parser.setPrettyPrint(true).encodeResourceToString(bundle));

	}

	@Test
	void testSearchLocationByState() throws DataFormatException, IOException {
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
		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchLocationByState", parser.setPrettyPrint(true).encodeResourceToString(bundle));

	}

	@Test
	void testSearchLocationByZip() throws DataFormatException, IOException {
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
		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testSearchLocationByZip", parser.setPrettyPrint(true).encodeResourceToString(bundle));

	}

	/*
	 * Test 01 (LOC_TC01)
	 * Query - Search for FHIR Locations with matching zipcode and part of the name 
	 */
	@Test
	void testLOC_TC01() throws DataFormatException, IOException {
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
		query.get("name").add("CHILDREN");
		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testLOC_TC01", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testLOC_TC01", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 02 (LOC_TC02)
	 * Query - Search for FHIR Locations with matching zipcode and day of operating hours. 
	 *
	 */
	@Test
	void testLOC_TC02() throws DataFormatException, IOException {
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
		query.put("hoursofoperation", new ArrayList<String>());
		query.get("hoursofoperation").add("By appointment (sp)");

		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testLOC_TC02", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testLOC_TC02", bundle.getEntry().isEmpty());

	}

	/*
	 * Test 03 (LOC_TC03)
	 * Query - Search for FHIR Locations with matching state code and language spoken 
	 */

	@Test
	void testLOC_TC03() throws DataFormatException, IOException {
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

		Bundle bundle = client.search().forResource(Location.class).whereMap(query).returnBundle(
			Bundle.class).execute();
		serializeResult("testLOC_TC03", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		assertFalse("testLOC_TC03", bundle.getEntry().isEmpty());

	}

	// {"$table.field": [{"physical_address.state": "MI"}]}
	// {"$table.field": [{"physical_address.postal_code": "49085"}]}

	@Test
	void testToken() throws InterruptedException {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 2);
		testStr = "TEST";
		int i = 0;
		do {
			System.out.println("print" + i + " :: " + testStr);
			Thread.sleep(1000);
			i++;
		} while (!callMethod(calendar));
		System.out.println("print" + i + " :: " + testStr);
	}

	boolean callMethod(Calendar cal) {
		Calendar now = Calendar.getInstance();
		if (now.after(cal)) {
			testStr = null;
			return true;
		} else
			return false;
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
