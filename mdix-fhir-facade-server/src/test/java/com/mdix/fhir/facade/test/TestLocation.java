package com.mdix.fhir.facade.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
	void testSearchLocation() {
		// Create a context and a client
		FhirContext ctx = FhirContext.forR4();
		ctx.getRestfulClientFactory().setSocketTimeout(200 * 1000);
		// There might be a better way to get the current url
		String serverBase = template.getRootUri() + "/fhir/";

		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		// We'll populate this list
		List<IBaseResource> organizations = new ArrayList<>();

		// We'll do a search for all Patients and extract the first page
		Bundle bundle = client.search().forResource(Location.class).returnBundle(Bundle.class).execute();
		organizations.addAll(BundleUtil.toListOfResources(ctx, bundle));
		IParser parser = ctx.newJsonParser();
		for (IBaseResource resoure : organizations) {
			System.out.println(parser.setPrettyPrint(true).encodeResourceToString(resoure));
		}

	}

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

}
