package test.com.wirelust.cfmock.web.exceptions;

import com.wirelust.cfmock.web.exceptions.ServiceException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Date: 18-Jun-2016
 *
 * @author T. Curran
 */
public class ServiceExceptionTest {

	private static final String TEST_MESSAGE = "This is a test";

	@Test
	public void shouldBeAbleToCreateException() {
		ServiceException serviceException = new ServiceException();
		assertTrue(RuntimeException.class.isAssignableFrom(serviceException.getClass()));
	}

	@Test
	public void shouldBeAbleToCreateExceptionWithMessage() {
		ServiceException serviceException = new ServiceException(TEST_MESSAGE);
		assertEquals(TEST_MESSAGE, serviceException.getMessage());
	}

	@Test
	public void shouldBeAbleToCreateExceptionWithCause() {
		ServiceException serviceException = new ServiceException(new IllegalStateException("foo"));
		assertTrue(serviceException.getCause() instanceof IllegalStateException);
	}

	@Test
	public void shouldBeAbleToCreateExceptionWithMessageAndCause() {
		ServiceException serviceException = new ServiceException(TEST_MESSAGE, new IllegalStateException("foo"));
		assertEquals(TEST_MESSAGE, serviceException.getMessage());
		assertTrue(serviceException.getCause() instanceof IllegalStateException);
	}

}
