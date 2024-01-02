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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.model.field.type.SequenceField;
import org.junit.jupiter.api.Test;

class SequenceFieldTest {

    @Test
    void generateData() {
        SequenceField sequenceField = new SequenceField();
        assertTrue(sequenceField.getIsPrimaryKey());

        assertEquals(1, sequenceField.generateData());
    }

    @Test
    void generateData_initialValue() {
        SequenceField sequenceField = new SequenceField();
        sequenceField.setInitialValue(7987L);
        assertTrue(sequenceField.getIsPrimaryKey());

        assertEquals(7988L, sequenceField.generateData());
    }

    @Test
    void generateData_delta() {
        SequenceField sequenceField = new SequenceField();
        sequenceField.setDelta(25L);

        assertTrue(sequenceField.getIsPrimaryKey());
        assertEquals(25, sequenceField.generateData());
        assertEquals(50, sequenceField.generateData());
    }
}