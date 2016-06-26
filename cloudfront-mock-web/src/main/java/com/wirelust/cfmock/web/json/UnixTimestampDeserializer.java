package com.wirelust.cfmock.web.json;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class UnixTimestampDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		String unixTimestamp = parser.getText().trim();
		return new Date(TimeUnit.SECONDS.toMillis(Long.valueOf(unixTimestamp)));
	}
}
