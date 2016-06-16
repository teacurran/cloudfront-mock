package com.wirelust.cfmock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

/**
 * Date: 16-Jun-2016
 *
 * @author T. Curran
 */
public class ConstantsTest {

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstantsConstructorIsPrivate() throws Exception {
		Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
		Assert.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}
