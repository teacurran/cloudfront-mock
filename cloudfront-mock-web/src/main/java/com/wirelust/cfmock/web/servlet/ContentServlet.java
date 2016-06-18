package com.wirelust.cfmock.web.servlet;

import java.io.File;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.wirelust.cfmock.web.services.Configuration;
import org.omnifaces.servlet.FileServlet;

/**
 * Date: 18-Jun-2016
 *
 * @author T. Curran
 */
@WebServlet("/*")
public class ContentServlet extends FileServlet {

	@Inject
	Configuration configuration;

	@Override
	protected File getFile(HttpServletRequest httpServletRequest) {

		String pathInfo = httpServletRequest.getPathInfo();

		if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
			throw new IllegalArgumentException();
		}

		return new File(configuration.getSetting("root"), pathInfo);
	}
}
