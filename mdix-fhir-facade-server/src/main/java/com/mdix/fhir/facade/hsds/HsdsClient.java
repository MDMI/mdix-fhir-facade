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
package com.mdix.fhir.facade.hsds;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdix.fhir.facade.servlet.HsdsSettings;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author seanmuir
 *
 */
@Component
public class HsdsClient {

	@Autowired
	HsdsSettings hsdsSettings;

	String accessToken = null;

	private String getAccessToken() throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(
			mediaType, "grant_type=client_credentials&client_id=" + hsdsSettings + "&scope=" + hsdsSettings.getScope());
		Request request = new Request.Builder().url(hsdsSettings.getTokenUrl()).method("POST", body).addHeader(
			"Authorization", "Basic " + hsdsSettings.getAuthentication()).addHeader(
				"Content-Type", "application/x-www-form-urlencoded").addHeader(
					"Cookie", "XSRF-TOKEN=" + hsdsSettings.getXrfToken()).build();
		Response response = client.newCall(request).execute();

		HashMap responseMap = new ObjectMapper().readValue(response.body().byteStream(), HashMap.class);
		String accessToken = (String) responseMap.get("access_token");
		return accessToken;

	}

	public String executeQuery(String resource) throws IOException {
		if (accessToken == null) {
			accessToken = this.getAccessToken();
		}

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(hsdsSettings.getDirectoryUrl() + resource).method(
			"GET", null).addHeader("accept", "application/json").addHeader(
				"x-api-key", hsdsSettings.getxApiKey()).addHeader("Authorization", "Bearer " + accessToken).build();

		Response response = client.newCall(request).execute();
		String hsds = response.body().string();
		return hsds;

	}

}
