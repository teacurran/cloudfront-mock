package com.wirelust.cfmock.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 27-Jun-2016
 *
 * @author T. Curran
 */
public class WildcardMatcher {

	private WildcardMatcher() {
		// static class can't be instantiated
	}

	public static boolean matches(final String input, final String pattern) {
		Pattern regexPattern = Pattern.compile(wildcardToRegex(pattern));
		Matcher matcher = regexPattern.matcher(input);
		return matcher.matches();
	}

	public static String wildcardToRegex(final String input) {
		StringBuilder sb = new StringBuilder(input.length() + 10);
		sb.append('^');
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			if (c == '*') {
				sb.append(".*");
			} else if (c == '?') {
				sb.append('.');
			} else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
				sb.append('\\');
				sb.append(c);
			} else {
				sb.append(c);
			}
		}
		sb.append('$');
		return sb.toString();
	}
}
