package test.com.wirelust.cfmock.web.representations;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.wirelust.cfmock.web.representations.Condition;
import com.wirelust.cfmock.web.representations.Policy;
import com.wirelust.cfmock.web.representations.Statement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class PolicyTest {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	ObjectMapper objectMapper;

	@Before
	public void init() {
		objectMapper = new ObjectMapper();
	}

	@Test
	public void shouldBeAbleToDeserializePolicy() throws Exception {

		ObjectReader objectReader = objectMapper.readerFor(Policy.class);
		Policy policy = objectReader.readValue(
			getClass().getClassLoader().getResourceAsStream("representations/custom_policy.json"));

		assertEquals(1, policy.getStatements().size());

		Statement statement = policy.getStatements().get(0);
		assertEquals("http*://*/web/content/*", statement.getResource());

		Condition condition = statement.getCondition();

		assertEquals(DATE_FORMAT.parse("6/26/2016 14:45:00"), condition.getDateLessThan().getDate());

		assertEquals(DATE_FORMAT.parse("6/26/2016 13:45:00"), condition.getDateGreaterThan().getDate());
	}
}
