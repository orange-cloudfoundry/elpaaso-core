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

import com.google.common.net.InternetDomainName;
import org.junit.Test;

import java.net.IDN;

import static org.fest.assertions.Assertions.assertThat;

/**
 *
 */
public class FqdnHelperTest {

    FqdnHelper fqdnHelper = new FqdnHelper();

    @Test(expected = IllegalArgumentException.class)
    public void rejects_null_default() {
        fqdnHelper.truncateUnsupportedCharsToValidHost("webgui", null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejects_empty_default() {
        fqdnHelper.truncateUnsupportedCharsToValidHost("webgui", "");
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejects_incorrect_default() {
        fqdnHelper.truncateUnsupportedCharsToValidHost("webgui", "!");
    }

    @Test
    public void leaves_intact_correct_ones() {
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("", "default.com")).isEqualTo("default.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("web.com", "default.com")).isEqualTo("web.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("correct.web.com", "default.com")).isEqualTo("correct.web.com");
    }

    @Test
    public void truncates_single_valid_char_or_defaults_when_empty() {
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("partially!correct.web.com", "default.com")).isEqualTo("partiallycorrect.web.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("partially#correct.web.com", "default.com")).isEqualTo("partiallycorrect.web.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("partially'correct.web.com", "default.com")).isEqualTo("partiallycorrect.web.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("partially correct.web.com", "default.com")).isEqualTo("partiallycorrect.web.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("#!", "default.com")).isEqualTo("default.com");
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("1375198999906/jeeprobe-uat-vdrqa.redacted-domain.org", "default.com")).isEqualTo("1375198999906jeeprobe-uat-vdrqa.redacted-domain.org");
    }

    @Test
    public void truncates_multiple_valid_char_or_defaults_when_empty() {
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("multiple words in phrase.web.com", "default.com")).isEqualTo("multiplewordsinphrase.web.com");
    }

    @Test
    public void normalizes_to_lower_case() {
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost("passMoiLeSel.com", "default.com")).isEqualTo("passmoilesel.com");
    }

    @Test
    public void sanitizes_normalizes_and_truncates_uri_subparts() {
        assertThat(fqdnHelper.sanitizeAndTruncatePart("jeeprobe front-end", 7)).isEqualTo("jeeprob");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("jeeprobe", 8)).isEqualTo("jeeprobe");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("a nice one", 12)).isEqualTo("aniceone");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("3mois.com", 12)).isEqualTo("3moiscom");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("3mois\uFF0Ecom", 12)).isEqualTo("3moiscom");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("a-b_c d", 12)).isEqualTo("ab_cd");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-------it-removes-right-part", 19)).isEqualTo("abcdefghijklmnopqrs");
        assertThat(fqdnHelper.sanitizeAndTruncatePart("a very long env name                         will get truncated on right     ", 19)).isEqualTo("averylongenvnamewil");

        //Normalize case
        assertThat(fqdnHelper.sanitizeAndTruncatePart("UPPERcase", 12)).isEqualTo("uppercase");
    }

    @Test
    public void guava_idn_class_does_not_convert_idns() {
        String chineseIdnVirtualHost = "\u8a00\u8a9e.net";
        assertThat(InternetDomainName.from(chineseIdnVirtualHost).name()).isEqualTo(chineseIdnVirtualHost);

    }

    @Test
    public void jdk_idn_supports_to_ascii_conversion() {
        //when the paas register the uri with the router, it should use IDN toAscii encoding
        String frenchIdnVirtualHost = "t\u00eatu.elpaaso.org";
        String chineseIdnVirtualHost = "\u8a00\u8a9e.net";
        String chinese2IdnVirtualHost= "host.\u6e2c\u8a66";

        assertThat(IDN.toASCII(frenchIdnVirtualHost)).isEqualTo("xn--ttu-fma.elpaaso.org");
        assertThat(IDN.toASCII(chineseIdnVirtualHost)).isEqualTo("xn--zz2a4l.net");
        assertThat(IDN.toASCII(chinese2IdnVirtualHost)).isEqualTo("host.xn--g6w251d");
    }

    @Test
    public void fqdnhelper_supports_idn_in_FQDN_without_encoding_them() {
        //given a user choosing an IDN as a webGui virtual host
        String frenchIdnVirtualHost = "t\u00eatu.elpaaso.org";
        String chinesePart= "host.\u6e2c\u8a66";

        //when the paas manipulates them it should not try to normalize them
        //when the paas displays the uri on the webpage, it should display unicode characters.
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost(frenchIdnVirtualHost, "default.com")).isEqualTo(frenchIdnVirtualHost);
        assertThat(fqdnHelper.truncateUnsupportedCharsToValidHost(chinesePart, "default.com")).isEqualTo(chinesePart);
    }

   @Test
    public void fqdnhelper_supports_idn_in_subparts_without_encoding_them() {
        //given a user choosing an IDN as a webGui virtual host
        String frenchIdnVirtualHost = "t\u00eatu";
        String chineseIdnVirtualHost = "\u8a00\u8a9e";

        //when the paas manipulates them it should not try to normalize them
        //when the paas displays the uri on the webpage, it should display unicode characters.
        assertThat(fqdnHelper.sanitizeAndTruncatePart(frenchIdnVirtualHost, 128)).isEqualTo(frenchIdnVirtualHost);
        assertThat(fqdnHelper.sanitizeAndTruncatePart(chineseIdnVirtualHost, 128)).isEqualTo(chineseIdnVirtualHost);
    }

}