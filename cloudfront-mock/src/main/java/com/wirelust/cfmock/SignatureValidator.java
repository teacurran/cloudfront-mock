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
import javax.validation.constraints.NotNull;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.wirelust.cfmock.exceptions.CFMockException;
import com.wirelust.cfmock.util.DomainUtil;
import com.wirelust.cfmock.util.WildcardMatcher;
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

		String port = DomainUtil.getPortForUrl(url);

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

	/**
	 *
	 * @param keyFile pem key file
	 * @param keyId id of the key
	 * @param expires date when the signature expires
	 * @param signature signature
	 * @return true if the signature is valid
	 */
	public static boolean validateSignature(@NotNull final String url,
											@NotNull final File keyFile,
											@NotNull final String keyId,
											@NotNull final Date expires,
											@NotNull final String signature) {
		try {
			CloudFrontCookieSigner.CookiesForCannedPolicy cookiesForCannedPolicy =
				CloudFrontCookieSigner.getCookiesForCannedPolicy(null, null, keyFile, url, keyId, expires);

			return signature.equals(cookiesForCannedPolicy.getSignature().getValue());
		} catch (InvalidKeySpecException | IOException e) {
			throw new CFMockException("unable to validate cookie", e);
		}

	}

	public static boolean validateSignature(@NotNull final String url,
											@NotNull final File keyFile,
											@NotNull final String keyId,
											@NotNull final CFPolicy policy,
											@NotNull final String signature) {
		try {

		if (policy.getStatements() == null || policy.getStatements().size() > 1) {
			throw new CFMockException("Only one policy statement supported at this time");
		}

		CFPolicyStatement statement = policy.getStatements().get(0);

		if (!WildcardMatcher.matches(url, statement.getResource())) {
			LOGGER.debug("url:{} does not match:{}", url, statement.getResource());
			return false;
		}

		CloudFrontCookieSigner.CookiesForCustomPolicy cookiesForCustomPolicy =
			CloudFrontCookieSigner.getCookiesForCustomPolicy(null, null, keyFile,
				statement.getResource(), keyId, statement.dateLessThan, statement.getDateGreaterThan(), null);

			return signature.equals(cookiesForCustomPolicy.getSignature().getValue());
		} catch (InvalidKeySpecException | IOException e) {
			throw new CFMockException("unable to validate cookie", e);
		}

	}


	public static boolean validateSignature(@NotNull final SignedRequest signedRequest) {
		checkForNulls(signedRequest);
		if (signedRequest.getType() == SignedRequest.Type.REQUEST) {
			return validateSignedUrl(signedRequest.getKeyFile(), signedRequest.getUrl());
		} else {
			if (signedRequest.getPolicy() == null) {
				return validateSignature(signedRequest.getUrl(),
					signedRequest.getKeyFile(),
					signedRequest.getKeyId(),
					signedRequest.getExpires(),
					signedRequest.getSignature());
			} else {
				return validateSignature(signedRequest.getUrl(),
					signedRequest.getKeyFile(),
					signedRequest.getKeyId(),
					signedRequest.getPolicy(),
					signedRequest.getSignature());
			}
		}
	}

	private static void checkForNulls(@NotNull final SignedRequest signedRequest) {
		if (signedRequest.getType() == null) {
			throw new CFMockException("request type cannot be null");
		}
		if (signedRequest.getKeyFile() == null) {
			throw new CFMockException("key file cannot be null");
		}
		if (signedRequest.getUrl() == null) {
			throw new CFMockException("url cannot be null");
		}
		if (signedRequest.getType() == SignedRequest.Type.COOKIE) {
			checkForCookieNulls(signedRequest);
		}
	}

	private static void checkForCookieNulls(@NotNull final SignedRequest signedRequest) {
		if (signedRequest.getKeyId() == null) {
			throw new CFMockException("key id cannot be null for cookie based signatures");
		}
		if (signedRequest.getExpires() == null && signedRequest.getPolicy() == null) {
			throw new CFMockException("either expires or policy must be set for cookie based signatures");
		}
		if (signedRequest.getSignature() == null) {
			throw new CFMockException("signature cannot be null for cookie based signatures");
		}
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

}
