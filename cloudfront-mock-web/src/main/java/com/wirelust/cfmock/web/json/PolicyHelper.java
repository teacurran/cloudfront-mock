package com.wirelust.cfmock.web.json;

import java.util.ArrayList;
import java.util.List;

import com.wirelust.cfmock.CFPolicy;
import com.wirelust.cfmock.CFPolicyStatement;
import com.wirelust.cfmock.web.representations.Condition;
import com.wirelust.cfmock.web.representations.Policy;
import com.wirelust.cfmock.web.representations.Statement;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class PolicyHelper {

	private PolicyHelper() {
		// helper class can't be instantiated
	}

	public static CFPolicy toCfPolicy(final Policy policy) {
		if (policy == null) {
			return null;
		}
		CFPolicy cfPolicy = new CFPolicy();
		List<CFPolicyStatement> statements = new ArrayList<>();

		if (policy.getStatements() != null) {
			for (Statement statement : policy.getStatements()) {
				statements.add(toCfPolicyStatement(statement));
			}
		}
		cfPolicy.setStatements(statements);
		return cfPolicy;
	}

	public static CFPolicyStatement toCfPolicyStatement(final Statement statement) {
		if (statement == null) {
			return null;
		}

		CFPolicyStatement cfPolicyStatement = new CFPolicyStatement();
		cfPolicyStatement.setResource(statement.getResource());

		Condition condition = statement.getCondition();
		if (condition != null) {
			if (condition.getDateLessThan() != null) {
				cfPolicyStatement.setDateLessThan(condition.getDateLessThan().getDate());
			}
			if (condition.getDateGreaterThan() != null) {
				cfPolicyStatement.setDateGreaterThan(condition.getDateGreaterThan().getDate());
			}
			if (condition.getIpAddress() != null) {
				cfPolicyStatement.setIpAddress(condition.getIpAddress().getValue());
			}
		}
		return cfPolicyStatement;
	}
}
