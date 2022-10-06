package com.mdix.fhir.facade.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.mdmi.core.Mdmi;
import org.mdmi.core.engine.MdmiUow;
import org.mdmi.core.engine.terminology.FHIRTerminologyTransform;
import org.mdmi.core.runtime.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdix.fhir.facade.FHIRTerminologySettings;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This is a simple resource provider which only implements "read/GET" methods, but
 * which uses a custom subclassed resource definition to add statically bound
 * extensions.
 *
 * See the MyOrganization definition to see how the custom resource
 * definition works.
 */

@Component
@PropertySource("classpath:hsds.properties")
public class HSDSHealthcareServiceResourceProvider implements IResourceProvider {

	@Value("${clientId}")
	private String clientId;

	@Value("${clientSecret}")
	private String clientSecret;

	@Value("${tokenUrl}")
	private String tokenUrl;

	@Autowired
	FHIRTerminologySettings terminologySettings;

	ServletContext context;

	static Boolean loaded = Boolean.FALSE;

	// @Value("#{systemProperties['mdmi.maps'] ?: '/maps'}")
	private String mapsFolder = "/Users/seanmuir/git/mdix-fhir-facade/mdix-fhir-facade-server/src/main/maps";

	private HashMap<String, Properties> mapProperties = new HashMap<>();

	/**
	 * @param context2
	 */
	public HSDSHealthcareServiceResourceProvider(ServletContext context2) {
		context = context2;
	}

	private void loadMaps() throws IOException {
		synchronized (this) {
			if (loaded) {
				return;
			}

			FHIRTerminologyTransform.codeValues.clear();

			FHIRTerminologyTransform.processTerminology = false;

			// FHIRTerminologyTransform.setFHIRTerminologyURL(terminologySettings.getUrl());

			// FHIRTerminologyTransform.setUserName(terminologySettings.getUserName());

			// FHIRTerminologyTransform.setPassword(terminologySettings.getPassword());

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
			@OptionalParam(name = HealthcareService.SP_PROGRAM) String program,
			@OptionalParam(name = HealthcareService.SP_SERVICE_TYPE) String service_type,
			@OptionalParam(name = "availableTime.daysOfWeek") String availableTime_daysOfWeek,
			@OptionalParam(name = "coverageArea.zip") String coverageArea_zip,
			@OptionalParam(name = "communication") String communication,

			@OptionalParam(name = HealthcareService.SP_CHARACTERISTIC) TokenParam characteristic,
			@OptionalParam(name = HealthcareService.SP_SERVICE_CATEGORY) String coverageArea,
			@OptionalParam(name = "_include") String _include) throws Exception {

		logger.debug("DEBUG Start transformation ");

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(
			mediaType,
			"grant_type=client_credentials&client_id=2p53cbe7e9rl3jvaidfpc4jrbq&scope=mi-cie.directory/read");
		Request request = new Request.Builder().url("https://login-uat.mi-cie.org/oauth2/token").method(
			"POST", body).addHeader(
				"Authorization",
				"Basic MnA1M2NiZTdlOXJsM2p2YWlkZnBjNGpyYnE6Ym9oM3BwZWdoZTNkcjdpcTdybDFrdTJndDRzYTFwdGxtNG5nazlrNnZtazgybzcxaTdq").addHeader(
					"Content-Type", "application/x-www-form-urlencoded").addHeader(
						"Cookie", "XSRF-TOKEN=8bcb84ca-46ba-4a53-8602-a309dad42b5a").build();
		Response response = client.newCall(request).execute();

		HashMap responseMap = new ObjectMapper().readValue(response.body().byteStream(), HashMap.class);
		// Read the value of the "access_token" key from the hashmap
		String accessToken = (String) responseMap.get("access_token");
		// System.out.println(responseMap.toString());
		// Return the access_token value
		System.out.println("accessToken " + accessToken);

		request = new Request.Builder().url("https://directory-uat.mi-cie.org/v1/services").method(
			"GET", null).addHeader("accept", "application/json").addHeader(
				"x-api-key", "RkhJUkZBQ0FERToxODI2NDg5Mjc=").addHeader(
					"Authorization", "Bearer " + accessToken).build();

		response = client.newCall(request).execute();
		String hsds = response.body().string();

		logger.debug("DEBUG Start transformation ");
		loadMaps();
		MdmiUow.setSerializeSemanticModel(false);

		// Set Stylesheet for CDA document section generation
		// CDAPostProcessor.setStylesheet("perspectasections.xsl");

		// add in fhir post processor
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new Deliminated2XML("NJ", "\\|"));
		Mdmi.INSTANCE().getPostProcessors().addPostProcessor(new FHIRR4PostProcessorJson());
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new HL7V2MessagePreProcessor());
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new PreProcessorForFHIRJson());
		Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new JSON2XML(context));
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new SBHA2XML("SBHA", "\\|"));
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new SBHA2XML("HSDS", "\\,"));
		// Mdmi.INSTANCE().getSourceSemanticModelProcessors().addSourceSemanticProcessor(new ProcessRelationships());
		// Mdmi.INSTANCE().getPreProcessors().addPreProcessor(new CDAPreProcesor());
		Properties sourceProperties = null;
		Properties targetProperties = null;

		System.err.println(hsds);

		String result = RuntimeService.runTransformation(
			"HSDSJSON.ServicesComplete", hsds.getBytes(), "FHIRR4JSON.MasterBundle", null, sourceProperties,
			targetProperties);

		System.err.println(result);

		// Create a FHIR context
		FhirContext ctx = FhirContext.forR4();

		// Instantiate a new parser
		IParser parser = ctx.newJsonParser();

		Bundle bundle = parser.parseResource(Bundle.class, result);

		List<BundleEntryComponent> entries = bundle.getEntry();

		List<HealthcareService> retVal = new ArrayList<>();
		for (BundleEntryComponent entry : entries) {
			retVal.add((HealthcareService) entry.getResource());

		}

		// Parse it
		// Patient parsed = parser.parseResource(Patient.class, input);
		// System.out.println(parsed.getName().get(0).getFamily());

		// HealthcareService hcs = new HealthcareService();
		// hcs.setId("hcs");
		//
		// hcs.addCategory().setText("aaaa");
		//
		// retVal.add(hcs);
		return retVal;
	}

	// @Autowired
	// OAuth2RestTemplate oauth2RestTemplate;

	public String getResource() {
		String apiUrl = "https://directory-uat.mi-cie.org/v1/services";
		// the simplicity lies in this line of code
		// where a developer make a call like a regular RestTemplate
		// and under the hood all the token fetching is handled
		return oauth2RestTemplate().getForEntity(apiUrl, String.class).getBody();
	}

	protected OAuth2ProtectedResourceDetails oauth2Resource() {

		/*
		 * curl --location --request POST "https://login-uat.mi-cie.org/oauth2/token" --header
		 * "Authorization: Basic MnA1M2NiZTdlOXJsM2p2YWlkZnBjNGpyYnE6Ym9oM3BwZWdoZTNkcjdpcTdybDFrdTJndDRzYTFwdGxtNG5nazlrNnZtazgybzcxaTdq" --header
		 * "Content-Type: application/x-www-form-urlencoded" --data-urlencode "grant_type=client_credentials" --data-urlencode
		 * "client_id=2p53cbe7e9rl3jvaidfpc4jrbq" --data-urlencode "scope=mi-cie.directory/read"
		 */

		// tokenUrl=
		// clientId=2p53cbe7e9rl3jvaidfpc4jrbq
		// clientSecret=boh3ppeghe3dr7iq7rl1ku2gt4sa1ptlm4ngk9k6vmk82o71i7j
		ClientCredentialsResourceDetails clientCredentialsResourceDetails = new ClientCredentialsResourceDetails();
		clientCredentialsResourceDetails.setAccessTokenUri("https://login-uat.mi-cie.org/oauth2/token");
		clientCredentialsResourceDetails.setClientId("2p53cbe7e9rl3jvaidfpc4jrbq");
		clientCredentialsResourceDetails.setClientSecret("2p53cbe7e9rl3jvaidfpc4jrbq");
		clientCredentialsResourceDetails.setGrantType("client_credentials"); // this depends on your specific OAuth2 server
		clientCredentialsResourceDetails.setScope(Collections.singletonList("mi-cie.directory/read"));
		// clientCredentialsResourceDetails.setTokenName("8bcb84ca-46ba-4a53-8602-a309dad42b5a");

		// clientCredentialsResourceDetails.set

		// .addHeader("Cookie", "XSRF-TOKEN=8bcb84ca-46ba-4a53-8602-a309dad42b5a")

		clientCredentialsResourceDetails.setAuthenticationScheme(AuthenticationScheme.header); // this again depends on the OAuth2 server
		// specifications

		// clientCredentialsResourceDetails.set
		return clientCredentialsResourceDetails;
	}

	public OAuth2RestTemplate oauth2RestTemplate() {
		AccessTokenRequest atr = new DefaultAccessTokenRequest();
		atr.setCookie("XSRF-TOKEN=8bcb84ca-46ba-4a53-8602-a309dad42b5a");

		OAuth2RestTemplate oauth2RestTemplate = new OAuth2RestTemplate(
			oauth2Resource(), new DefaultOAuth2ClientContext(atr));
		oauth2RestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

		return oauth2RestTemplate;
	}

}
