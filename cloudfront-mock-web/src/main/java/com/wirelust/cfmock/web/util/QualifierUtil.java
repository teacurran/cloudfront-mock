package com.wirelust.cfmock.web.util;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.inject.spi.InjectionPoint;

import com.wirelust.cfmock.web.qualifiers.ClasspathResource;

/**
 * Date: 30-Jun-2016
 *
 * @author T. Curran
 */
public class QualifierUtil {

	private QualifierUtil() {
		// util class cannot be instantiated
	}

	public static String getClasspathResourceValue(InjectionPoint ip) {
		String name = null;
		Set<Annotation> qualifiers = ip.getQualifiers();
		for (Annotation qualifier : qualifiers) {
			if (qualifier.annotationType().equals(ClasspathResource.class)) {
				name = ((ClasspathResource) qualifier).value();
				break;
			}
		}
		return name;
	}

}
