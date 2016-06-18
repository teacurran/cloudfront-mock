package test.com.wirelust.cfmock.web.services;

import java.net.URL;

import com.wirelust.cfmock.web.services.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 17-Jun-2016
 *
 * @author T. Curran
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

	@Spy
	Configuration config = new Configuration();


	@Before
	public void init() throws Exception {
		URL configUrl = this.getClass().getResource("/config/test-config.properties");
		System.setProperty(Configuration.ENV_FILE_NAME, configUrl.toURI().getPath());
		config.init();
	}

	@Test
	public void shouldLoadProperties() {
		assertTrue(config.isLoaded());
	}

	@Test
	public void shouldFailWhenFileIsNull() {
		config = new Configuration();
		System.clearProperty(Configuration.ENV_FILE_NAME);
		config.init();
		assertFalse(config.isLoaded());
	}

	@Test
	public void shouldGetNullForNonSetting() {
		assertEquals(null, config.getSetting("test.nonexistant"));
	}

	@Test
	public void shouldBeAbleToReadConfigString() {
		assertEquals("value1", config.getSetting("test.key"));
	}

	@Test
	public void shouldBeAbleToReadConfigStringOrDefault() {
		assertEquals("alternative", config.getSetting("test.key.empty", "alternative"));
		assertEquals("value1", config.getSetting("test.key", "alternative"));
		assertEquals("alternative", config.getSetting("test.key.nonexistant", "alternative"));
	}

	@Test
	public void shouldBeAbleToReadConfigInteger() {
		assertEquals(456345, config.getSettingInt("test.integer"));
		assertEquals(0, config.getSettingInt("test.integer.invalid"));
	}

	@Test
	public void shouldBeAbleToReadConfigIntegerOrDefault() {
		assertEquals(456345, config.getSettingInt("test.integer", 1234));
		assertEquals(1234, config.getSettingInt("test.integer.nonexistant", 1234));
	}

	@Test
	public void shouldBeAbleToReadConfigBool() {
		assertEquals(true, config.getSettingBool("test.bool.true"));
		assertEquals(false, config.getSettingBool("test.bool.false"));
	}

	@Test
	public void shouldBeAbleToReadConfigBoolOrDefault() {
		assertEquals(true, config.getSettingBool("test.bool.true", false));
		assertEquals(false, config.getSettingBool("test.bool.nonexistant", false));
	}

}
