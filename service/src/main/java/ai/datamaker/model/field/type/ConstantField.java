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
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Use that field if the value is never expected to change.
 */
@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Value that never changes", localizationKey = "field.group.constant", group = FieldGroup.CUSTOM)
public class ConstantField extends Field<Object> {

    static final PropertyConfig VALUE_PROPERTY =
            new PropertyConfig("field.constant.value",
                               "Constant value",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig VALUE_TYPES_PROPERTY =
            new PropertyConfig("field.constant.value.type",
                               "Value type",
                               PropertyConfig.ValueType.STRING,
                               "STRING",
                               Lists.newArrayList("BOOLEAN", "DATE", "NUMERIC", "STRING"));


    public ConstantField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Object generateData() {
        return config.getConfigProperty(VALUE_PROPERTY);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(VALUE_PROPERTY, VALUE_TYPES_PROPERTY);
    }

    public void setValue(Object value) {
        config.put(VALUE_PROPERTY.getKey(), value);
    }
}
