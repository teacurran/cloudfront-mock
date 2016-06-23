package com.wirelust.cfmock;

import java.io.File;
import java.util.Date;

/**
 * Date: 23-Jun-2016
 *
 * @author T. Curran
 */
public class SignatureRequest {

	public enum Type {
		REQUEST, COOKIE
	}

	Type type;
	String keyId;
	File keyFile;
	String requestUrl;
	Date expires;
	String signature;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public File getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(File keyFile) {
		this.keyFile = keyFile;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
