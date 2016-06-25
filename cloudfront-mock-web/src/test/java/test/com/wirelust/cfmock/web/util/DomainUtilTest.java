package test.com.wirelust.cfmock.web.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.wirelust.cfmock.web.util.DomainUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Date: 25-Jun-2016
 *
 * @author T. Curran
 */
public class DomainUtilTest {

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstantsConstructorIsPrivate() throws Exception {
		Constructor<DomainUtil> constructor = DomainUtil.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void shouldMatchComDomain() {
		assertEquals("wirelust.com", DomainUtil.getTopLevelDomain("www.wirelust.com"));
		assertEquals("wirelust.com", DomainUtil.getTopLevelDomain("third.www.wirelust.com"));
	}

	@Test
	public void shouldMatchLocalhost() {
		assertEquals("localhost", DomainUtil.getTopLevelDomain("localhost"));
		assertEquals("127.0.0.1", DomainUtil.getTopLevelDomain("127.0.0.1"));
	}

	@Test
	public void shouldMatchUkDomain() {
		assertEquals("wirelust.co.uk", DomainUtil.getTopLevelDomain("www.wirelust.co.uk"));
		assertEquals("wirelust.co.uk", DomainUtil.getTopLevelDomain("third.www.wirelust.co.uk"));
	}

	@Test
	public void shouldMatchIoDomain() {
		assertEquals("wirelust.io", DomainUtil.getTopLevelDomain("www.wirelust.io"));
		assertEquals("wirelust.io", DomainUtil.getTopLevelDomain("third.www.wirelust.io"));
	}
}
