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

package ai.datamaker.model.field.type;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.search.annotations.Indexed;

@Indexed
@Entity
@NoArgsConstructor
@FieldType(description = "Bytes", localizationKey = "field.group.primitive.bytes", group = FieldGroup.PRIMITIVE)
public class BytesField extends Field<byte[]> {

    static final PropertyConfig BYTES_LENGTH_PROPERTY =
            new PropertyConfig("field.bytes.length",
                               "Bytes length",
                               PropertyConfig.ValueType.NUMERIC,
                               25,
                               Collections.emptyList());

    public BytesField(String name, Locale locale) {
        super(name,
            locale);
    }

    @Override
    protected byte[] generateData() {
        int length = (int) config.getConfigProperty(BYTES_LENGTH_PROPERTY);

        return RandomStringUtils.random(length).getBytes();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(BYTES_LENGTH_PROPERTY);
    }
}
