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
package com.mdix.fhir.facade.servlet;

import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author seanmuir
 *
 */
@Configuration
@ConfigurationProperties("hsdssettings")
public class HsdsSettings {

	private String clientId;

	private String clientSecret;

	private String tokenUrl;

	private String xrfToken;

	private String scope;

	private String directoryUrl;

	private String xApiKey;

	/**
	 * @return the xApiKey
	 */
	public String getxApiKey() {
		return xApiKey;
	}

	/**
	 * @param xApiKey the xApiKey to set
	 */
	public void setxApiKey(String xApiKey) {
		this.xApiKey = xApiKey;
	}

	/**
	 * @return the directoryUrl
	 */
	public String getDirectoryUrl() {
		return directoryUrl;
	}

	/**
	 * @param directoryUrl the directoryUrl to set
	 */
	public void setDirectoryUrl(String directoryUrl) {
		this.directoryUrl = directoryUrl;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the xrfToken
	 */
	public String getXrfToken() {
		return xrfToken;
	}

	/**
	 * @param xrfToken the xrfToken to set
	 */
	public void setXrfToken(String xrfToken) {
		this.xrfToken = xrfToken;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * @param clientSecret the clientSecret to set
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * @return the tokenUrl
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}

	/**
	 * @param tokenUrl the tokenUrl to set
	 */
	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	/**
	 * @return
	 */
	public String getAuthentication() {
		String authentication = clientId + ":" + clientSecret;
		return Base64.getEncoder().encodeToString(authentication.getBytes());
	}

}
