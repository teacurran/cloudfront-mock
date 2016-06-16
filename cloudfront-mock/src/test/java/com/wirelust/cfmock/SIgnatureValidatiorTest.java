package com.wirelust.cfmock;

import java.io.File;
import java.net.URL;
import java.util.Date;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Date: 16-Jun-2016
 *
 * @author T. Curran
 */
public class SignatureValidatiorTest {

	private static final long EXPIRES_IN = 3600000;

	@Test
	public void shouldBeAbleToValidateSignedURL() throws Exception {
		String distributionDomain = "localhost";

		URL pemUrl = this.getClass().getClassLoader().getResource("keys/private_key.pem");
		File keyFile = new File(pemUrl.toURI());
		String keyPairId = "test-keypair";

		String s3Path = "test/url.html";

		Date expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(SignerUtils.Protocol.https,
			distributionDomain, keyFile, s3Path, keyPairId, expiresDate);

		assertTrue(SignatureValidator.validateSignedUrl(keyFile, signedUrl));

	}
}
