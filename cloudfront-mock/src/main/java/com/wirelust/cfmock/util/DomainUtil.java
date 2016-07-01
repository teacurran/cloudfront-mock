package com.wirelust.cfmock.util;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 30-Jun-2016
 *
 * @author T. Curran
 */
public class DomainUtil {

	public static final String PROTOCOL_HTTP = "http";
	public static final String PROTOCOL_HTTPS = "https";

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

	public static String getPortForUrl(URL url) {
		if (url.getPort() == -1) {
			return "";
		}
		String port = "";
		if (url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTP) && url.getPort() != 80
				|| url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTPS) && url.getPort() != 443) {
			port = ":" + url.getPort();
		}
		return port;
	}
}
