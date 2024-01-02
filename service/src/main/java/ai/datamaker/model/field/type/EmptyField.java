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
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Empty value field", localizationKey = "field.group.empty", group = FieldGroup.CUSTOM)
public class EmptyField extends Field<String> {

    static final PropertyConfig EMPTY_VALUE_PROPERTY =
        new PropertyConfig("field.empty.value",
            "Empty value",
            ValueType.STRING,
            "",
            Collections.emptyList());

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(EMPTY_VALUE_PROPERTY);
    }

    public EmptyField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected String generateData() {
        return (String) config.getConfigProperty(EMPTY_VALUE_PROPERTY);
    }

    public void setEmptyValue(String value) {
        config.put(EMPTY_VALUE_PROPERTY, value);
    }
}
