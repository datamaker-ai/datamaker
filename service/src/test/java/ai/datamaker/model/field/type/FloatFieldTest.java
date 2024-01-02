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

import static org.junit.Assert.assertNotNull;

import ai.datamaker.model.field.formatter.NumberFormatter;
import ai.datamaker.model.field.type.FloatField;
import org.junit.jupiter.api.Test;

class FloatFieldTest {

    @Test
    void generateData() {
        FloatField field = new FloatField();
        field.setMaxNumberOfDecimals(3);

        field.getConfig().put(FloatField.MIN_VALUE_PROPERTY, 0L);
        field.getConfig().put(FloatField.MAX_VALUE_PROPERTY, 100L);

        Float value = field.generateData();

        assertNotNull(value);
    }

    @Test
    void generateData_decimalFormatter() {
        FloatField field = new FloatField();
        field.setMaxNumberOfDecimals(3);
        field.getConfig().put(NumberFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "###########.000");

        NumberFormatter<Float> df = new NumberFormatter<>();
        field.setFormatter(df);

        String value = (String) field.getData();

        assertNotNull(value);
    }
}