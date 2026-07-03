package org.mytest.test.common.utils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author gemo
 * @date 2025/11/28 16:45
 */
public class ParsingUtils {
    private static final String UPPER = "\\p{Lu}|\\P{InBASIC_LATIN}";

    private static final String LOWER = "\\p{Ll}";

    private static final String CAMEL_CASE_REGEX = "(?<!(^|[%u_$]))(?=[%u])|(?<!^)(?=[%u][%l])".replace("%u", UPPER)
            .replace("%l", LOWER);

    private static final Pattern CAMEL_CASE = Pattern.compile(CAMEL_CASE_REGEX);

    private ParsingUtils() {
    }

    /**
     * Splits up the given camel-case {@link String}.
     * @param source must not be {@literal null}.
     * @return
     */
    public static List<String> splitCamelCase(String source) {
        return split(source, false);
    }

    /**
     * Splits up the given camel-case {@link String} and returns the parts in lower case.
     * @param source must not be {@literal null}.
     * @return
     */
    public static List<String> splitCamelCaseToLower(String source) {
        return split(source, true);
    }

    /**
     * Reconcatenates the given camel-case source {@link String} using the given
     * delimiter. Will split up the camel-case {@link String} and use an uncapitalized
     * version of the parts.
     * @param source must not be {@literal null}.
     * @param delimiter must not be {@literal null}.
     * @return
     */
    public static String reConcatenateCamelCase(String source, String delimiter) {

        return collectionToDelimitedString(splitCamelCaseToLower(source), delimiter, "", "");
    }

    public static String collectionToDelimitedString(
            Collection<?> coll, String delim, String prefix, String suffix) {

        if (coll==null || coll.isEmpty()) {
            return "";
        }

        int totalLength = coll.size() * (prefix.length() + suffix.length()) + (coll.size() - 1) * delim.length();
        for (Object element : coll) {
            totalLength += String.valueOf(element).length();
        }

        StringBuilder sb = new StringBuilder(totalLength);
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    private static List<String> split(String source, boolean toLower) {
        String[] parts = CAMEL_CASE.split(source);
        List<String> result = new ArrayList<>(parts.length);

        for (String part : parts) {
            result.add(toLower ? part.toLowerCase() : part);
        }

        return Collections.unmodifiableList(result);
    }
}
