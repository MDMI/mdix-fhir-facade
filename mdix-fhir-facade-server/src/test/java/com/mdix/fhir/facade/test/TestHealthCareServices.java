package com.mdix.fhir.facade.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HealthcareService;
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
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TestHealthCareServices {

	@Autowired
	private TestRestTemplate template;

	@Test
	void testSearchHealthCareServices() throws Exception {

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

		serializeResult("testSearchHealthCareServices", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		// validate(ctx, bundle);

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
	void testSearchHealthCareServicesWithInclude() throws Exception {

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
			"testSearchHealthCareServicesWithInclude", parser.setPrettyPrint(true).encodeResourceToString(bundle));
		// validate(ctx, bundle);

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
