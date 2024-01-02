/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

public class LocaleUtils {

    @Test
    void testConvert() {
        Locale fromString = Locale.forLanguageTag("fr-CA");

        Locale fromLocale = Locale.CANADA_FRENCH;

        assertEquals(fromString, fromLocale);
    }

    @Test
    void testLanguageTag() {
        Locale fromString = Locale.forLanguageTag("fr-CA");

        assertEquals(fromString.toLanguageTag(), "fr-CA");
    }

    @Test
    void testCreate() {
        Locale fromString = Locale.forLanguageTag("fr");

        System.out.println(fromString);
    }
}
