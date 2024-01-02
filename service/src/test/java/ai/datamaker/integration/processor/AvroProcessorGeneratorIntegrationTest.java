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

package ai.datamaker.integration.processor;

import ai.datamaker.generator.AvroGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.processor.AvroProcessor;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
//@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
public class AvroProcessorGeneratorIntegrationTest {

    @Autowired
    private AvroProcessor avroProcessor;

    @Test
    void convertFromAvroToAvroTest() throws Exception {

        InputStream input = getClass().getClassLoader().getResourceAsStream("ga.avro");

        Optional<Dataset> datasetOptional = avroProcessor.process(input);

        datasetOptional.ifPresent(d -> {
            List<Field> nestedFields = Lists.newArrayList();
            d.getFields().forEach(f -> processField(d, f, nestedFields));
            d.getFields().addAll(nestedFields);
        });
//        dataset.setFlushOnEveryRecord(true);
        Dataset dataset = datasetOptional.get();
        dataset.getFields().forEach(f -> {
            setDataset(f, dataset);
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AvroGenerator avroGenerator = new AvroGenerator();

        avroGenerator.generate(dataset, baos);

        baos.flush();
        baos.close();

        assertNotNull(baos.toString());
    }

    private void processField(Dataset dataset, Field field, List<Field> nestedFields) {
        if (field.getIsNested()) {
            field.setDataset(dataset);
            //dataset.addField(field);
            nestedFields.add(field);
        }
        if (field instanceof ComplexField) {
            ((ComplexField)field).getReferences().forEach(f -> processField(dataset, f, nestedFields));
        } else if (field instanceof ArrayField) {
            processField(dataset, ((ArrayField)field).getReference(), nestedFields);
        }
    }

    private void setDataset(Field f, Dataset d) {
        if (f instanceof ComplexField) {
            ComplexField c = (ComplexField) f;
            c.getReferences().forEach(cf -> setDataset(cf, d));
        }
        if (f instanceof ArrayField) {
            ArrayField a = (ArrayField) f;
            if (a.getReference() instanceof ComplexField) {
                setDataset(a.getReference(), d);
            }
        }
        f.setDataset(d);
    }
}
