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

package ai.datamaker.model.field.formatter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.CamelCaseFormatter;
import org.junit.jupiter.api.Test;

class CamelCaseFormatterTest {

    @Test
    void format() {
        FieldConfig fieldConfig = new FieldConfig();

        CamelCaseFormatter formatter = new CamelCaseFormatter();
        fieldConfig.put(CamelCaseFormatter.LOCALE_PROPERTY.getKey(), Locale.ENGLISH.toLanguageTag());

        String value = (String) formatter.format("Foo_ bAR  done_that", fieldConfig);
        assertEquals("fooBarDoneThat", value);
    }
}