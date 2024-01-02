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

import ai.datamaker.generator.JsonGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.EmptyField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.processor.JsonProcessor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
public class JsonProcessorIntegrationTest {

    @Autowired
    private JsonProcessor datasetProcessor;

    private final JsonGenerator jsonGenerator = new JsonGenerator();

    @Test
    void process() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("servlet.json");

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);

        assertTrue(datasetOptional.isPresent());

        assertThat(((ComplexField)((ComplexField)((ArrayField)datasetOptional.get().getFields().get(0)).getReference()).getReferences().toArray()[2]).getReferences())
                .hasSize(29)
                .extracting("name")
                .containsExactly("configGlossary:installationAt",
                                 "configGlossary:adminEmail",
                                 "configGlossary:poweredBy",
                                 "configGlossary:staticPath",
                                 "templateOverridePath",
                                 "defaultListTemplate",
                                 "defaultFileTemplate",
                                 "useJSP",
                                 "jspListTemplate",
                                 "jspFileTemplate",
                                 "cachePackageTagsTrack",
                                 "cachePackageTagsStore",
                                 "cachePackageTagsRefresh",
                                 "cacheTemplatesTrack",
                                 "cacheTemplatesStore",
                                 "cacheTemplatesRefresh",
                                 "cachePagesTrack",
                                 "cachePagesStore",
                                 "cachePagesRefresh",
                                 "cachePagesDirtyRead",
                                 "searchEngineListTemplate",
                                 "searchEngineFileTemplate",
                                 "searchEngineRobotsDb",
                                 "useDataStore",
                                 "dataStoreClass",
                                 "dataStoreMaxConns",
                                 "dataStoreConnUsageLimit",
                                 "dataStoreLogLevel",
                                 "maxUrlLength");

        assertThat(((ComplexField)((ComplexField)((ArrayField)datasetOptional.get().getFields().get(0)).getReference()).getReferences().toArray()[2]).getReferences())
                .hasSize(29)
                .extractingResultOf("getClass")
                .containsExactly(TextField.class,
                                 TextField.class,
                                 TextField.class,
                                 TextField.class,
                                 EmptyField.class,
                                 TextField.class,
                                 TextField.class,
                                 BooleanField.class,
                                 TextField.class,
                                 TextField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 TextField.class,
                                 TextField.class,
                                 TextField.class,
                                 BooleanField.class,
                                 TextField.class,
                                 IntegerField.class,
                                 IntegerField.class,
                                 TextField.class,
                                 IntegerField.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        datasetOptional.ifPresent(d -> {
            List<Field> nestedFields = Lists.newArrayList();
            d.getFields().forEach(f -> processField(d, f, nestedFields));
            d.getFields().addAll(nestedFields);
        });
        jsonGenerator.generate(datasetOptional.get(), baos);
        System.out.println(baos.toString());
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
}
