package com.wirelust.cfmock.web.qualifiers;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Date: 20-Jun-2016
 *
 * @author T. Curran
 */
@Retention(RUNTIME)
@Target({METHOD, TYPE, FIELD, PARAMETER})
@Documented
@Qualifier
public @interface ClasspathResource {

	@Nonbinding String value();

}
