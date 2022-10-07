package com.mdix.fhir.facade.test;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HealthcareService;
import org.junit.BeforeClass;
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
import ca.uhn.fhir.util.BundleUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class TestHealthCareServices {

	@BeforeClass
	public static void setEnvironment() {
		System.setProperty("mdmimaps", "src/test/resources/testmaps");
	}

	@Autowired
	private TestRestTemplate template;

	@Test
	void testSearchHealthCareServices() {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> healthcareServices = new ArrayList<>();

		new Include("*");
		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(HealthcareService.class).returnBundle(Bundle.class).execute();
		healthcareServices.addAll(BundleUtil.toListOfResources(ctx, bundle));
		// Create a FHIR context
		// FhirContext ctx = FhirContext.forR4();

		// Instantiate a new parser
		IParser parser = ctx.newJsonParser();
		for (IBaseResource resoure : healthcareServices) {
			System.out.println(parser.setPrettyPrint(true).encodeResourceToString(resoure));
		}

	}

}
