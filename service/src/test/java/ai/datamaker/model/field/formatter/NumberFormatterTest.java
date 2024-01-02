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

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.NumberFormatter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class NumberFormatterTest {

    @Test
    void format_integer() {
        NumberFormatter<Integer> formatter = new NumberFormatter<>();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(NumberFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "#00000");

        assertEquals("00034", formatter.format(34, fieldConfig));
    }

    @Test
    void format_decimal() {

        NumberFormatter<BigDecimal> formatter = new NumberFormatter<>();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(NumberFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "#,###.00");

        assertEquals("123,456.75", formatter.format(new BigDecimal("123456.75"), fieldConfig));
        assertEquals("123,456.00", formatter.format(new BigDecimal("123456.00"), fieldConfig));
        assertEquals("123,456,123,456.78", formatter.format(new BigDecimal("123456123456.78"), fieldConfig));
    }
}