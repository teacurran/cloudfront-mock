package com.wirelust.cfmock.web.servlet;

import static com.wirelust.cfmock.web.servlet.SecurityFilter.PUBLIC_PATHS_PARAM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;

import com.wirelust.cfmock.SignatureValidator;
import com.wirelust.cfmock.web.services.Configuration;

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

	public static final String PUBLIC_PATHS_PARAM = "publicPaths";

	@Inject
	Configuration configuration;

	List<String> keys;

	private Pattern[] publicPaths = new Pattern[0];

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		String keysString = configuration.getSetting("keys");
		keys = Arrays.asList(keysString.split(","));

		if (keys.isEmpty()) {
			throw new IllegalStateException("no keys are configured");
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

		if (pathMatches(request, publicPaths)) {
			filterChain.doFilter(request, servletResponse);
			return;
		}

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
