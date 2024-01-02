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

package ai.datamaker.utils.json;

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.ComplexField;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

@Slf4j
public class DatasetSerializer extends StdSerializer<Dataset> {

    public DatasetSerializer() {
        this(null);
    }

    public DatasetSerializer(Class<Dataset> t) {
        super(t);
    }

//    @Override
//    public void serialize(
//            AbstractReadWriteAccess.Item value, JsonGenerator jgen, SerializerProvider provider)
//      throws IOException, JsonProcessingException {
//
//        jgen.writeStartObject();
//        jgen.writeNumberField("id", value.id);
//        jgen.writeStringField("itemName", value.itemName);
//        jgen.writeNumberField("owner", value.owner.id);
//        jgen.writeEndObject();
//    }

    @Override
    public void serialize(Dataset dataset, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        dataset.getFields().forEach(f -> {
            try {

                // If complex type, drill down
                if (f.getObjectType() == ComplexField.class) {

                }

                jsonGenerator.writeObjectField(f.getName(), f.getData());

                // jsonGenerator.write(f.getName(), convertToBytes(f.getData()));
                // jsonGenerator.writeStringField(f.getName(), f.getData().toString());

            } catch (Exception e) {
               log.warn("Error while serializing json", e);
               throw new DatasetSerializationException(e.getMessage(), e, dataset);
            }
        });

        jsonGenerator.writeEndObject();
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }
}