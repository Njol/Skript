
= Language Files =
.lang files are in the same format as all Skript configs & scripts.
You can either modify an existing file or create a completely new one with any name you want
and modify the 'language' entry in your config.sk accordingly.

You can either put language files into the lang folder in the jar or into the plugins/Skript/lang/ folder, but files in the lattern folder take precedence
(If there are two files for the same language both will be loaded but the file in the folder will overwrite values from the file in the jar)

Strings that have arguments use Java's formatter, see http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html#syntax.

Please make sure that the version number in your file matches the version number of the english file your
file is based off. It is used to inform the users and yourself if the file is outdated.