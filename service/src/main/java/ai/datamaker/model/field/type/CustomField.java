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
 *  Returns a string with the '#' characters in the parameter replaced with random digits between 0-9 inclusive.
 *  Returns a string with the '?' characters in the parameter replaced with random alphabetic characters.
 *  Example: FLIGHT-???-#### will generate a value of FLIGHT-ZUK-8943
 */
@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Custom field ", localizationKey = "field.group.custom", group = FieldGroup.CUSTOM)
public class CustomField extends Field<String> {

    static final PropertyConfig PATTERN_PROPERTY =
            new PropertyConfig("field.custom.pattern",
                               "Pattern: '#' random digits between 0-9 inclusive, '?' random alphabetic characters. Example: FLIGHT-???-#### will generate a value of FLIGHT-ZUK-8943",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig UPPER_VALUE_PROPERTY =
            new PropertyConfig("field.custom.upper.value",
                               "Uppercase value",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Collections.emptyList());

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(PATTERN_PROPERTY, UPPER_VALUE_PROPERTY);
    }

    public CustomField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected String generateData() {
        return faker.bothify((String) config.getConfigProperty(PATTERN_PROPERTY),
                             (Boolean) config.getConfigProperty(UPPER_VALUE_PROPERTY));
    }

    public void setPattern(String pattern) {
        config.put(PATTERN_PROPERTY.getKey(), pattern);
    }

    public void setUpper(boolean upper) {
        config.put(UPPER_VALUE_PROPERTY.getKey(), upper);
    }
}
