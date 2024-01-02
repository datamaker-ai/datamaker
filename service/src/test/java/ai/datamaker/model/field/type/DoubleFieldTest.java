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
import ai.datamaker.model.field.type.DoubleField;
import org.junit.jupiter.api.Test;

class DoubleFieldTest {

    @Test
    void generateData() {
        DoubleField field = new DoubleField();

        assertNotNull(field.generateData());
    }

    @Test
    void generateData_decimalFormatter() {
        DoubleField field = new DoubleField();
        field.getConfig().put(NumberFormatter.NUMBER_PATTERN_PROPERTY.getKey(), "###########.0000000");

        NumberFormatter<Double> df = new NumberFormatter<>();
        field.setFormatter(df);

        String value = (String) field.getData();

        assertNotNull(value);
    }
}