/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.commons;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Cleans up a candidate domain name into a valid one. Inspired from guava InternetDomainName
 */
public final class InternetDomainNameCleaner {

    private static final CharMatcher DOTS_MATCHER =
            CharMatcher.anyOf(".\u3002\uFF0E\uFF61");
    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Joiner DOT_JOINER = Joiner.on('.');

    /**
     * Maximum parts (labels) in a domain name. This value arises from
     * the 255-octet limit described in
     * <a href="http://www.ietf.org/rfc/rfc2181.txt">RFC 2181</a> part 11 with
     * the fact that the encoding of each part occupies at least two bytes
     * (dot plus label externally, length byte plus label internally). Thus, if
     * all labels have the minimum size of one byte, 127 of them will fit.
     */
    private static final int MAX_PARTS = 127;

    /**
     * Maximum length of a full domain name, including separators, and
     * leaving room for the root label. See
     * <a href="http://www.ietf.org/rfc/rfc2181.txt">RFC 2181</a> part 11.
     */
    private static final int MAX_LENGTH = 253;

    /**
     * Maximum size of a single part of a domain name. See
     * <a href="http://www.ietf.org/rfc/rfc2181.txt">RFC 2181</a> part 11.
     */
    private static final int MAX_DOMAIN_PART_LENGTH = 63;

    /**
     * The full domain name, converted to lower case.
     */
    private final String name;

    /**
     * The parts of the domain name, converted to lower case.
     */
    private final List<String> parts;

    /**
     * Constructor used to implement {@link #from(String)}, and from subclasses.
     */
    InternetDomainNameCleaner(String name) {
        // Normalize:
        // * ASCII characters to lowercase
        // * All dot-like characters to '.'
        // * Strip trailing '.'

        name = Ascii.toLowerCase(DOTS_MATCHER.replaceFrom(name, '.'));

        if (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }

        if(name.length() > MAX_LENGTH) {
            name = name.substring(MAX_LENGTH);
        }

        this.parts = Lists.newArrayList(DOT_SPLITTER.split(name));
        while (parts.size() > MAX_PARTS) {
            parts.remove(0);
        }
        fixParts(parts);

        this.name = DOT_JOINER.join(parts);
    }

    /**
     * Returns an instance of {@link InternetDomainNameCleaner} after lenient
     * validation.  Specifically, validation against <a
     * href="http://www.ietf.org/rfc/rfc3490.txt">RFC 3490</a>
     * ("Internationalizing Domain Names in Applications") is skipped, while
     * validation against <a
     * href="http://www.ietf.org/rfc/rfc1035.txt">RFC 1035</a> is relaxed in
     * the following ways:
     * <ul>
     * <li>Any part containing non-ASCII characters is considered valid.
     * <li>Underscores ('_') are permitted wherever dashes ('-') are permitted.
     * <li>Parts other than the final part may start with a digit.
     * </ul>
     *
     *
     * @param domain A domain name (not IP address)
     * @throws IllegalArgumentException if {@code name} is not syntactically valid
     *     according to isValid()
     * @since 10.0 (previously named {@code fromLenient})
     */
    public static InternetDomainNameCleaner from(String domain) {
        return new InternetDomainNameCleaner(checkNotNull(domain));
    }

    /**
     * Validation method used by {@from} to ensure that the domain name is
     * syntactically valid according to RFC 1035.
     *
     * @return Is the domain name syntactically valid?
     */
    public static void fixParts(List<String> parts) {
        final int lastIndex = parts.size() - 1;

        // Validate the last part specially, as it has different syntax rules.

        parts.set(lastIndex, getFixedPart(parts.get(lastIndex), true));

        for (int i = 0; i < lastIndex; i++) {
            String part = parts.get(i);
            parts.set(i, getFixedPart(part, false));
        }
    }

    private static final CharMatcher DASH_MATCHER = CharMatcher.anyOf("-_");

    private static final CharMatcher PART_CHAR_MATCHER =
            CharMatcher.JAVA_LETTER_OR_DIGIT.or(DASH_MATCHER);

    /**
     * Helper method for {@link #fixParts(List)}. Validates that one part of
     * a domain name is valid.
     *
     * @param part The domain name part to be validated
     * @param isFinalPart Is this the final (rightmost) domain part?
     * @return Whether the part is valid
     */
    public static String getFixedPart(String part, boolean isFinalPart) {

        // These tests could be collapsed into one big boolean expression, but
        // they have been left as independent tests for clarity.

        if (part.length() > MAX_DOMAIN_PART_LENGTH) {
            part = StringUtils.left(part, MAX_DOMAIN_PART_LENGTH);
        }

    /*
     * GWT claims to support java.lang.Character's char-classification methods,
     * but it actually only works for ASCII. So for now, assume any non-ASCII
     * characters are valid. The only place this seems to be documented is here:
     * http://osdir.com/ml/GoogleWebToolkitContributors/2010-03/msg00178.html
     *
     * <p>ASCII characters in the part are expected to be valid per RFC 1035,
     * with underscore also being allowed due to widespread practice.
     */

        String asciiChars = CharMatcher.ASCII.retainFrom(part);

        if (!part.isEmpty() && !PART_CHAR_MATCHER.matchesAllOf(asciiChars)) {
            part = PART_CHAR_MATCHER.retainFrom(asciiChars);
        }

        // No initial or final dashes or underscores.

        if (!part.isEmpty() && DASH_MATCHER.matches(part.charAt(0))) {
            part = part.substring(1);
        } else if (!part.isEmpty() && DASH_MATCHER.matches(part.charAt(part.length() - 1))) {
            part = part.substring(0, part.length() - 1);
        }

    /*
     * Note that we allow (in contravention of a strict interpretation of the
     * relevant RFCs) domain parts other than the last may begin with a digit
     * (for example, "3com.com"). It's important to disallow an initial digit in
     * the last part; it's the only thing that stops an IPv4 numeric address
     * like 127.0.0.1 from looking like a valid domain name.
     */

        if (!part.isEmpty() && isFinalPart && CharMatcher.DIGIT.matches(part.charAt(0))) {
            part = part.substring(1);
        }

        return part;
    }

    /**
     * Returns the domain name, normalized to all lower case.
     */
    public String name() {
        return name;
    }

    // TODO: specify this to return the same as name(); remove name()
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).toString();
    }

    /**
     * Equality testing is based on the text supplied by the caller,
     * after normalization as described in the class documentation. For
     * example, a non-ASCII Unicode domain name and the Punycode version
     * of the same domain name would not be considered equal.
     *
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof InternetDomainNameCleaner) {
            InternetDomainNameCleaner that = (InternetDomainNameCleaner) object;
            return this.name.equals(that.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
