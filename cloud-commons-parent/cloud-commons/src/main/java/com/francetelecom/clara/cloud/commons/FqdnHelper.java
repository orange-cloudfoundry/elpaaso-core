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

import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;
import com.google.common.net.InternetDomainName;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class FqdnHelper {

    public static final CharMatcher FILTERED_CHARS_IN_NAMES = CharMatcher.anyOf(".-" +
            "\u3002\uFF0E\uFF61"); //additional unicode characters synomym for .

    /**
     * Takes a candidate fqdn and removes all invalid parts. if this becomes empty, it returns the default.
     * When too long, trims by removing chars at beginning
     * @param candidateFqdn
     * @param defaultOnEmpty a valid default domain name, otherwise an exception is thrown.
     * @return
     */
    public static String truncateUnsupportedCharsToValidHost(String candidateFqdn, String defaultOnEmpty) {
        if (defaultOnEmpty== null || defaultOnEmpty.isEmpty() || !InternetDomainName.isValid(defaultOnEmpty)) {
            throw new IllegalArgumentException("invalid default (" +defaultOnEmpty + ")");
        }
        String trimmed;
        if (candidateFqdn == null || candidateFqdn.isEmpty()) {
            return defaultOnEmpty;
        }
        trimmed = InternetDomainNameCleaner.from(candidateFqdn).name();
        if (trimmed.isEmpty()) {
            return defaultOnEmpty;
        }
        return trimmed;
    }

    /**
     * Sanitize a subpart of the uri so that it does not contain separators we use in domains.
     * e.g. when trying to construct "c-{conflictid}-{webGuiLabel}-{envLabel}-{appRelease}{appVersion}-{paasInstance}.{cfSubDomain]"
     * this method can be called on each individual part such as "webGuiLabel" so that they don't use
     * dots or dashes and get truncated to the appropriate length by trimming extra chars at beginning.
     * @param uriPart a part of the fqdn (e.g. "webguilabel")
     */
    public static String sanitizeAndTruncatePart(String uriPart, int maxLength) {
        String sanitizedString = FILTERED_CHARS_IN_NAMES.removeFrom(uriPart);
        sanitizedString = Ascii.toLowerCase(sanitizedString);
        sanitizedString = InternetDomainNameCleaner.getFixedPart(sanitizedString, false);
        if (sanitizedString.length() > maxLength) {
            sanitizedString = StringUtils.left(sanitizedString, maxLength);
        }
        sanitizedString = InternetDomainNameCleaner.getFixedPart(sanitizedString, false);
        return sanitizedString;
    }
}
