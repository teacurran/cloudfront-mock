package com.wirelust.cfmock.web.representations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class Policy {

	@JsonProperty("Statement")
	List<Statement> statements;

	public List<Statement> getStatements() {
		return statements;
	}

	public void setStatements(List<Statement> statements) {
		this.statements = statements;
	}
}
