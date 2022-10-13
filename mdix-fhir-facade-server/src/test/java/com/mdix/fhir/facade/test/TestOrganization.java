package com.mdix.fhir.facade.test;

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
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;

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
			"testGetHealthCareServices", parser.setPrettyPrint(true).encodeResourceToString(
				client.read().resource(Organization.class).withId("969").execute()));
		// validate(ctx, bundle);

	}

	@Test
	void testSearchOrganization() {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> organizations = new ArrayList<>();

		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(Organization.class).returnBundle(Bundle.class).execute();
		organizations.addAll(BundleUtil.toListOfResources(ctx, bundle));
		IParser parser = ctx.newJsonParser();
		for (IBaseResource resoure : organizations) {
			System.out.println(parser.setPrettyPrint(true).encodeResourceToString(resoure));
		}

	}

	@Test
	void testSearchOrganizationByName() {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
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
		organizations.addAll(BundleUtil.toListOfResources(ctx, bundle));
		IParser parser = ctx.newJsonParser();
		for (IBaseResource resoure : organizations) {
			System.out.println(parser.setPrettyPrint(true).encodeResourceToString(resoure));
		}

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
