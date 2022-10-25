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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.mdmi.MessageModel;
import org.mdmi.core.MdmiMessage;
import org.mdmi.core.engine.preprocessors.IPreProcessor;
import org.springframework.core.io.ClassPathResource;

/**
 * @author seanmuir
 *
 */
public class JSON2XML implements IPreProcessor {

	ServletContext context;

	/**
	 * @param context
	 */
	public JSON2XML(ServletContext context) {
		super();
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#canProcess(org.mdmi.MessageModel)
	 */
	@Override
	public boolean canProcess(MessageModel messageModel) {
		if ("PERATON".equals(messageModel.getGroup().getName()) ||
				"HSDSJSON".equals(messageModel.getGroup().getName())) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.core.engine.preprocessors.IPreProcessor#getName()
	 */
	@Override
	public String getName() {
		return "JSON2XML";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mdmi.cor.engine.preprocessors.IPreProcessor#processMessage(org.mdmi.MessageModel, org.mdmi.core.MdmiMessage)
	 */
	@Override
	public void processMessage(MessageModel arg0, MdmiMessage mdmiMessage) {
		JSONObject json = new JSONObject(
			"{\"row\":" + mdmiMessage.getDataAsString().replace("null", "\"\"").replace("N/A", "NA") + "}");
		// String tagmessage = walkFields(json);
		// System.err.println("<root>" + XML.toString(json) + "</root>");
		mdmiMessage.setData("<root>" + XML.toString(json) + "</root>");

	}

	Properties readClarification() {

		ClassPathResource clarifications = new ClassPathResource("clarification.properties");

		Properties properties = new Properties();
		if (clarifications != null && clarifications.exists()) {
			try {
				properties.load(clarifications.getInputStream());
			} catch (IOException e) {

			}
		}
		return properties;
	}

	String walkFields(JSONObject json) {
		Properties clarifications = readClarification();
		org.json.JSONArray fields = (JSONArray) json.get("fields");
		for (int fctr = 0; fctr < fields.length(); fctr++) {
			org.json.JSONObject field = (JSONObject) fields.get(fctr);

			// org.json.JSONArray tags = (JSONArray) field.get("tags");
			if (clarifications.getProperty(field.getString("name")) != null) {
				String[] parts = clarifications.getProperty(field.getString("name")).split(",");
				for (String part : parts) {
					if (field.has("tags")) {
						org.json.JSONArray tags = (JSONArray) field.get("tags");
						tags.put(part);
					} else {
						field.append("tags", parts);
					}
				}
			}
			walkChildren(field, clarifications);
		}
		return json.toString();

	}

	void walkChildren(JSONObject parent, Properties clarifications) {
		org.json.JSONArray children = (JSONArray) parent.get("children");
		for (int fctr = 0; fctr < children.length(); fctr++) {
			org.json.JSONObject child = (JSONObject) children.get(fctr);
			// org.json.JSONArray ctags = (JSONArray) child.get("tags");
			if (clarifications.getProperty(child.getString("name")) != null) {
				String[] parts = clarifications.getProperty(child.getString("name")).split(",");
				for (String part : parts) {
					// ctags.put(part);
					if (child.has("tags")) {
						org.json.JSONArray ctags = (JSONArray) child.get("tags");
						ctags.put(part);
					} else {
						child.append("tags", parts);
					}
				}
			}
			walkChildren(child, clarifications);
		}

	}

}
