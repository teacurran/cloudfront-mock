package com.wirelust.cfmock.web.servlet;

import java.io.File;
import java.io.IOException;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wirelust.cfmock.SignatureValidator;
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

		String keyId = servletRequest.getParameter(SignatureValidator.PARAM_KEY_PAIR_ID);
		if (keyId != null && keys.get(keyId) != null) {
			LOGGER.debug("validating against keyId:{}", keyId);

			Appendable requestUrl = request.getRequestURL();
			requestUrl.append("?").append(request.getQueryString());

			if (SignatureValidator.validateSignedUrl(keys.get(keyId), requestUrl.toString())) {
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}
		} else {
			// check for cookie access
		}

		response.sendError(SC_FORBIDDEN);
	}

	@Override
	public void destroy() {
		// do nothing
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
