package com.wirelust.cfmock.web.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import com.amazonaws.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.wirelust.cfmock.SignatureValidator;
import com.wirelust.cfmock.SignedRequest;
import com.wirelust.cfmock.exceptions.CFMockException;
import com.wirelust.cfmock.web.exceptions.ServiceException;
import com.wirelust.cfmock.web.json.PolicyHelper;
import com.wirelust.cfmock.web.representations.Policy;
import com.wirelust.cfmock.web.services.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wirelust.cfmock.web.servlet.SecurityFilter.PUBLIC_PATHS_PARAM;
import static javax.servlet.http.HttpServletResponse.*;

/**
 * Date: 18-Jun-2016
 *
 * @author T. Curran
 */
@WebFilter(urlPatterns = {"/*"},
			initParams = {
				@WebInitParam(name = PUBLIC_PATHS_PARAM, value = "^/ArquillianServletRunner/*"),
		}
)
public class SecurityFilter extends AbstractPathAwareFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

	public static final String PUBLIC_PATHS_PARAM = "publicPaths";

	@Inject
	Configuration configuration;

	HashMap<String, File> keys = new HashMap<>();

	private Pattern[] publicPaths = new Pattern[0];

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		String keysString = configuration.getSetting("keys");

		String[] keyIds = keysString.split(",");
		if (keyIds.length == 0) {
			throw new IllegalStateException("no keys are configured");
		}

		for (String keyId : keyIds) {
			String keyLocation = configuration.getSetting("key." + keyId + ".location");

			if (!findKeyFile(filterConfig.getServletContext(), keyId, keyLocation)) {
				LOGGER.error("unable to find key file id:{} location:{}", keyId, keyLocation);
			}
		}
	}

	@Override
	protected void compilePaths() {
		publicPaths = compilePaths(PUBLIC_PATHS_PARAM);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;

		if (pathMatches(request, publicPaths)) {
			filterChain.doFilter(request, servletResponse);
			return;
		}

		Appendable requestUrl = request.getRequestURL();
		if (request.getQueryString() != null) {
			requestUrl.append("?").append(request.getQueryString());
		}

		SignedRequest signedRequest = new SignedRequest();

		String keyId = servletRequest.getParameter(SignatureValidator.PARAM_KEY_PAIR_ID);
		if (keyId != null) {
			LOGGER.debug("validating against keyId:{}", keyId);

			signedRequest.setType(SignedRequest.Type.REQUEST);
			signedRequest.setKeyFile(keys.get(keyId));
			LOGGER.info("setting key id:{}, file:{}", keyId, keys.get(keyId));
		} else {
			populateSignedRequestFromCookies(signedRequest, request);
		}
		signedRequest.setUrl(requestUrl.toString());

		try {
			if (SignatureValidator.validateSignature(signedRequest)) {
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}
		} catch (CFMockException e) {
			LOGGER.error("unable to validate request", e);
			response.sendError(SC_BAD_REQUEST);
			return;
		}

		response.sendError(SC_FORBIDDEN);
	}

	@Override
	public void destroy() {
		// do nothing
	}

	private void populateSignedRequestFromCookies(SignedRequest signedRequest, HttpServletRequest request) {
		String keyId = getCookieValue(request, SignatureValidator.COOKIE_KEY_PAIR_ID);
		signedRequest.setKeyFile(keys.get(keyId));
		signedRequest.setType(SignedRequest.Type.COOKIE);
		signedRequest.setKeyId(keyId);
		signedRequest.setSignature(getCookieValue(request, SignatureValidator.COOKIE_SIGNATURE));
		LOGGER.info("here 0");
		populateSignedRequestPolicyFromCookies(signedRequest, request);

		String expiresString = getCookieValue(request, SignatureValidator.COOKIE_EXPIRES);
		if (expiresString != null) {
			try {
				signedRequest.setExpires(new Date(Long.parseLong(expiresString)*1000));
			} catch (NumberFormatException e) {
				throw new ServiceException("expires cookie is invalid:" + expiresString);
			}
		}

	}

	private void populateSignedRequestPolicyFromCookies(SignedRequest signedRequest, HttpServletRequest request) {
		String policyBase64 = getCookieValue(request, SignatureValidator.COOKIE_POLICY);
		if (policyBase64 == null) {
			return;
		}
		LOGGER.info("decoding base64:{}", policyBase64);
		try {
			String policyJson = new String(Base64.decode(policyBase64.replaceAll("_", "=")));

			ObjectReader objectReader = new ObjectMapper().readerFor(Policy.class);

			Policy policy = objectReader.readValue(policyJson);
			signedRequest.setPolicy(PolicyHelper.toCfPolicy(policy));
		} catch (IOException e) {
			throw new ServiceException("unable to decode policyBase64:" + policyBase64, e);
		}
	}

	private String getCookieValue(final HttpServletRequest request, final String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private boolean findKeyFile(ServletContext servletContext, String keyId, String keyLocation) {

		File keyFile = new File(keyLocation);
		if (keyFile.exists()) {
			keys.put(keyId, keyFile);
			return true;
		}

		keyFile = new File(servletContext.getRealPath(keyLocation));
		LOGGER.info("looking for key at:{}", keyFile.getAbsolutePath());
		if (keyFile.exists()) {
			keys.put(keyId, keyFile);
			return true;
		}

		return false;
	}

}
