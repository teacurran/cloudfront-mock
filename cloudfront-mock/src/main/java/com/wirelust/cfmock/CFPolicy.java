package com.wirelust.cfmock;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class CFPolicy {

	List<CFPolicyStatement> statements;

	public void addStatement(CFPolicyStatement statement) {
		this.getStatements().add(statement);
	}

	public List<CFPolicyStatement> getStatements() {
		if (statements == null) {
			statements = new ArrayList<>();
		}
		return statements;
	}

	public void setStatements(List<CFPolicyStatement> statements) {
		this.statements = statements;
	}
}
