package com.wirelust.cfmock.web.producers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import com.wirelust.cfmock.web.qualifiers.ClasspathResource;
import com.wirelust.cfmock.web.util.QualifierUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 20-Jun-2016
 *
 * @author T. Curran
 */
@ApplicationScoped
public class ResourceBundleProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleProducer.class);

	@Produces
	@ClasspathResource("")
	Properties loadPropertiesBundle(InjectionPoint injectionPoint) {
		String name = QualifierUtil.getClasspathResourceValue(injectionPoint);

		Properties properties = new Properties();

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream inputStream = classLoader.getResourceAsStream(name)) {
			if (inputStream == null) {
				LOGGER.error("error loading properties file:" + name);
			} else {
				properties.load(inputStream);
			}
		} catch (IOException e) {
			LOGGER.error("error loading properties file:" + name, e);
		}
		return properties;
	}

}
