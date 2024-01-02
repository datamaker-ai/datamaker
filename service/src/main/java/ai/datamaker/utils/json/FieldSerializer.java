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

import ai.datamaker.model.field.Field;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class FieldSerializer extends StdSerializer<Field> {

    public FieldSerializer() {
        this(null);
    }

    public FieldSerializer(Class<Field> t) {
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
    public void serialize(Field field, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }
}