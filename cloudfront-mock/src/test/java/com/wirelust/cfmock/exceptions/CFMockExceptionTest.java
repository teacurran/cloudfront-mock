package com.wirelust.cfmock.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Date: 05-Jul-2016
 *
 * @author T. Curran
 */
public class CFMockExceptionTest {

	private static final String TEST_MESSAGE = "This is a test";

	@Test
	public void shouldBeAbleToCreateExceptionWithMessage() {
		CFMockException serviceException = new CFMockException(TEST_MESSAGE);
		assertEquals(TEST_MESSAGE, serviceException.getMessage());
	}

	@Test
	public void shouldBeAbleToCreateExceptionWithCause() {
		CFMockException serviceException = new CFMockException(new IllegalStateException("foo"));
		assertTrue(serviceException.getCause() instanceof IllegalStateException);
	}

	@Test
	public void shouldBeAbleToCreateExceptionWithMessageAndCause() {
		CFMockException serviceException = new CFMockException(TEST_MESSAGE, new IllegalStateException("foo"));
		assertEquals(TEST_MESSAGE, serviceException.getMessage());
		assertTrue(serviceException.getCause() instanceof IllegalStateException);
	}

}
