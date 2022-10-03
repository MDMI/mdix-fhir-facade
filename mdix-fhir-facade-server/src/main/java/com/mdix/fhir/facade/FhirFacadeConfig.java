package com.mdix.fhir.facade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mdix.fhir.facade.servlet.FhirFacade;

import ca.uhn.fhir.rest.server.RestfulServer;

@Configuration
public class FhirFacadeConfig {

	@Bean
	public RestfulServer restfulServer() {
		RestfulServer fhirServer = new FhirFacade();
		return fhirServer;
	}

}
