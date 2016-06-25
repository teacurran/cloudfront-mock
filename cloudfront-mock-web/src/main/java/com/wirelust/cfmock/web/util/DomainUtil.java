package com.wirelust.cfmock.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 25-Jun-2016
 *
 * @author T. Curran
 */
public class DomainUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(DomainUtil.class);

	private static final String DOMAIN_REGEX = "[^.]*\\.[^.]{2,3}(?:\\.[^.]{2,3})?$";
	private static final Pattern DOMAIN_PATTERN = Pattern.compile(DOMAIN_REGEX);

	private DomainUtil() {
		// static class can't be instantiated
	}

	public static String getTopLevelDomain(final String fullDomain) {
		Matcher matcher = DOMAIN_PATTERN.matcher(fullDomain);
		if (matcher.find()) {
			LOGGER.debug("found top level domain input:{} result:{}", fullDomain, matcher.group());
			return matcher.group();
		}
		return fullDomain;
	}

}
