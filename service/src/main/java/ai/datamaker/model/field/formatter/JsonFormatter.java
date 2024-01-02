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

package ai.datamaker.model.field.formatter;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.FieldConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Transient;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.List;

@Data
public class JsonFormatter implements FieldFormatter<Object> {

    @Transient
    @JsonIgnore
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    @Override
    public Object format(Object value, FieldConfig config) {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList();
    }
}
