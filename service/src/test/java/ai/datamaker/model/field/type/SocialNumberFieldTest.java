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

package ai.datamaker.model.field.type;

import ai.datamaker.model.field.type.SocialNumberField;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SocialNumberFieldTest {

    @Test
    void generateData() {
        SocialNumberField socialNumberField = new SocialNumberField();
        socialNumberField.setLocale(Locale.CANADA_FRENCH);
        socialNumberField.getConfig().put(SocialNumberField.SIN_REGION_PROPERTY, 5);
        String sin = socialNumberField.generateData();
        System.out.println(sin);
        assertTrue(LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(sin), "luhn check failed");
    }

    @Test
    void generateData_Canada() {
        SocialNumberField socialNumberField = new SocialNumberField();
    }

    @Test
    void getConfigProperties() {
    }
}