package org.codehaus.mojo.wagon.shared;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A copy of plexus-util's SelectorUtils to deal with unix file separator only.
 */
public final class SelectorUtils {

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the first "**".
     * <p>
     * This is not a general purpose test and should only be used if you can live with false positives. For example,
     * <code>pattern=**\a</code> and <code>str=b</code> will yield <code>true</code>.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @return whether or not a given path matches the start of a given pattern up to the first "**".
     */
    public static boolean matchPatternStart(String pattern, String str) {
        return matchPatternStart(pattern, str, true);
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the first "**".
     * <p>
     * This is not a general purpose test and should only be used if you can live with false positives. For example,
     * <code>pattern=**\a</code> and <code>str=b</code> will yield <code>true</code>.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return whether or not a given path matches the start of a given pattern up to the first "**".
     */
    public static boolean matchPatternStart(String pattern, String str, boolean isCaseSensitive) {
        // When str starts with a separator, pattern has to start with a
        // separator.
        // When pattern starts with a separator, str has to start with a
        // separator.
        if (str.startsWith("/") != pattern.startsWith("/")) {
            return false;
        }

        List<String> patDirs = tokenizePath(pattern);
        List<String> strDirs = tokenizePath(str);

        int patIdxStart = 0;
        int patIdxEnd = patDirs.size() - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.size() - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs.get(patIdxStart);
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs.get(strIdxStart), isCaseSensitive)) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }

        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            return true;
        } else if (patIdxStart > patIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else {
            // pattern now holds ** while string is not exhausted
            // this will generate false positives but we can live with that.
            return true;
        }
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code> otherwise.
     */
    public static boolean matchPath(String pattern, String str) {
        return matchPath(pattern, str, true);
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code> otherwise.
     */
    public static boolean matchPath(String pattern, String str, boolean isCaseSensitive) {
        // When str starts with a separator, pattern has to start with a
        // separator.
        // When pattern starts with a separator, str has to start with a
        // separator.
        if (str.startsWith("/") != pattern.startsWith("/")) {
            return false;
        }

        List<String> patDirs = tokenizePath(pattern);
        List<String> strDirs = tokenizePath(str);

        int patIdxStart = 0;
        int patIdxEnd = patDirs.size() - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.size() - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs.get(patIdxStart);
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs.get(strIdxStart), isCaseSensitive)) {
                return false;
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs.get(i).equals("**")) {
                    return false;
                }
            }
            return true;
        } else {
            if (patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                return false;
            }
        }

        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs.get(patIdxEnd);
            if (patDir.equals("**")) {
                break;
            }
            if (!match(patDir, strDirs.get(strIdxEnd), isCaseSensitive)) {
                return false;
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs.get(i).equals("**")) {
                    return false;
                }
            }
            return true;
        }

        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patDirs.get(i).equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = patDirs.get(patIdxStart + j + 1);
                    String subStr = strDirs.get(strIdxStart + i + j);
                    if (!match(subPat, subStr, isCaseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!patDirs.get(i).equals("**")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not be <code>null</code>.
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str) {
        return match(pattern, str, true);
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two special characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str, boolean isCaseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for (char aPatArr : patArr) {
            if (aPatArr == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?' && !equals(ch, strArr[i], isCaseSensitive)) {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && !equals(ch, strArr[strIdxStart], isCaseSensitive)) {
                return false; // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && !equals(ch, strArr[strIdxEnd], isCaseSensitive)) {
                return false; // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?' && !equals(ch, strArr[strIdxStart + i + j], isCaseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether two characters are equal.
     */
    private static boolean equals(char c1, char c2, boolean isCaseSensitive) {
        if (c1 == c2) {
            return true;
        }
        if (!isCaseSensitive) {
            // NOTE: Try both upper case and lower case as done by String.equalsIgnoreCase()
            return Character.toUpperCase(c1) == Character.toUpperCase(c2)
                    || Character.toLowerCase(c1) == Character.toLowerCase(c2);
        }
        return false;
    }

    /**
     * Breaks a path up into a Vector of path elements, tokenizing on <code>File.separator</code>.
     *
     * @param path Path to tokenize. Must not be <code>null</code>.
     * @return a Vector of path elements from the tokenized path
     */
    public static List<String> tokenizePath(String path) {
        List<String> ret = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreTokens()) {
            ret.add(st.nextToken());
        }
        return ret;
    }
}
