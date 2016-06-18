package com.wirelust.cfmock.web.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.wirelust.cfmock.web.exceptions.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Date: 17-Jun-2016
 *
 * @author T. Curran
 */
@Named
@ApplicationScoped
public class Configuration implements Serializable {

	public static final String ENV_FILE_NAME = "app.cfmock.env";

	private static final long serialVersionUID = -3221266624481566406L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

	Properties configuredProperties = new Properties();

	boolean loaded = false;

	@PostConstruct
	public void init() {
		String configFileName = System.getProperty(ENV_FILE_NAME);
		LOGGER.info("{}={}", ENV_FILE_NAME, configFileName);

		if (configFileName == null) {
			LOGGER.error("{} was not specified. using defaults only");
			return;
		}

		File propertyFile = new File(configFileName);

		loadPropertyFile(propertyFile);

		LOGGER.info("env properties loaded:{}", configuredProperties.toString());
	}

	public String getSetting(final String key) {

		if (this.configuredProperties.containsKey(key)) {
			return this.configuredProperties.getProperty(key);
		}

		return null;
	}

	public String getSetting(final String key, final String defaultValue) {

		final String returnValue = this.getSetting(key);
		if (returnValue == null || returnValue.isEmpty()) {
			return defaultValue;
		}
		return returnValue;
	}

	public int getSettingInt(final String key) {

		final String resultString = this.getSetting(key);
		int resultInt = 0;
		try {
			resultInt = Integer.parseInt(resultString);
		} catch (final NumberFormatException e) {
			// do nothing
			LOGGER.warn("Attempt to cast setting as int failed. key:'{}'", key);
		}
		return resultInt;
	}

	public int getSettingInt(final String key, final int defaultValue) {

		final String resultString = this.getSetting(key);
		int resultInt = 0;
		try {
			resultInt = Integer.parseInt(resultString);
		} catch (final NumberFormatException e) {
			resultInt = defaultValue;
		}
		return resultInt;
	}

	public boolean getSettingBool(final String key) {
		return getSettingBool(key, false);
	}

	public boolean getSettingBool(final String key, final boolean defaultValue) {

		String resultString = getSetting(key);
		if (resultString == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(resultString);
	}

	public boolean isLoaded() {
		return loaded;
	}

	private void loadPropertyFile(File propertyFile) {
		try (InputStream inputStream = new FileInputStream(propertyFile)) {
			configuredProperties.load(inputStream);
			loaded = true;
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

}
