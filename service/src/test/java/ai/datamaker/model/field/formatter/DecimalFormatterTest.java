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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.DecimalFormatter;
import org.junit.jupiter.api.Test;

class DecimalFormatterTest {

    @Test
    void format() {
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(DecimalFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "#,###.00");

        DecimalFormatter formatter = new DecimalFormatter();

        assertEquals("123,456.75", formatter.format(new BigDecimal("123456.75"), fieldConfig));
        assertEquals("123,456.00", formatter.format(new BigDecimal("123456.00"), fieldConfig));
        assertEquals("123,456,123,456.78", formatter.format(new BigDecimal("123456123456.78"), fieldConfig));
    }
}