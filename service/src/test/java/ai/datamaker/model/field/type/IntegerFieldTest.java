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

import ai.datamaker.model.field.constraint.RangeConstraint;
import ai.datamaker.model.field.type.IntegerField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegerFieldTest {

    @Test
    void generateData() {
        IntegerField field = new IntegerField();

        assertNotNull(field.generateData());
    }

    @Test
    void generateData_negative() {
        IntegerField field = new IntegerField();
        field.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, Integer.MIN_VALUE);
        field.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 0);
        System.out.println(field.generateData());
        assertTrue(field.generateData() <= -1);
    }

    @Test
    void generateDate_Byte() {
        RangeConstraint<Integer> rangeConstraint = new RangeConstraint<>(0, 127);
        IntegerField byteField = new IntegerField();
        byteField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 0);
        byteField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 100);

        Integer byteValue = byteField.generateData();
        assertTrue(byteValue >= Byte.MIN_VALUE && byteValue <= Byte.MAX_VALUE, byteValue.toString());
    }
}