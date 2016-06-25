package test.com.wirelust.cfmock.web.servlet;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.wirelust.cfmock.web.services.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	private static final String KEY_PAIR_ID_1 = "key1";
	private static final String KEY_PAIR_ID_2 = "key2";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Inject
	Configuration configuration;

	HttpClient client;
	URL pemUrl;
	File keyFile;
	Date expiresDate;
	CookieStore cookieStore;

	@Deployment
	public static WebArchive create() {
		WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");
		testWar.addPackages(true, "com.wirelust.cfmock");
		testWar.addPackage("test.com.wirelust.cfmock");

		testWar.addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/beans.xml")), "beans.xml");
		testWar.addAsWebInfResource(new FileAsset(new File("src/main/webapp/WEB-INF/web.xml")), "web.xml");

		testWar.addAsWebInfResource("defaults.properties", "classes/defaults.properties");

		testWar.addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml")
				.importDependencies(ScopeType.COMPILE)
				.resolve()
				.withTransitivity().asFile());

		addResources(testWar, new File("src/test/resources"));

		File dir = new File("src/test/resources/web");
		addFilesToWebArchive(testWar, dir);

		System.out.println("test.war:" + testWar.toString(true).replaceAll("\n", "\n\t"));
		LOGGER.debug("test deployment: {}", testWar.toString(true));

		return testWar;
	}

	@Before
	public void init() throws Exception {
		expiresDate = new Date(new Date().getTime() + EXPIRES_IN);

		cookieStore = new BasicCookieStore();

		client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

		pemUrl = this.getClass().getClassLoader().getResource("keys/private_key.pem");

		// we have to copy the pem file to an external file because AWS SDK won't load it from the .war file
		keyFile = tempFolder.newFile("private_key.pem");
		FileUtils.copyURLToFile(pemUrl, keyFile);

		configuration.setSetting("root", "/");
		configuration.setSetting("root.inwar", "true");
	}

	@Test
	public void shouldBeAbleToAccessSignedUrl() throws Exception {

		String url = ROOT_URL + "/web/content/moby-dick/OPS/toc.xhtml";

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, url, KEY_PAIR_ID_1, expiresDate);

		HttpGet get = new HttpGet(signedUrl);
		HttpResponse response = client.execute(get);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

		HttpEntity httpEntity = response.getEntity();
		String responseContent = IOUtils.toString(httpEntity.getContent(), "UTF-8");

		// random string found in the document
		assertTrue(responseContent.contains("Chapter 1. Loomings."));
	}

	@Test
	public void shouldBeAbleToAccessSignedUrlOutsideWar() throws Exception {

		URL chapter1Url = this.getClass().getClassLoader().getResource("web/content/moby-dick/OPS/chapter_001.xhtml");

		File chapter1 = tempFolder.newFile("chapter_001.xhtml");
		FileUtils.copyURLToFile(chapter1Url, chapter1);

		configuration.setSetting("root", tempFolder.getRoot().getAbsolutePath());
		configuration.setSetting("root.inwar", "false");

		String url = ROOT_URL + "/chapter_001.xhtml";

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, url, KEY_PAIR_ID_1, expiresDate);

		HttpGet get = new HttpGet(signedUrl);
		HttpResponse response = client.execute(get);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

		HttpEntity httpEntity = response.getEntity();
		String responseContent = IOUtils.toString(httpEntity.getContent(), "UTF-8");

		// random string found in the document
		assertTrue(responseContent.contains("Call me Ishmael."));
	}

	@Test
	public void shouldNotBeBeAbleToAccessSignedUrlWithInvalidKey() throws Exception {

		String url = ROOT_URL + "/web/content/moby-dick/OPS/toc.xhtml";

		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, url, KEY_PAIR_ID_2, expiresDate);

		HttpGet get = new HttpGet(signedUrl);
		HttpResponse response = client.execute(get);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
	}

	@Test
	public void shouldNotBeAbleToAccessSignedUrlWithoutContent() throws Exception {
		String signedUrl = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(null,
			null, keyFile, ROOT_URL, KEY_PAIR_ID_1, expiresDate);

		HttpGet get = new HttpGet(signedUrl);
		HttpResponse response = client.execute(get);
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusLine().getStatusCode());
	}


	@Test
	public void shouldBeAbleToAccessContentWithSignedCookie() throws Exception {

		String url = ROOT_URL + "/web/content/moby-dick/OPS/toc.xhtml";

		HttpGet get = new HttpGet(url);


		CloudFrontCookieSigner.CookiesForCannedPolicy cookiesForCannedPolicy =
			CloudFrontCookieSigner.getCookiesForCannedPolicy(null, null, keyFile, url, KEY_PAIR_ID_1, expiresDate);

		ClientCookie expiresCookie = getCookie(cookiesForCannedPolicy.getExpires());
		ClientCookie signatureCookie = getCookie(cookiesForCannedPolicy.getSignature());
		ClientCookie keyPairIdCookie = getCookie(cookiesForCannedPolicy.getKeyPairId());

		cookieStore.addCookie(expiresCookie);
		cookieStore.addCookie(signatureCookie);
		cookieStore.addCookie(keyPairIdCookie);

		HttpResponse response = client.execute(get);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

		HttpEntity httpEntity = response.getEntity();
		String responseContent = IOUtils.toString(httpEntity.getContent(), "UTF-8");

		// random string found in the document
		assertTrue(responseContent.contains("Chapter 1. Loomings."));

	}

	private ClientCookie getCookie(final Map.Entry<String, String> entry) {
		BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
		cookie.setDomain("localhost");
		return cookie;
	}

	private static void addResources(WebArchive war, File dir) throws IllegalArgumentException {
		if (dir == null || !dir.isDirectory()) {
			throw new IllegalArgumentException("not a directory");
		}
		for (File f : dir.listFiles()) {
			if (f.isFile()) {
				war.addAsResource(f, f.getPath().replace("\\", "/").substring("src/test/resources/".length()));
			} else {
				addResources(war, f);
			}
		}
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
