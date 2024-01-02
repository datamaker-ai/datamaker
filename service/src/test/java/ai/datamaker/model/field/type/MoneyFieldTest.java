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

import ai.datamaker.model.field.type.MoneyField;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyFieldTest {

    @Test
    void generateData() {
        MoneyField field = new MoneyField("usd", Locale.US);

        String value = field.generateData();

        System.out.println(value);
        assertNotNull(value);
        assertTrue(value.matches("\\$\\d{0,3},?\\d{1,3}\\.\\d{2}"));
    }

    @Test
    void generateData_Ranges() {
        MoneyField field = new MoneyField("cad", Locale.CANADA);
        field.getConfig().put(MoneyField.MIN_VALUE_PROPERTY, 100.00d);
        field.getConfig().put(MoneyField.MAX_VALUE_PROPERTY, 999.99d);

        String value = field.generateData();

        assertNotNull(value);
        assertTrue(value.matches("\\$\\d{3}\\.\\d{2}"));
    }

    @Test
    void generateData_Ranges_CAD() {
        MoneyField field = new MoneyField("jap", Locale.JAPAN);
        field.getConfig().put(MoneyField.MIN_VALUE_PROPERTY, 100.00d);
        field.getConfig().put(MoneyField.MAX_VALUE_PROPERTY, 999.99d);

        String value = field.generateData();

        assertNotNull(value);
        assertTrue(value.matches("￥\\d{3}"));
    }
}