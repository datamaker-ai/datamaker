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

package ai.datamaker.model;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldValue;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.ConstantField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.ReferenceField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.field.type.StringField;
import com.google.common.collect.Lists;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class DatasetTest {

    @Test
    void getAllValues() {
        Dataset dataset = new Dataset();
        dataset.setNumberOfRecords(10l);
        SequenceField sequenceField = new SequenceField("id", Locale.getDefault());
        dataset.addField(sequenceField);
        dataset.addField(new AddressField("address", Locale.getDefault()));
        dataset.addField(new StringField("test", Locale.getDefault()));
        dataset.addField(new FloatField("number", Locale.getDefault()));

        List<List<?>> values = Lists.newArrayList();
        dataset.processAllValues(values::add);
        assertThat(values).hasSize(10);
        assertThat(dataset.getPrimaryKeyValues().keys()).contains(sequenceField);
    }

    @Test
    void getAllValues_forceRetry_error() {
        Dataset dataset = new Dataset();
        dataset.setAllowDuplicates(false);
        dataset.setNumberOfRecords(10l);
        ConstantField constant = new ConstantField("number", Locale.getDefault());
        constant.setValue(42);
        dataset.addField(constant);

        Assertions.assertThrows(IllegalStateException.class, () -> dataset.processAllValues((objects) -> {}));
    }

    @Test
    void getAllValues_withDuplicates_beyondThreshold() {
        Dataset dataset = new Dataset();
        dataset.setAllowDuplicates(true);
        dataset.setDuplicatesPercentLimit(0.5f);
        dataset.setNumberOfRecords(10l);
        Field mockField = Mockito.mock(Field.class);
        when(mockField.getData()).thenReturn(1, 1, 2, 2, 2, 2, 2, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        dataset.addField(mockField);

        List<Object> values = Lists.newArrayList();

        dataset.processAllValues(objects -> values.addAll(objects.stream()
                              .map(FieldValue::getValue)
                              .collect(Collectors.toList())));
        assertThat(values).hasSize(10).contains(1, 1, 2, 2, 2, 2, 2, 7, 8, 9);

    }

    @Test
    void getAllValues_withDuplicates() {
        Dataset dataset = new Dataset();
        dataset.setAllowDuplicates(true);
        dataset.setDuplicatesPercentLimit(0.1f);
        dataset.setNumberOfRecords(10l);
        Field mockField = Mockito.mock(Field.class);
        when(mockField.getData()).thenReturn(1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        dataset.addField(mockField);

        List<Object> values = Lists.newArrayList();

        dataset.processAllValues(objects -> values.addAll(objects.stream()
                .map(FieldValue::getValue)
                .collect(Collectors.toList())));
        assertThat(values).hasSize(10).contains(1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    void testReference() {
        Dataset datasetReference = new Dataset();
        datasetReference.setThreadPoolSize(1);
        datasetReference.setNumberOfRecords(10l);
        IntegerField idField = new IntegerField("id", Locale.getDefault());
        idField.setIsPrimaryKey(true);
        datasetReference.addField(idField);

        System.out.println("Reference");
        datasetReference.processAllValues((fv) -> fv.forEach(v -> System.out.println(v.getValue())));

        Dataset otherDataset = new Dataset();
        otherDataset.setThreadPoolSize(1);
        otherDataset.setNumberOfRecords(10l);
        ReferenceField referenceField = new ReferenceField("reference", Locale.getDefault());
        referenceField.setReference(idField);
        otherDataset.addField(referenceField);

        System.out.println("\nOther");
        otherDataset.processAllValues((fv) -> fv.forEach(v -> System.out.println(v.getValue())));
    }

    @Test
    void testReference_noData() {
        Dataset datasetReference = new Dataset();
        datasetReference.setThreadPoolSize(1);
        datasetReference.setNumberOfRecords(10l);
        IntegerField idField = new IntegerField("id", Locale.getDefault());
        idField.setIsPrimaryKey(true);
        datasetReference.addField(idField);

        Dataset otherDataset = new Dataset();
        otherDataset.setThreadPoolSize(1);
        otherDataset.setNumberOfRecords(10l);
        ReferenceField referenceField = new ReferenceField("reference", Locale.getDefault());
        referenceField.setReference(idField);
        otherDataset.addField(referenceField);

        assertFalse("no data", datasetReference.getPrimaryKeyValues().containsKey(idField));

        Assertions.assertThrows(IllegalStateException.class, () -> {
            otherDataset.processAllValues((fv) -> fv.forEach(v -> System.out.println(v.getValue())));
        });
    }

    @Test
    void testPositions() {
        Dataset dataset = new Dataset();
        SequenceField sequenceField = new SequenceField("id", Locale.getDefault());
        dataset.addField(sequenceField);
        dataset.addField(new AddressField("address", Locale.getDefault()));
        Field strField = new StringField("test", Locale.getDefault());
        dataset.addField(strField);
        dataset.addField(new FloatField("number", Locale.getDefault()));

        assertThat(dataset.getFields())
            .extracting("name", "position")
            .contains(
                Tuple.tuple("id", 1),
                Tuple.tuple("address", 2),
                Tuple.tuple("test", 3),
                Tuple.tuple("number", 4));

        dataset.removeField(strField);

        assertThat(dataset.getFields())
            .extracting("name", "position")
            .contains(
                Tuple.tuple("id", 1),
                Tuple.tuple("address", 2),
                Tuple.tuple("number", 3));
    }
}