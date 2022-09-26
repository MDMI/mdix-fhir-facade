package com.mdix.fhir.facade.provider;

import org.hl7.fhir.r4.model.Location;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/**
 * This is a simple resource provider which only implements "read/GET" methods, but
 * which uses a custom subclassed resource definition to add statically bound
 * extensions.
 * 
 * See the MyOrganization definition to see how the custom resource 
 * definition works.
 */
public class HSDSLocationResourceProvider implements IResourceProvider {

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
	 */
	@Read()
	public Location getResourceById(@IdParam IdDt theId) {
		
		/*
		 * We only support one organization, so the follwing
		 * exception causes an HTTP 404 response if the 
		 * ID of "1" isn't used.
		 */
		if (!"1".equals(theId.getValue())) {
			throw new ResourceNotFoundException(theId);
		}
		
		Location retVal = new Location();
	 
		
		return retVal;
	}


}
