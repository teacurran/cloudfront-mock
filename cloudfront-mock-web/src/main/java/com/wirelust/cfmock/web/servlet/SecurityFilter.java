package com.wirelust.cfmock.web.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.wirelust.cfmock.SignatureValidator;
import com.wirelust.cfmock.web.services.Configuration;

/**
 * Date: 18-Jun-2016
 *
 * @author T. Curran
 */
@WebFilter(urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

	@Inject
	Configuration configuration;

	List<String> keys;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String keysString = configuration.getSetting("keys");
		keys = Arrays.asList(keysString.split(","));

		if (keys.isEmpty()) {
			throw new IllegalStateException("no keys are configured");
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;

		String keyId = servletRequest.getParameter(SignatureValidator.PARAM_KEY_PAIR_ID);
		if (keyId != null && keys.contains(keyId)) {

			Appendable requestUrl = request.getRequestURL();
			requestUrl.append("?").append(request.getQueryString());

			String keyLocation = configuration.getSetting("key." + keyId + ".location");
			if (SignatureValidator.validateSignedUrl(new File(keyLocation), requestUrl.toString())) {
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}
		} else {
			// check for cookie access
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

}
