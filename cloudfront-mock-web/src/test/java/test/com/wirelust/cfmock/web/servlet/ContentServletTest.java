package test.com.wirelust.cfmock.web.servlet;

import java.io.File;
import java.net.URL;
import java.util.Date;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.wirelust.cfmock.web.services.Configuration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 19-Jun-2016
 *
 * @author T. Curran
 */
@RunWith(Arquillian.class)
public class ContentServletTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentServletTest.class);

	private static final String ROOT_URL = "http://localhost:8080/test";
	private static final long EXPIRES_IN = 3600000;

	HttpClient client;
	URL pemUrl;
	File keyFile;
	String keyPairId = "key1";

	@Deployment
	public static WebArchive create() {
		WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
		testWar.addPackages(true, "com.wirelust.cfmock");
		testWar.addPackage("test.com.wirelust.cfmock");

		testWar.addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/beans.xml")), "beans.xml");
		testWar.addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")), "web.xml");

		testWar.addAsWebInfResource("config/test-config.properties", "classes/defaults.properties");

		//testWar.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
		//		.importDependencies(ScopeType.RUNTIME)
		//		.resolve()
		//		.withTransitivity().asFile());
		testWar.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
				.importDependencies(ScopeType.COMPILE)
				.resolve()
				.withTransitivity().asFile());

		File dir = new File("src/test/resources");
		addFilesToWebArchive(testWar, dir);

		System.out.println("test.war:" + testWar.toString(true).replaceAll("\n", "\n\t"));
		LOGGER.debug("test deployment: {}", testWar.toString(true));

		return testWar;
	}

	@Before
	public void init() throws Exception {
		URL configUrl = this.getClass().getResource("/config/test-config.properties");
		System.setProperty(Configuration.ENV_FILE_NAME, configUrl.toURI().getPath());

		client = HttpClients.createDefault();
		pemUrl = this.getClass().getClassLoader().getResource("keys/private_key.pem");
		if (pemUrl != null) {
			keyFile = new File(pemUrl.toURI());
		}
	}

	@Test
	public void shouldBeAbleToAccessSignedUrl() throws Exception {

		String url = ROOT_URL + "/content/moby-dick/OPS/toc.xhtml";

		Date expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			"localhost", keyFile, url, keyPairId, expiresDate);

		LOGGER.info("url:{}", signedUrl);
	}


	private static void addFilesToWebArchive(WebArchive war, File dir) throws IllegalArgumentException {
		if (dir == null || !dir.isDirectory()) {
			throw new IllegalArgumentException("not a directory");
		}
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				war.addAsWebResource(f, f.getPath().replace("\\", "/").substring("src/test/resources/".length()));
			} else {
				addFilesToWebArchive(war, f);
			}
		}
	}

}
