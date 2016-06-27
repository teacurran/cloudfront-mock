package com.wirelust.cfmock.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 27-Jun-2016
 *
 * @author T. Curran
 */
public class WildcardMatcherTest {

	private static final String SEARCH_STRING = "The quick fox jumped over the lazy dog.";

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstantsConstructorIsPrivate() throws Exception {
		Constructor<WildcardMatcher> constructor = WildcardMatcher.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void shouldBeAbleToMatchWildcard() {
		assertTrue(WildcardMatcher.matches(SEARCH_STRING, "*lazy dog*"));
		assertTrue(WildcardMatcher.matches(SEARCH_STRING, "*lazy dog."));
		assertTrue(WildcardMatcher.matches(SEARCH_STRING, "The quick ?ox jumped over ?he lazy dog."));

		assertFalse(WildcardMatcher.matches(SEARCH_STRING, "*lazy do.."));
		assertFalse(WildcardMatcher.matches(SEARCH_STRING, "*lazy dog"));
		assertFalse(WildcardMatcher.matches(SEARCH_STRING, "The quick [a-z]ox jumped over [a-z]he lazy dog."));
	}
}
