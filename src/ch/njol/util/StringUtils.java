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
	 * @return
	 */
	public final static String replaceAll(final String string, final String regex, final Callback<String, Matcher> callback) {
		final Matcher m = Pattern.compile(regex).matcher(string);
		final StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, callback.run(m));
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
	
}
