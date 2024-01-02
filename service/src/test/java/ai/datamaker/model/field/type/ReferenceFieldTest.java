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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.CustomField;
import ai.datamaker.model.field.type.ReferenceField;
import ai.datamaker.model.field.type.StringField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ReferenceFieldTest {

    @Test
    void generateData() {
        ReferenceField field = new ReferenceField();

        Dataset dataset = new Dataset();
        dataset.setNumberOfRecords(5L);
        CustomField pdoNumber = new CustomField("pdo", Locale.getDefault());
        pdoNumber.setPattern("###########");
        pdoNumber.setIsPrimaryKey(true);

        dataset.addField(pdoNumber);
        dataset.processAllValues((v) -> {});

        field.setReference(pdoNumber);

        //Object value = field.generateData();

        //assertNotNull(value);
        Iterator<Object> iterator = dataset.getPrimaryKeyValues().values().iterator();
        for (int i=0; i<5; i++) {
            assertEquals(iterator.next(), field.generateData(), "current: " + i);
        }
    }

    @Test
    void generateData_noPrimaryValues() {
        ReferenceField field = new ReferenceField();

        Dataset dataset = new Dataset();
        StringField stringField = new StringField();
        dataset.addField(stringField);

        field.setReference(stringField);

        Assertions.assertThrows(IllegalStateException.class, field::generateData);
    }

    void test_referenceIsNotPrimary_throwException() {
        fail("should implement");
    }

    void test_referenceIsNotInDataset_throwException() {
        fail("should implement");
    }

    void test_currentCountDoesNotMatch_throwException() {
        fail("should implement");
    }
}