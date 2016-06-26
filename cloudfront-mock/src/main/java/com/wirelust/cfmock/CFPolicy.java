package com.wirelust.cfmock;

import java.beans.Statement;
import java.util.List;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class CFPolicy {

	List<CFPolicyStatement> statements;

	public List<CFPolicyStatement> getStatements() {
		return statements;
	}

	public void setStatements(List<CFPolicyStatement> statements) {
		this.statements = statements;
	}
}
