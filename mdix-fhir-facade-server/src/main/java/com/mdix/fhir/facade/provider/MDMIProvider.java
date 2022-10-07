/*******************************************************************************
 * Copyright (c) 2022 seanmuir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     seanmuir - initial API and implementation
 *
 *******************************************************************************/
package com.mdix.fhir.facade.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.mdmi.core.Mdmi;
import org.mdmi.core.engine.MdmiUow;
import org.mdmi.core.engine.terminology.FHIRTerminologyTransform;
import org.mdmi.core.runtime.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mdix.fhir.facade.servlet.FHIRTerminologySettings;

/**
 * @author seanmuir
 *
 */
public class MDMIProvider {

	private static Logger logger = LoggerFactory.getLogger(MDMIProvider.class);

	ServletContext context;

	FHIRTerminologySettings terminologySettings;

	static Boolean loaded = Boolean.FALSE;

	private String mapsFolder = "/Users/seanmuir/git/mdix-fhir-facade/mdix-fhir-facade-server/src/main/maps";

	private HashMap<String, Properties> mapProperties = new HashMap<>();

	/**
	 *
	 */
	public MDMIProvider(ServletContext context, FHIRTerminologySettings terminologySettings) {
		super();
		this.context = context;
		this.terminologySettings = terminologySettings;
	}

	protected void loadMaps() throws IOException {
		synchronized (this) {
			if (loaded) {
				return;
			}

			FHIRTerminologyTransform.codeValues.clear();

			FHIRTerminologyTransform.processTerminology = false;

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

	/**
	 * @param string
	 * @param bytes
	 * @param string2
	 * @param object
	 * @param sourceProperties
	 * @param targetProperties
	 * @return
	 * @throws Exception
	 */
	protected String runTransformation(String source, String hsds, String target) throws Exception {

		logger.debug("DEBUG Start transformation ");
		loadMaps();
		MdmiUow.setSerializeSemanticModel(false);

		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(new FHIRR4PostProcessorJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new JSON2XML(context));
		Properties sourceProperties = new Properties();
		Properties targetProperties = new Properties();

		String result = RuntimeService.runTransformation(
			source, hsds.getBytes(), target, null, sourceProperties, targetProperties);

		return result;

	}

}
