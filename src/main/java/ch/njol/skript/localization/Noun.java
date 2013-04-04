/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class Noun extends Message {
	
	private String singular, plural;
	private int gender = 0;
	
	public Noun(final String key) {
		super(key);
	}
	
	@Override
	protected void onValueChange() {
		String value = getValue();
		if (value == null) {
			plural = singular = key;
			gender = 0;
			return;
		}
		final int g = value.indexOf('@');
		if (g != -1) {
			gender = getGenderID(value.substring(g + 1).trim());
			value = value.substring(0, g).trim();
		} else {
			gender = 0;
		}
		final Pair<String, String> p = Language.getPlural(value);
		singular = p.first;
		plural = p.second;
	}
	
	@Override
	public String toString() {
		validate();
		return singular;
	}
	
	public String toString(final boolean plural) {
		validate();
		return plural ? this.plural : singular;
	}
	
	public String withIndefiniteArticle() {
		validate();
		return (indefiniteArticles.get(gender).isEmpty() ? "" : indefiniteArticles.get(gender) + " ") + singular;
	}
	
	public String getIndefiniteArticle() {
		validate();
		return indefiniteArticles.get(gender);
	}
	
	public String withDefiniteArticle() {
		validate();
		return (definiteArticles.get(gender).isEmpty() ? "" : definiteArticles.get(gender) + " ") + singular;
	}
	
	public String withDefiniteArticle(final boolean plural) {
		validate();
		return plural ? (definitePluralArticle.isEmpty() ? "" : definitePluralArticle + " ") + this.plural : withDefiniteArticle();
	}
	
	public String getDefiniteArticle() {
		validate();
		return definiteArticles.get(gender);
	}
	
	public String toString(final int amount) {
		validate();
		return amount + " " + (amount == 1 ? singular : plural);
	}
	
	public String toString(final double amount) {
		validate();
		return Skript.toString(amount) + " " + (amount == 1 ? singular : plural);
	}
	
	public String getSingular() {
		validate();
		return singular;
	}
	
	public String getPlural() {
		validate();
		return plural;
	}
	
	private final static HashMap<String, Integer> genders = new HashMap<String, Integer>();
	
	private int getGenderID(final String gender) {
		final Integer i = genders.get(gender);
		if (i != null)
			return i;
		Skript.warning("Undefined gender '" + gender + "' at " + key);
		return 0;
	}
	
	private final static List<String> indefiniteArticles = new ArrayList<String>(3);
	private final static List<String> definiteArticles = new ArrayList<String>(3);
	private static String definitePluralArticle = "";
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				genders.clear();
				indefiniteArticles.clear();
				definiteArticles.clear();
				for (int i = 0; i < 100; i++) {
					final String g = Language.localized.get("genders." + i + ".name");
					if (g == null)
						break;
					genders.put(g, i);
					final String ia = Language.localized.get("genders." + i + ".indefinite article");
					indefiniteArticles.add(ia == null ? "" : ia);
					final String da = Language.localized.get("genders." + i + ".definite article");
					definiteArticles.add(da == null ? "" : da);
				}
				if (genders.isEmpty()) {
					Skript.error("No genders defined in language file " + Language.getName() + ".lang!");
					indefiniteArticles.add("");
					definiteArticles.add("");
				}
				final String dpa = Language.localized.get("gender.plural.definite article");
				definitePluralArticle = dpa == null ? "" : dpa;
			}
		}, LanguageListenerPriority.EARLIEST);
	}
	
	public final static String stripIndefiniteArticle(final String s) {
		for (final String a : indefiniteArticles) {
			if (StringUtils.startsWithIgnoreCase(s, a + " "))
				return s.substring(a.length() + 1);
		}
		return s;
	}
	
}
