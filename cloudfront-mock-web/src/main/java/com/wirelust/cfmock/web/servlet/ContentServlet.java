package com.wirelust.cfmock.web.servlet;

import java.io.File;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.wirelust.cfmock.web.services.Configuration;
import org.omnifaces.servlet.FileServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 18-Jun-2016
 *
 * @author T. Curran
 */
@WebServlet("/*")
public class ContentServlet extends FileServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentServlet.class);

	@Inject
	Configuration configuration;

	@Override
	protected File getFile(HttpServletRequest httpServletRequest) {

		String pathInfo = httpServletRequest.getPathInfo();

		if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
			throw new IllegalArgumentException();
		}

		String rootPath;
		if (configuration.getSettingBool("root.inwar")) {
			rootPath = httpServletRequest.getServletContext().getRealPath(configuration.getSetting("root"));
		} else {
			rootPath = configuration.getSetting("root");
		}

		File file = new File(rootPath, pathInfo);
		LOGGER.debug("serving file:{}", file.getAbsolutePath());

		return file;
	}
}
