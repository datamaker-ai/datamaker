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
import ai.datamaker.model.field.FieldValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FieldValueSerializer extends StdSerializer<List<FieldValue>> {

    public FieldValueSerializer() {
        this(null);
    }

    public FieldValueSerializer(Class<List<FieldValue>> t) {
        super(t);
    }

    @Override
    public void serialize(List<FieldValue> fieldValues, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        fieldValues.forEach(fv -> {
            try {

                // If complex type, drill down
//                if (f.getObjectType() == ComplexField.class) {
//
//                }

                jsonGenerator.writeObjectField(fv.getField().getName(), fv.getValue());

            } catch (Exception e) {
                log.warn("Error while serializing json", e);
                throw new DatasetSerializationException(e.getMessage(), e, fv.getField().getDataset());
            }
        });

        jsonGenerator.writeEndObject();
    }

}