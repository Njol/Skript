
= Language Files =
.lang files are in the same format as all Skript configs & scripts.
To create a new language file it's preferable copy an existing language file and rename it to <language>.lang
and then modify the 'language' entry in your config.sk accordingly.

You can either put language files into the lang folder in the jar or into the plugins/Skript/lang/ folder, but files in the latter folder take precedence
(If there are two files for the same language both will be loaded but the file in the folder will overwrite values from the file in the jar)
The exception to this rule is the default english file which is only loaded from the jar.

Strings that have arguments use Java's formatter syntax, see http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax.

Nouns have special syntax to define their plural and gender:
	Plurals are defined like in aliases.sk: 'name¦s' / 'shel¦f¦ves' / 'word¦¦s¦ of power'
	Genders are defined by adding @<gender> at the end of the noun, e.g. 'word¦s @a' or 'ocelot¦s @an'
		(english uses a/an as genders, while other languages actually have genders)

Please make sure that the version number in your file matches the version number of the english file your
file is based off. It is used to inform the users and yourself if the file is outdated.
