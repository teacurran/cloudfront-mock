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
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.wirelust.cfmock.exceptions.CFMockException;

public class SignatureValidator {


	public static boolean validateSignedUrl(File keyFile, String resource) {
		URL url;

		try {
			url = new URL(resource);
		} catch (MalformedURLException e) {
			throw new CFMockException(e);
		}

		Map<String, String> queryParams;
		try {
			queryParams = splitQuery(url);
		} catch (UnsupportedEncodingException e) {
			throw new CFMockException(e);
		}

		Date expires = new Date(Long.parseLong(queryParams.get("Expires"))*1000);

		Date now = new Date();
		if (now.getTime() > expires.getTime()) {
			throw new CFMockException("Signature is expired");
		}

		String path = url.getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		String keyPairId = queryParams.get("Key-Pair-Id");

		try {
			String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
						SignerUtils.Protocol.https, url.getHost(), keyFile,
						path, keyPairId, expires);

			if (signedUrl.equals(resource)) {
				return true;
			}
		} catch (InvalidKeySpecException | IOException e) {
			throw new CFMockException(e);
		}


		return false;
	}

	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		Map<String, String> queryPairs = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf('=');
			queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
				URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return queryPairs;
	}
}
