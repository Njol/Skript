/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class StringUtils {
	
	/**
	 * Appends the english order suffix to the given number.
	 * 
	 * @param i the number
	 * @return 1st, 2nd, 3rd, 4th, etc.
	 */
	public static String fancyOrderNumber(final int i) {
		final int imod10 = i % 10;
		if (imod10 == 1)
			return i + "st";
		if (imod10 == 2)
			return i + "nd";
		if (imod10 == 3)
			return i + "rd";
		return i + "th";
	}
	
	/**
	 * Performs regex replacing using a callback.
	 * 
	 * @param string the String in which should be searched & replaced
	 * @param regex the Regex to match
	 * @param callback the callback will be run for every match of the regex in the string, and should return the replacement string for the given match.
	 *            If the callback returns null for any given match this function will immediately terminate and return null.
	 * @return
	 */
	public final static String replaceAll(final String string, final String regex, final Callback<String, Matcher> callback) {
		final Matcher m = Pattern.compile(regex).matcher(string);
		final StringBuffer sb = new StringBuffer();
		while (m.find()) {
			final String r = callback.run(m);
			if (r == null)
				return null;
			m.appendReplacement(sb, r);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static int count(final String s, final char c) {
		int r = 0;
		for (final char x : s.toCharArray())
			if (x == c)
				r++;
		return r;
	}
	
	public static int count(final String s, final char c, final int start) {
		return count(s, c, start, s.length());
	}
	
	public static int count(final String s, final char c, final int start, final int end) {
		if (start < 0 || end > s.length())
			throw new StringIndexOutOfBoundsException("invalid start/end indices " + start + "," + end + " for string \"" + s + "\" (length " + s.length() + ")");
		int r = 0;
		for (int i = start; i < end; i++) {
			if (s.charAt(i) == c)
				r++;
		}
		return r;
	}
	
	/**
	 * Gets a rounded representation of a number
	 * 
	 * @param d The number to be turned into a string
	 * @param accuracy Maximum number of digits after the period
	 * @return
	 */
	public static final String toString(final double d, final int accuracy) {
		final String s = "" + d;
		final int c = s.indexOf('.');
		if (c == -1)
			return s;
		int i = Math.min(c + accuracy, s.length() - 1);
		while (i >= c && (s.charAt(i) == '0' || s.charAt(i) == '.'))
			i--;
		return s.substring(0, i + 1);
	}
	
	public static final String firstToUpper(final String s) {
		if (s.isEmpty())
			return s;
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	public static final String substring(final String s, int start, int end) {
		if (start < 0)
			start = start + s.length();
		if (end < 0)
			end = end + s.length();
		if (end < start)
			throw new IllegalArgumentException("invalid indices");
		return s.substring(start, end);
	}
	
	/**
	 * Capitalizes the first character of the string and all characters that follow periods.
	 * 
	 * @param string
	 * @return
	 */
	public static String fixCapitalization(final String string) {
		final char[] s = string.toCharArray();
		int c = 0;
		while (c != -1) {
			while (c < s.length && (s[c] == '.' || Character.isWhitespace(s[c])))
				c++;
			if (c == s.length)
				return new String(s);
			if (c == 0 || s[c - 1] != '.') // don't capitalize directly after a dot - fixes URLs
				s[c] = Character.toUpperCase(s[c]);
			c = indexOf(s, '.', c + 1);
		}
		return new String(s);
	}
	
	private final static int indexOf(final char[] s, final char c, final int start) {
		for (int i = start; i < s.length; i++)
			if (s[i] == c)
				return i;
		return -1;
	}
	
	/**
	 * Finds a number before the specified index (if any). Only whitespace is allowed between the index and the number.
	 * 
	 * @param s
	 * @param index
	 * @return the number or -1 if none.
	 */
	public final static int numberBefore(final CharSequence s, final int index) {
		boolean stillWhitespace = true;
		int start = -1, end = -1;
		for (int i = index - 1; i >= 0; i--) {
			if ('0' < s.charAt(i) && s.charAt(i) < '9') {
				if (start == -1)
					start = end = i;
				else
					start--;
				stillWhitespace = false;
			} else if (Character.isWhitespace(s.charAt(i))) {
				if (stillWhitespace)
					continue;
				break;
			} else {
				break;
			}
		}
		if (start == -1)
			return -1;
		return Integer.parseInt(s.subSequence(start, end + 1).toString());
	}
	
	public static boolean startsWithIgnoreCase(final String string, final String start) {
		Validate.notNull(string, start);
		if (string.length() < start.length())
			return false;
		return string.substring(0, start.length()).equalsIgnoreCase(start);
	}
	
}
