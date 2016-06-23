package com.wirelust.cfmock;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.wirelust.cfmock.exceptions.CFMockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignatureValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureValidator.class);

	public static final String PARAM_EXPIRES = "Expires";
	public static final String PARAM_KEY_PAIR_ID = "Key-Pair-Id";

    public static final String COOKIE_EXPIRES = "CloudFront-Expires";
    public static final String COOKIE_SIGNATURE = "CloudFront-Signature";
    public static final String COOKIE_POLICY = "CloudFront-Policy";
    public static final String COOKIE_KEY_PAIR_ID = "CloudFront-Key-Pair-Id";

	public static final String PROTOCOL_HTTP = "http";
	public static final String PROTOCOL_HTTPS = "https";

	private SignatureValidator() {
		// static only class
	}

	/**
	 * validate a signed URL
	 * @param keyFile the key used to sign the url
	 * @param resource the full signed URL
	 * @return true if signature is valid and not expired
	 */
	public static boolean validateSignedUrl(File keyFile, String resource) {
		URL url;

		try {
			url = new URL(resource);
		} catch (MalformedURLException e) {
			throw new CFMockException(e);
		}

		Map<String, String> queryParams = splitQuery(url.getQuery());

		Date expires = new Date(Long.parseLong(queryParams.get(PARAM_EXPIRES))*1000);

		Date now = new Date();
		if (now.getTime() > expires.getTime()) {
			throw new CFMockException("Signature is expired");
		}

		String port = getPort(url);

		String urlToCheck = url.getProtocol() + "://" + url.getHost() + port + url.getPath();

		LOGGER.debug("checking URL:{}", urlToCheck);
		String keyPairId = queryParams.get(PARAM_KEY_PAIR_ID);

		String signedUrl;
		try {
			signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
						null, null, keyFile,
						urlToCheck, keyPairId, expires);
		} catch (InvalidKeySpecException | IOException e) {
			throw new CFMockException(e);
		}

		return resource.equals(signedUrl);
	}

	public static Map<String, String> splitQuery(final String query) {
		return splitQuery(query, Constants.DEFAULT_ENCODING);
	}

	public static Map<String, String> splitQuery(final String query, final String encoding) {
		Map<String, String> queryPairs = new LinkedHashMap<>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			try {
				queryPairs.put(URLDecoder.decode(pair.substring(0, idx), encoding),
					URLDecoder.decode(pair.substring(idx + 1), encoding));
			} catch (UnsupportedEncodingException e) {
				throw new CFMockException(e);
			}
		}
		return queryPairs;
	}

	private static String getPort(URL url) {
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
