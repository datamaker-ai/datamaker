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

import java.util.Locale;

import ai.datamaker.model.field.formatter.NumberFormatter;
import ai.datamaker.model.field.type.LongField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongFieldTest {

    @Test
    void generateData() {
        LongField field = new LongField("number", Locale.ENGLISH);

        assertNotNull(field.generateData());
    }

    @Test
    void generate_IdentificationNumber() {
        LongField longField = new LongField("pdo", Locale.CANADA_FRENCH);

        longField.getConfig().put(LongField.MIN_VALUE_PROPERTY, 10000000L);
        longField.getConfig().put(LongField.MAX_VALUE_PROPERTY, 999999999L);
        longField.getConfig().put(NumberFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "00000000000");

        NumberFormatter<Long> longNumberFormatter = new NumberFormatter<>();

        longField.setFormatter(longNumberFormatter);

        Object value1 = longField.getData();

        assertTrue(value1.toString().matches("00\\d{9}"));
    }
}