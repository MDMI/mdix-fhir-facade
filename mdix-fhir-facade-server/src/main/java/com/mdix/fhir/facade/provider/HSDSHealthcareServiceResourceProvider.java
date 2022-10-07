package com.mdix.fhir.facade.provider;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdix.fhir.facade.hsds.HsdsClient;
import com.mdix.fhir.facade.servlet.FHIRTerminologySettings;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.util.BundleUtil;

/**
 * This is a simple resource provider which only implements "read/GET" methods, but
 * which uses a custom subclassed resource definition to add statically bound
 * extensions.
 *
 * See the MyOrganization definition to see how the custom resource
 * definition works.
 */

public class HSDSHealthcareServiceResourceProvider extends MDMIProvider implements IResourceProvider {

	HsdsClient hsdsClient;

	/**
	 * @param context2
	 * @param terminologySettings2
	 * @param hsdsClient2
	 */
	public HSDSHealthcareServiceResourceProvider(ServletContext context2, FHIRTerminologySettings terminologySettings2,
			HsdsClient hsdsClient2) {
		super(context2, terminologySettings2);
		hsdsClient = hsdsClient2;
	}

	private static Logger logger = LoggerFactory.getLogger(HSDSHealthcareServiceResourceProvider.class);

	/**
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<HealthcareService> getResourceType() {
		return HealthcareService.class;
	}

	/**
	 * The "@Read" annotation indicates that this method supports the read operation. It takes one argument, the Resource type being returned.
	 *
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public HealthcareService getResourceById(@IdParam IdType theId) {

		/*
		 * We only support one organization, so the follwing
		 * exception causes an HTTP 404 response if the
		 * ID of "1" isn't used.
		 */
		if (!"1".equals(theId.getValue())) {
			throw new ResourceNotFoundException(theId);
		}
		HealthcareService retVal = new HealthcareService();

		return retVal;
	}

	@Search()
	public List<HealthcareService> searchHealthcareService(

			@OptionalParam(name = HealthcareService.SP_ACTIVE) TokenParam active,
			@OptionalParam(name = HealthcareService.SP_PROGRAM) String program,
			@OptionalParam(name = HealthcareService.SP_SERVICE_TYPE) String service_type,
			@OptionalParam(name = "availableTime.daysOfWeek") String availableTime_daysOfWeek,
			@OptionalParam(name = "coverageArea.zip") String coverageArea_zip,
			@OptionalParam(name = "communication") String communication,

			@OptionalParam(name = HealthcareService.SP_CHARACTERISTIC) TokenParam characteristic,
			@OptionalParam(name = HealthcareService.SP_SERVICE_CATEGORY) String coverageArea,
			@OptionalParam(name = "_include") String _include) throws Exception {
		String hsds = this.hsdsClient.executeQuery("services");
		String result = runTransformation("HSDSJSON.ServicesComplete", hsds, "FHIRR4JSON.MasterBundle");
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		Bundle bundle = parser.parseResource(Bundle.class, result);
		List<BundleEntryComponent> entries = bundle.getEntry();
		List<HealthcareService> retVal = new ArrayList<>();
		retVal.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, HealthcareService.class));
		return retVal;
	}

}
