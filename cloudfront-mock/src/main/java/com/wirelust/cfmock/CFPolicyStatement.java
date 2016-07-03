package com.wirelust.cfmock;

import java.util.Date;

/**
 * Date: 26-Jun-2016
 *
 * @author T. Curran
 */
public class CFPolicyStatement {

	String resource;
	Date dateLessThan;
	Date dateGreaterThan;
	String ipAddress;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Date getDateLessThan() {
		return dateLessThan;
	}

	public void setDateLessThan(Date dateLessThan) {
		this.dateLessThan = dateLessThan;
	}

	public Date getDateGreaterThan() {
		return dateGreaterThan;
	}

	public void setDateGreaterThan(Date dateGreaterThan) {
		this.dateGreaterThan = dateGreaterThan;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
