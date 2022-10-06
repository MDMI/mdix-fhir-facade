package com.mdix.fhir.facade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

//import ca.uhn.fhir.jpa.starter.annotations.OnEitherVersion;
import ca.uhn.fhir.rest.server.RestfulServer;

//@ServletComponentScan(basePackageClasses = {FhirFacade.class})

@ServletComponentScan(basePackageClasses = { RestfulServer.class })
@SpringBootApplication()
@ComponentScan({ "com.mdix.fhir.facade", "com.mdix.fhir.facade.provider" })
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);

		// Server is now accessible at eg. http://localhost:8080/fhir/metadata
		// UI is now accessible at http://localhost:8080/
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	@Bean
	public ServletRegistrationBean hapiServletRegistration(RestfulServer restfulServer) {
		ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
		// beanFactory.autowireBean(restfulServer);
		servletRegistrationBean.setServlet(restfulServer);
		servletRegistrationBean.addUrlMappings("/fhir/*");
		servletRegistrationBean.setLoadOnStartup(1);

		return servletRegistrationBean;
	}

	// @Autowired
	// AutowireCapableBeanFactory beanFactory;
	//
	// @Bean
	// @Conditional(OnEitherVersion.class)
	// public ServletRegistrationBean hapiServletRegistration(RestfulServer restfulServer) {
	// ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
	// beanFactory.autowireBean(restfulServer);
	// servletRegistrationBean.setServlet(restfulServer);
	// servletRegistrationBean.addUrlMappings("/fhir/*");
	// servletRegistrationBean.setLoadOnStartup(1);
	//
	// return servletRegistrationBean;
	// }
	//
	// @Bean
	// public ServletRegistrationBean overlayRegistrationBean() {
	//
	// AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext();
	// annotationConfigWebApplicationContext.register(FhirTesterConfig.class);
	//
	// DispatcherServlet dispatcherServlet = new DispatcherServlet(
	// annotationConfigWebApplicationContext);
	// dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
	// dispatcherServlet.setContextConfigLocation(FhirTesterConfig.class.getName());
	//
	// ServletRegistrationBean registrationBean = new ServletRegistrationBean();
	// registrationBean.setServlet(dispatcherServlet);
	// registrationBean.addUrlMappings("/*");
	// registrationBean.setLoadOnStartup(1);
	// return registrationBean;
	//
	// }
}
