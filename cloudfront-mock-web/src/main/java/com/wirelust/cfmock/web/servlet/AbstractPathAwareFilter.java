package com.wirelust.cfmock.web.servlet;

import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Date: 20-Jun-2016
 *
 * @author T. Curran
 */
abstract class AbstractPathAwareFilter implements Filter {

	private FilterConfig filterConfig;
	private String root;

	@Override
	public void init(FilterConfig pConfig) throws ServletException {
		filterConfig = pConfig;
		root = pConfig.getServletContext().getContextPath();
		if (root.length() == 0) {
			root = "/";
		}
		compilePaths();
	}

	@Override
	public void destroy() {
	}

	protected abstract void compilePaths();

	protected String getRoot() {
		return root;
	}

	protected FilterConfig getConfig() {
		return filterConfig;
	}

	protected Pattern[] compilePaths(String pPathsParam) {
		Pattern[] retval = new Pattern[0];
		String pathsParam = filterConfig.getInitParameter(pPathsParam);
		if (pathsParam != null) {
			String[] regexes = split(pathsParam, ',');
			retval = new Pattern[regexes.length];
			for (int i = 0; i < regexes.length; i++) {
				retval[i] = Pattern.compile(regexes[i]);
			}
		}
		return retval;
	}

	protected boolean pathMatches(HttpServletRequest pRequest, Pattern[] pPatterns) {
		boolean retval = false;
		String contextPath = pRequest.getContextPath();
		String uri = pRequest.getRequestURI();
		String path = substringAfter(uri, contextPath);

		for (Pattern p : pPatterns) {
			if (p.matcher(path).matches()) {
				retval = true;
				break;
			}
		}

		return retval;
	}


}
