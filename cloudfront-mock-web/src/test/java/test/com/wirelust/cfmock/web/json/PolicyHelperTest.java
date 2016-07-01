package test.com.wirelust.cfmock.web.json;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.wirelust.cfmock.CFPolicy;
import com.wirelust.cfmock.CFPolicyStatement;
import com.wirelust.cfmock.web.json.PolicyHelper;
import com.wirelust.cfmock.web.representations.Policy;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Date: 30-Jun-2016
 *
 * @author T. Curran
 */
public class PolicyHelperTest {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss z");
	ObjectMapper objectMapper;
	ObjectReader objectReader;
	Policy policy;

	@Before
	public void init() throws Exception {
		objectMapper = new ObjectMapper();

		objectReader = objectMapper.readerFor(Policy.class);

		policy = objectReader.readValue(
			getClass().getClassLoader().getResourceAsStream("representations/custom_policy.json"));

	}

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstantsConstructorIsPrivate() throws Exception {
		Constructor<PolicyHelper> constructor = PolicyHelper.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void shouldReturnNullWhenPolicyIsNull() {
		assertNull(PolicyHelper.toCfPolicy(null));
		assertNull(PolicyHelper.toCfPolicyStatement(null));
	}

	@Test
	public void shouldBeAbleToConvertToCfPolicy() throws Exception {

		CFPolicy cfPolicy = PolicyHelper.toCfPolicy(policy);

		assertEquals(1, cfPolicy.getStatements().size());

		CFPolicyStatement statement = cfPolicy.getStatements().get(0);
		assertEquals("http*://*/web/content/*", statement.getResource());

		assertEquals(DATE_FORMAT.parse("6/26/2016 18:45:00 UTC"), statement.getDateLessThan());

		assertEquals(DATE_FORMAT.parse("6/26/2016 17:45:00 UTC"), statement.getDateGreaterThan());
	}

	@Test
	public void shouldBeAbleToConvertToCfPolicyWithNullStatements() throws Exception {

		policy.setStatements(null);

		CFPolicy cfPolicy = PolicyHelper.toCfPolicy(policy);

		assertNotNull(cfPolicy.getStatements());
		assertEquals(0, cfPolicy.getStatements().size());
	}

}
