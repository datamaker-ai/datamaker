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

import ai.datamaker.model.field.type.BigIntegerField;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BigIntegerFieldTest {

    @Test
    void generateData() {
        BigIntegerField field = new BigIntegerField();

        BigInteger value = field.generateData();

        assertNotNull(value);
        System.out.println(value);
    }

    @Test
    void generateData_bitLength() {
        BigIntegerField field = new BigIntegerField();
        field.setBitLength(128);

        BigInteger value = field.generateData();

        assertNotNull(value);
        System.out.println(value);
    }

    @Test
    void generateData_upperLimit() {
        BigIntegerField field = new BigIntegerField();
        field.setBitLength(128);

        field.getConfig().put(BigIntegerField.MIN_VALUE_PROPERTY, BigIntegerField.MAX_VALUE.toString());
        field.getConfig().put(BigIntegerField.MAX_VALUE_PROPERTY, new BigInteger("999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999").toString());

        BigInteger value = field.generateData();

        assertNotNull(value);
        System.out.println(value);
    }
}