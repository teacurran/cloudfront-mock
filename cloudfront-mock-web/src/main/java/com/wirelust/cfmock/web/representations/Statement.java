package com.wirelust.cfmock.web.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class Statement {

	@JsonProperty("Resource")
	String resource;

	@JsonProperty("Condition")
	Condition condition;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
}
