package com.mdix.fhir.facade.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.mdmi.core.Mdmi;
import org.mdmi.core.engine.terminology.FHIRTerminologyTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
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
public class HSDSHealthcareServiceResourceProvider implements IResourceProvider {

	@Autowired
	FHIRTerminologySettings terminologySettings;

	@Autowired
	ServletContext context;

	static Boolean loaded = Boolean.FALSE;

	@Value("#{systemProperties['mdmi.maps'] ?: '/maps'}")
	private String mapsFolder;

	private HashMap<String, Properties> mapProperties = new HashMap<>();

	private void loadMaps() throws IOException {
		synchronized (this) {
			if (loaded) {
				return;
			}

			FHIRTerminologyTransform.codeValues.clear();

			FHIRTerminologyTransform.processTerminology = true;

			FHIRTerminologyTransform.setFHIRTerminologyURL(terminologySettings.getUrl());

			FHIRTerminologyTransform.setUserName(terminologySettings.getUserName());

			FHIRTerminologyTransform.setPassword(terminologySettings.getPassword());

			Set<String> maps = Stream.of(new File(mapsFolder).listFiles()).filter(
				file -> (!file.isDirectory() && file.toString().endsWith("mdmi"))).map(File::getName).collect(
					Collectors.toSet());
			for (String map : maps) {
				InputStream targetStream = new FileInputStream(mapsFolder + "/" + map);
				Mdmi.INSTANCE().getResolver().resolve(targetStream);
			}
			loaded = Boolean.TRUE;
		}
	}

	private void reloadMaps() throws IOException {
		synchronized (this) {
			loaded = false;
			mapProperties.clear();
			loadMaps();
		}
	}

	private Properties getMapProperties(String target) {
		if (!mapProperties.containsKey(target)) {
			Properties properties = new Properties();
			Path propertyFile = Paths.get(context.getRealPath(mapsFolder + "/" + target + ".properties"));
			if (Files.exists(propertyFile)) {
				try {
					properties.load(Files.newInputStream(propertyFile));
				} catch (IOException e) {
				}
			}
			Path valuesFile = Paths.get(context.getRealPath(mapsFolder + "/" + target + ".json"));
			if (Files.exists(valuesFile)) {
				try {
					properties.put("InitialValues", new String(Files.readAllBytes(valuesFile)));
				} catch (IOException e) {
				}
			}
			mapProperties.put(target, properties);
		}
		return mapProperties.get(target);
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
			@OptionalParam(name = HealthcareService.SP_CHARACTERISTIC) TokenParam characteristic,
			@OptionalParam(name = HealthcareService.SP_COVERAGE_AREA) String coverageArea) throws Exception {

		logger.debug("DEBUG Start transformation ");
		// loadMaps();
		// MdmiUow.setSerializeSemanticModel(false);

		// String result = RuntimeService.runTransformation(
		// "source", "hsdsdata".getBytes(), "target", null, getMapProperties("source"), getMapProperties("target"));

		List<HealthcareService> retVal = new ArrayList<>();

		HealthcareService hcs = new HealthcareService();
		hcs.setId("hcs");

		hcs.addCategory().setText("aaaa");

		retVal.add(hcs);
		return retVal;
	}

}
