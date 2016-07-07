package com.wirelust.cfmock;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.wirelust.cfmock.exceptions.CFMockException;
import com.wirelust.cfmock.util.WildcardMatcher;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignatureValidator {

	public static final String PARAM_EXPIRES = "Expires";
	public static final String PARAM_KEY_PAIR_ID = "Key-Pair-Id";
	public static final String PARAM_POLICY = "Policy";
	public static final String PARAM_SIGNATURE = "Signature";

	public static final String COOKIE_EXPIRES = "CloudFront-Expires";
	public static final String COOKIE_SIGNATURE = "CloudFront-Signature";
	public static final String COOKIE_POLICY = "CloudFront-Policy";
	public static final String COOKIE_KEY_PAIR_ID = "CloudFront-Key-Pair-Id";

	private static final Logger LOGGER = LoggerFactory.getLogger(SignatureValidator.class);

	private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static final Validator validator = factory.getValidator();

	private SignatureValidator() {
		// static only class
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
											@NotNull final String remoteIp,
											@NotNull final File keyFile,
											@NotNull final String keyId,
											@NotNull final CFPolicy policy,
											@NotNull final String signature) {
		try {

		if (policy.getStatements().isEmpty() || policy.getStatements().size() > 1) {
			throw new CFMockException("Only one policy statement supported at this time");
		}

		CFPolicyStatement statement = policy.getStatements().get(0);
		validateStatement(statement);

		if (statement.getIpAddress() != null) {
			SubnetUtils subnetUtils = new SubnetUtils(statement.getIpAddress());
			if (!subnetUtils.getInfo().isInRange(remoteIp)) {
				return false;
			}
		}

		if (statement.getResource() != null && !WildcardMatcher.matches(url, statement.getResource())) {
			LOGGER.debug("url:{} does not match:{}", url, statement.getResource());
			return false;
		}

		CloudFrontCookieSigner.CookiesForCustomPolicy cookiesForCustomPolicy =
			CloudFrontCookieSigner.getCookiesForCustomPolicy(null, null, keyFile,
				statement.getResource(), keyId, statement.getDateLessThan(), statement.getDateGreaterThan(), null);

			return signature.equals(cookiesForCustomPolicy.getSignature().getValue());
		} catch (InvalidKeySpecException | IOException e) {
			throw new CFMockException("unable to validate cookie", e);
		}

	}


	public static boolean validateSignature(@NotNull final SignedRequest signedRequest) {
		checkForNulls(signedRequest);

		if (signedRequest.getPolicy() == null) {
			return validateSignature(signedRequest.getUrl(),
				signedRequest.getKeyFile(),
				signedRequest.getKeyId(),
				signedRequest.getExpires(),
				signedRequest.getSignature());
		} else {
			return validateSignature(signedRequest.getUrl(),
				signedRequest.getRemoteIpAddress(),
				signedRequest.getKeyFile(),
				signedRequest.getKeyId(),
				signedRequest.getPolicy(),
				signedRequest.getSignature());
		}
	}

	private static void checkForNulls(@NotNull final SignedRequest signedRequest) {
		Set<ConstraintViolation<SignedRequest>> violations = validator.validate(signedRequest);
		if (!violations.isEmpty()) {
			throw new CFMockException("Error validating signed request. errors: " + buildValidationError(violations));
		}
		if (signedRequest.getExpires() == null && signedRequest.getPolicy() == null) {
			throw new CFMockException("either expires or policy");
		}
	}

	private static void validateStatement(@NotNull final CFPolicyStatement statement) {

		validateParameters(statement);

		Date now = new Date();
		if (statement.getDateLessThan() == null || statement.getDateLessThan().getTime() < now.getTime()) {
			throw new CFMockException(Constants.SIGNATURE_IS_EXPIRED);
		}

		if (statement.getDateGreaterThan() != null && statement.getDateGreaterThan().getTime() > now.getTime()) {
			throw new CFMockException(String.format(Constants.SIGNATURE_VALID_AT, statement.getDateGreaterThan()));
		}
	}


	private static <T> void validateParameters(T object) {
		Set<ConstraintViolation<T>> violations = validator.validate(object);
		if (!violations.isEmpty()) {
			throw new CFMockException("Error validating policy. errors: " + buildValidationError(violations));
		}
	}

	private static <T> String buildValidationError(Set<ConstraintViolation<T>> violations) {
		StringBuilder errors = new StringBuilder();
		for (ConstraintViolation<T> violation : violations) {
			if (errors.length() > 0) {
				errors.append(", ");
			}
			errors.append(violation.getPropertyPath())
				.append(" ").append(violation.getMessage());
		}
		return errors.toString();
	}

}
