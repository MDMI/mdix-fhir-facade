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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
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

	Calendar calendar;

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

		int seconds = (int) responseMap.get("expires_in");
		calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, seconds);

		return accessToken;

	}

	public String executeQuery(String resource, JSONObject json) throws IOException {
		Calendar now = Calendar.getInstance();
		if (accessToken == null || now.after(calendar)) {
			accessToken = this.getAccessToken();
		}

		String queryParemeter = null;
		if (!json.isEmpty()) {

			// Gson gson = new GsonBuilder().create();
			// String jsonString = gson.toJson(json);
			// System.out.println("JSON " + jsonString);
			queryParemeter = "?query=" + URLEncoder.encode(json.toString(), StandardCharsets.UTF_8.toString());
			// queryParemeter = "?query=%7B%22name%22%20%3A%20%7B%22%24like%22%20%3A%20%22CHILDREN'S%22%7D%7D";

			System.out.println(URLDecoder.decode(queryParemeter));

		}

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(
			hsdsSettings.getDirectoryUrl() + resource + "/complete" + (queryParemeter != null
					? queryParemeter
					: "")).method("GET", null).addHeader("accept", "application/json").addHeader(
						"x-api-key", hsdsSettings.getxApiKey()).addHeader(
							"Authorization", "Bearer " + accessToken).build();

		Response response = client.newCall(request).execute();
		String hsds = response.body().string();
		System.err.println(hsds);
		return hsds;

	}

	public String executeGet(String reference) throws IOException {
		Calendar now = Calendar.getInstance();
		if (accessToken == null || now.after(calendar)) {
			accessToken = this.getAccessToken();
		}
		String resource[] = reference.split("/");

		OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(500, TimeUnit.SECONDS).build();
		Request request = new Request.Builder().url(
			hsdsSettings.getDirectoryUrl() + resource[0].toLowerCase() + "s/complete/" + resource[1]).method(
				"GET", null).addHeader("accept", "application/json").addHeader(
					"x-api-key", hsdsSettings.getxApiKey()).addHeader("Authorization", "Bearer " + accessToken).build();

		Response response = client.newCall(request).execute();
		String hsds = response.body().string();
		return hsds;

	}

}
