package test.com.wirelust.cfmock.web.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Qualifier;

import com.wirelust.cfmock.web.qualifiers.ClasspathResource;
import com.wirelust.cfmock.web.util.DomainUtil;
import com.wirelust.cfmock.web.util.QualifierUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Date: 30-Jun-2016
 *
 * @author T. Curran
 */
public class QualifierUtilTest {

	@ClasspathResource("test.properties")
	Properties testProperties;

	@ClasspathResource("test.properties2")
	@Deprecated
	Properties testProperties2;

	@Deprecated
	Properties testProperties3;

	/**
	 * This method simply instantiates a private constructor to ensure code coverage for it so the
	 * coverage reports aren't diminished
	 */
	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<QualifierUtil> constructor = QualifierUtil.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void shouldBeAbleToGetClasspathResourceValue() throws Exception {

		Field annotatedField = this.getClass().getDeclaredField("testProperties");
		Annotation[] annotations = annotatedField.getDeclaredAnnotations();

		Set<Annotation> annotationSet = new HashSet<>();
		Collections.addAll(annotationSet, annotations);

		InjectionPoint ip = mock(InjectionPoint.class);
		when(ip.getQualifiers()).thenReturn(annotationSet);

		assertEquals("test.properties", QualifierUtil.getClasspathResourceValue(ip));
	}

	@Test
	public void shouldBeAbleToGetClasspathResourceValueWhenMultipleQualifiers() throws Exception {

		Field annotatedField = this.getClass().getDeclaredField("testProperties2");
		Annotation[] annotations = annotatedField.getDeclaredAnnotations();

		Set<Annotation> annotationSet = new HashSet<>();
		Collections.addAll(annotationSet, annotations);

		InjectionPoint ip = mock(InjectionPoint.class);
		when(ip.getQualifiers()).thenReturn(annotationSet);

		assertEquals("test.properties2", QualifierUtil.getClasspathResourceValue(ip));
	}

	@Test
	public void shouldReturnNullWhenClasspathResourceDoesNotExist() throws Exception {

		Field annotatedField = this.getClass().getDeclaredField("testProperties3");
		Annotation[] annotations = annotatedField.getDeclaredAnnotations();

		Set<Annotation> annotationSet = new HashSet<>();
		Collections.addAll(annotationSet, annotations);

		InjectionPoint ip = mock(InjectionPoint.class);
		when(ip.getQualifiers()).thenReturn(annotationSet);

		assertNull(QualifierUtil.getClasspathResourceValue(ip));
	}

}
