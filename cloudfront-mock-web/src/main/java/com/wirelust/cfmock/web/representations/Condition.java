package com.wirelust.cfmock.web.representations;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wirelust.cfmock.web.json.UnixTimestampDeserializer;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class Condition {

	@JsonProperty("DateLessThan")
	DateLessThan dateLessThan;

	@JsonProperty("DateGreaterThan")
	DateGreaterThan dateGreaterThan;

	public DateLessThan getDateLessThan() {
		return dateLessThan;
	}

	public void setDateLessThan(DateLessThan dateLessThan) {
		this.dateLessThan = dateLessThan;
	}

	public DateGreaterThan getDateGreaterThan() {
		return dateGreaterThan;
	}

	public void setDateGreaterThan(DateGreaterThan dateGreaterThan) {
		this.dateGreaterThan = dateGreaterThan;
	}

	public class DateLessThan {
		@JsonProperty("AWS:EpochTime")
		@JsonDeserialize(using = UnixTimestampDeserializer.class)
		Date date;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
	}

	public class DateGreaterThan {
		@JsonProperty("AWS:EpochTime")
		@JsonDeserialize(using = UnixTimestampDeserializer.class)
		Date date;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
	}
}
