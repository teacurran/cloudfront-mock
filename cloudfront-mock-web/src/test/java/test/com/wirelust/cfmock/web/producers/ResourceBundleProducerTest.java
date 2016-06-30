package test.com.wirelust.cfmock.web.producers;

import java.util.Properties;
import javax.inject.Inject;

import com.wirelust.cfmock.web.producers.ResourceBundleProducer;
import com.wirelust.cfmock.web.qualifiers.ClasspathResource;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Date: 20-Jun-2016
 *
 * @author T. Curran
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(ResourceBundleProducer.class)
public class ResourceBundleProducerTest {

	private static final String APPLICATION_NAME = "Personal API";

	@Inject
	@ClasspathResource("defaults.properties")
	Properties defaultProperties;

	@Inject
	@ClasspathResource("defaults.properties")
	Properties defaultProperties2;

	@Inject
	@ClasspathResource("invalid.properties")
	Properties invalidProperties;

	@Inject
	ResourceBundleProducer resourceBundleProducer;

	@Test
	public void shouldBeAbleToLoadResource() {
		Assert.assertEquals("default", defaultProperties.getProperty("applicationSetting"));
	}

	@Test
	public void invalidPropertiesShouldNotLoad() {
		Assert.assertNull(invalidProperties.getProperty("applicationSetting"));
	}

}
