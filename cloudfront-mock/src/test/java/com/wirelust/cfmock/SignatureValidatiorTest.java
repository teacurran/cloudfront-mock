package com.wirelust.cfmock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.wirelust.cfmock.exceptions.CFMockException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Date: 16-Jun-2016
 *
 * @author T. Curran
 */
public class SignatureValidatiorTest {

	private static final long EXPIRES_IN = 3600000;

	URL pemUrl;
	File keyFile;
	String keyPairId = "test-keypair";
	Date expiresDate;
	String testUrl = "http://localhost/test/url.html";

	@Before
	public void init() throws Exception {
		pemUrl = this.getClass().getClassLoader().getResource("keys/private_key.pem");
		if (pemUrl != null) {
			keyFile = new File(pemUrl.toURI());
		}
		expiresDate = new Date(new Date().getTime() + EXPIRES_IN);
	}

	@Test
	public void shouldBeAbleToValidateSignedURL() throws Exception {
		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, testUrl, keyPairId, expiresDate);

		assertTrue(SignatureValidator.validateSignedUrl(keyFile, signedUrl));
	}

	@Test
	public void shouldBeAbleToValidateSignedURLWithCannedPolicy() throws Exception {
		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, testUrl, keyPairId, expiresDate);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setType(SignedRequest.Type.REQUEST);
		signedRequest.setUrl(signedUrl);
		signedRequest.setExpires(expiresDate);
		signedRequest.setKeyFile(keyFile);
		signedRequest.setKeyId(keyPairId);

		assertTrue(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldValidateParametersForSignedRequest() throws Exception {
		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, testUrl, keyPairId, expiresDate);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setType(null);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("request type cannot be null"));
		}

		signedRequest.setType(SignedRequest.Type.REQUEST);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("key file cannot be null"));
		}

		signedRequest.setKeyFile(keyFile);
		signedRequest.setUrl(signedUrl);
		signedRequest.setExpires(expiresDate);
		signedRequest.setKeyId(keyPairId);
		assertTrue(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldValidateParametersForSignedCookieWithCannedPolicy() throws Exception {
		CloudFrontCookieSigner.CookiesForCannedPolicy cfcp = CloudFrontCookieSigner.getCookiesForCannedPolicy(null,
			null, keyFile, testUrl, keyPairId, expiresDate);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setType(SignedRequest.Type.COOKIE);
		signedRequest.setKeyFile(keyFile);
		signedRequest.setUrl(testUrl);

		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("key id cannot be null"));
		}

		signedRequest.setKeyId(keyPairId);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("either expires or policy must be set"));
		}

		signedRequest.setExpires(expiresDate);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("signature cannot be null"));
		}

		signedRequest.setSignature(cfcp.getSignature().getValue());

		assertTrue(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldValidateParametersForSignedCookieWithCustomPolicy() throws Exception {
		CloudFrontCookieSigner.CookiesForCustomPolicy cfcp = CloudFrontCookieSigner.getCookiesForCustomPolicy(null,
			null, keyFile, testUrl, keyPairId, expiresDate, null, null);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setType(SignedRequest.Type.COOKIE);
		signedRequest.setKeyFile(keyFile);
		signedRequest.setUrl(testUrl);

		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("key id cannot be null"));
		}

		signedRequest.setKeyId(keyPairId);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("either expires or policy must be set"));
		}

		CFPolicy cfPolicy = new CFPolicy();
		CFPolicyStatement cfPolicyStatement = new CFPolicyStatement();
		cfPolicyStatement.setDateLessThan(expiresDate);
		cfPolicyStatement.setResource(testUrl);
		signedRequest.setPolicy(cfPolicy);

		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("signature cannot be null"));
		}

		signedRequest.setSignature(cfcp.getSignature().getValue());

		// no statement
		cfPolicy.setStatements(null);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Only one policy statement supported"));
		}

		cfPolicy.addStatement(cfPolicyStatement);
		cfPolicy.addStatement(cfPolicyStatement);

		// two statements
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Only one policy statement supported"));
		}

		cfPolicy.setStatements(null);
		cfPolicy.addStatement(cfPolicyStatement);

		cfPolicyStatement.setDateGreaterThan(new Date(new Date().getTime() + 20000));
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Signature is not valid until"));
		}
		cfPolicyStatement.setDateGreaterThan(null);

		cfPolicyStatement.setDateLessThan(null);
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Signature is expired"));
		}

		cfPolicyStatement.setDateLessThan(new Date(new Date().getTime() - 20000));
		try {
			SignatureValidator.validateSignature(signedRequest);
			Assert.fail();
		} catch (CFMockException e) {
			assertTrue(e.getMessage().contains("Signature is expired"));
		}
		cfPolicyStatement.setDateGreaterThan(null);

		cfPolicyStatement.setDateLessThan(expiresDate);
		assertTrue(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldBeAbleToMatchWildCard() throws Exception {

		CFPolicy cfPolicy = new CFPolicy();
		CFPolicyStatement statement = new CFPolicyStatement();
		statement.setDateLessThan(expiresDate);
		statement.setResource("http*://localhost/*");
		cfPolicy.addStatement(statement);

		CloudFrontCookieSigner.CookiesForCustomPolicy cookiesForCustomPolicy = CloudFrontCookieSigner
			.getCookiesForCustomPolicy(null, null, keyFile, statement.getResource(), keyPairId,
				statement.getDateLessThan(), statement.getDateGreaterThan(), null);
		String signature = cookiesForCustomPolicy.getSignature().getValue();

		assertTrue(SignatureValidator.validateSignature("http://localhost/1234", keyFile, keyPairId, cfPolicy,
			signature));

		assertTrue(SignatureValidator.validateSignature("https://localhost/1234", keyFile, keyPairId, cfPolicy,
			signature));

		assertFalse(SignatureValidator.validateSignature("http://google.com/1234", keyFile, keyPairId, cfPolicy,
			signature));
	}

	@Test
	public void shouldBeAbleToMatchIPAddress() throws Exception {

		CloudFrontCookieSigner.CookiesForCustomPolicy cfcp = CloudFrontCookieSigner.getCookiesForCustomPolicy(null,
			null, keyFile, null, keyPairId, expiresDate, null, null);


		CFPolicy cfPolicy = new CFPolicy();
		CFPolicyStatement statement = new CFPolicyStatement();
		statement.setDateLessThan(expiresDate);
		statement.setIpAddress("192.0.2.0/24");
		cfPolicy.addStatement(statement);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setKeyFile(keyFile);
		signedRequest.setKeyId(keyPairId);
		signedRequest.setRemoteIpAddress("192.0.2.34");
		signedRequest.setPolicy(cfPolicy);
		signedRequest.setSignature(cfcp.getSignature().getValue());

		assertTrue(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldNotBeAbleToMatchIPAddress() throws Exception {

		CloudFrontCookieSigner.CookiesForCustomPolicy cfcp = CloudFrontCookieSigner.getCookiesForCustomPolicy(null,
			null, keyFile, null, keyPairId, expiresDate, null, null);


		CFPolicy cfPolicy = new CFPolicy();
		CFPolicyStatement statement = new CFPolicyStatement();
		statement.setDateLessThan(expiresDate);
		statement.setIpAddress("192.0.2.0/24");
		cfPolicy.addStatement(statement);

		SignedRequest signedRequest = new SignedRequest();
		signedRequest.setKeyFile(keyFile);
		signedRequest.setKeyId(keyPairId);
		signedRequest.setRemoteIpAddress("10.10.10.10");
		signedRequest.setPolicy(cfPolicy);
		signedRequest.setSignature(cfcp.getSignature().getValue());

		assertFalse(SignatureValidator.validateSignature(signedRequest));
	}

	@Test
	public void shouldNotBeAbleToSignWithBadPemKey() throws Exception {

		String url = "http://localhost/test/url.html";

		Date expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null, null, keyFile, url, keyPairId,
			expiresDate);

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
