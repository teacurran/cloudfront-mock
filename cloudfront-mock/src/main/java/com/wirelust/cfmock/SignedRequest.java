package com.wirelust.cfmock;

import java.io.File;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Date: 23-Jun-2016
 *
 * @author T. Curran
 */
public class SignedRequest {

	public enum Type {
		REQUEST, COOKIE
	}

	@NotNull
	Type type = Type.COOKIE;

	String keyId;

	@NotNull
	File keyFile;
	String url;
	Date expires;
	String signature;
	CFPolicy policy;

	@Pattern(regexp = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})")
	String remoteIpAddress;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public CFPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(CFPolicy policy) {
		this.policy = policy;
	}

	public String getRemoteIpAddress() {
		return remoteIpAddress;
	}

	public void setRemoteIpAddress(String remoteIpAddress) {
		this.remoteIpAddress = remoteIpAddress;
	}
}
