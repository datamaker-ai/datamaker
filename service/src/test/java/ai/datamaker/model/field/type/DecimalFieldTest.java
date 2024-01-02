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

import ai.datamaker.model.field.type.DecimalField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecimalFieldTest {

    @Test
    void generateData() {
        DecimalField field = new DecimalField("decimal", Locale.ENGLISH);

        BigDecimal value = field.generateData();

        assertNotNull(value);
    }

    @Test
    void generateData_ranges() {
        DecimalField field = new DecimalField("decimal", Locale.ENGLISH);
        field.getConfig().put(DecimalField.MIN_VALUE_PROPERTY, BigDecimal.ZERO.doubleValue());
        field.getConfig().put(DecimalField.MAX_VALUE_PROPERTY, BigDecimal.TEN.doubleValue());

        BigDecimal value = field.generateData();

        assertNotNull(value);
        assertTrue(value.compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(value.compareTo(BigDecimal.TEN) < 0);
    }
}