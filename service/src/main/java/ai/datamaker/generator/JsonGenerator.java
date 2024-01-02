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

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.utils.json.DatasetSerializer;
import ai.datamaker.utils.json.FieldSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Evaluate https://github.com/vincentrussell/json-data-generator
 */
public class JsonGenerator implements DataGenerator {

    private final ObjectMapper mapper;

    static final PropertyConfig JSON_END_OF_LINE =
            new PropertyConfig("json.generator.line.ending",
                               "Line ending",
                               PropertyConfig.ValueType.STRING,
                               "\n",
                               Collections.emptyList());

    public JsonGenerator() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Dataset.class, new DatasetSerializer());
        module.addSerializer(Field.class, new FieldSerializer());
        mapper.registerModule(module);
        mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        // TODO should we flush or not
        mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);

        this.mapper = mapper;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        List<PropertyConfig> properties = Lists.newArrayList();
        properties.add(JSON_END_OF_LINE);
        return properties;
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        String endOfLine = (String) config.getConfigProperty(JSON_END_OF_LINE);

        //TODO write json as multiple lines (like in HDFS) or array

        if (!dataset.getExportHeader() && dataset.getFields().size() == 1) {
            dataset.processAllValues(fieldValues -> {
                writeJson(dataset, outputStream, endOfLine, fieldValues.get(0).getValue());
            });
        } else {
            dataset.processAllValues(fieldValues -> {
                LinkedHashMap<String, Object> preserverOrderValues = new LinkedHashMap<>();
                fieldValues.forEach(fv -> preserverOrderValues.put(fv.getField().getName(), fv.getValue()));
                writeJson(dataset, outputStream, endOfLine, preserverOrderValues);
            });
        }

        // TODO if dataset contains only one ArrayField or only primitive and header is set to false, output as array
        // if (!dataset.getExportHeader() && (dataset.getFields().contains(new ArrayField()) || (!dataset.getFields().contains(new ComplexField()) && !dataset.getFields().contains(new ArrayField()))
    }

    private void writeJson(Dataset dataset, OutputStream outputStream, String endOfLine, Object value) {
        try {
            mapper.writeValue(outputStream, value);
            outputStream.write(endOfLine.getBytes());
            if (dataset.getFlushOnEveryRecord()) {
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new DatasetSerializationException("Errors during JSON serialization", e, dataset);
        }
    }

    @Override
    public FormatType getDataType() {
        return FormatType.JSON;
    }

}
