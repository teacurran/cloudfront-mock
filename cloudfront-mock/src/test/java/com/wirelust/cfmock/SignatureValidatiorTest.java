package com.wirelust.cfmock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.amazonaws.services.dynamodbv2.xspec.B;
import com.wirelust.cfmock.exceptions.CFMockException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Date: 16-Jun-2016
 *
 * @author T. Curran
 */
public class SignatureValidatiorTest {

	private static final long EXPIRES_IN = 3600000;

	URL pemUrl;
	File keyFile;

	@Before
	public void init() throws Exception {
		pemUrl = this.getClass().getClassLoader().getResource("keys/private_key.pem");
		if (pemUrl != null) {
			keyFile = new File(pemUrl.toURI());
		}
	}

	@Test
	public void shouldBeAbleToValidateSignedURL() throws Exception {
		String distributionDomain = "localhost";

		String keyPairId = "test-keypair";

		String s3Path = "test/url.html";

		Date expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(SignerUtils.Protocol.https,
			distributionDomain, keyFile, s3Path, keyPairId, expiresDate);

		assertTrue(SignatureValidator.validateSignedUrl(keyFile, signedUrl));
	}

	@Test
	public void shouldNotBeAbleToSignWithBadPemKey() throws Exception {
		String distributionDomain = "localhost";

		String keyPairId = "test-keypair";

		String s3Path = "test/url.html";

		Date expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(SignerUtils.Protocol.https,
			distributionDomain, keyFile, s3Path, keyPairId, expiresDate);

		try {
			SignatureValidator.validateSignedUrl(new File("/file/does/not/exist.pem"), signedUrl);

			fail();
		} catch (CFMockException e) {
			assertTrue(e.getCause() instanceof FileNotFoundException);
		}
	}

	@Test
	public void shouldNotAcceptMalformedUrl() {
		try {
			SignatureValidator.validateSignedUrl(keyFile, "bad URL");

			fail();
		} catch (CFMockException e) {
			assertTrue(e.getCause() instanceof MalformedURLException);
		}
	}

	@Test
	public void shouldNotAcceptExpiredToken() {
		try {
			long expires = new Date().getTime()/1000 - 2000;
			SignatureValidator.validateSignedUrl(keyFile, "http://localhost/test.html?Expires=" + expires);

			fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Signature is expired"));
		}
	}

	@Test
	public void shouldBeAbleToSplitQuery() {
		Map<String, String> queries = SignatureValidator.splitQuery("key1=value1&key2=value2&key3=value3");
		assertEquals("value2", queries.get("key2"));
	}

	@Test
	public void shouldHandleUnsupportedEncodingForQuerySplit() {
		try {
			Map<String, String> queries = SignatureValidator.splitQuery("key1=value1&key2=value2&key3=value3", "");

			fail();
		} catch (CFMockException e) {
			assertTrue(e.getCause() instanceof UnsupportedEncodingException);
		}
	}

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<SignatureValidator> constructor = SignatureValidator.class.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}
