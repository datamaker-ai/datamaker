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

package ai.datamaker.generator;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.field.type.StringField;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class GeneratorAbstractTest {

    protected Dataset getDataset(long records) {
        Dataset dataset = new Dataset();
        dataset.setNumberOfRecords(records);
        SequenceField sequenceField = new SequenceField("id", Locale.getDefault());
        dataset.addField(sequenceField);
        dataset.addField(new AddressField("address", Locale.getDefault()));
        dataset.addField(new StringField("test", Locale.getDefault()));
        dataset.addField(new FloatField("number", Locale.getDefault()));

        return dataset;
    }

}
