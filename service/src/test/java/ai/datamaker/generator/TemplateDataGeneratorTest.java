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

import ai.datamaker.generator.TemplateDataGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.StringField;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class TemplateDataGeneratorTest {

    private TemplateDataGenerator templateDataGenerator = new TemplateDataGenerator();

    @Test
    void generate() throws Exception {
        Dataset dataset = new Dataset("test", Locale.ENGLISH);
        dataset.setNumberOfRecords(10L);
        dataset.getFields().add(new StringField("text", Locale.ENGLISH));
        dataset.getFields().add(new AgeField("age", Locale.ENGLISH));
        dataset.getFields().add(new FloatField("balance", Locale.ENGLISH));
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        templateDataGenerator.generate(dataset, System.out);
    }

    @Test
    void getDataType() {
    }

    @Test
    void getConfigProperties() {
    }
}