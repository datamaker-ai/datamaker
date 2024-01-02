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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.annotations.Indexed;

@Entity
@Getter
@Setter
@Indexed
@NoArgsConstructor
@FieldType(description = "Password", localizationKey = "field.group.password", group = FieldGroup.NETWORK)
public class PasswordField extends Field<String> {

    static final PropertyConfig MINIMUM_LENGTH_PROPERTY =
        new PropertyConfig("field.password.min.length",
            "Minimum password length",
            ValueType.NUMERIC,
            8,
            Lists.newArrayList(0, 10000));

    static final PropertyConfig MAXIMUM_LENGTH_PROPERTY =
        new PropertyConfig("field.password.max.length",
            "Maximum password length",
            ValueType.NUMERIC,
            16,
            Lists.newArrayList(0, 10000));

    static final PropertyConfig INCLUDE_UPPERCASE_PROPERTY =
        new PropertyConfig("field.password.include.uppercase",
            "Include upper case",
            ValueType.BOOLEAN,
            false,
            Collections.emptyList());

    static final PropertyConfig INCLUDE_SPECIAL_CHARACTER_PROPERTY =
        new PropertyConfig("field.password.include.special.chars",
            "Include special characters",
            ValueType.BOOLEAN,
            false,
            Collections.emptyList());

    static final PropertyConfig INCLUDE_DIGITS_PROPERTY =
        new PropertyConfig("field.password.include.digits",
            "Include digits",
            ValueType.BOOLEAN,
            true,
            Collections.emptyList());

    public PasswordField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected String generateData() {
        int minimumLength = (int) config.getConfigProperty(MINIMUM_LENGTH_PROPERTY);
        int maximumLength = (int) config.getConfigProperty(MAXIMUM_LENGTH_PROPERTY);

        boolean includeDigit = (boolean) config.getConfigProperty(INCLUDE_DIGITS_PROPERTY);
        boolean includeUppercase = (boolean) config.getConfigProperty(INCLUDE_UPPERCASE_PROPERTY);
        boolean includeSpecial = (boolean) config.getConfigProperty(INCLUDE_SPECIAL_CHARACTER_PROPERTY);

        return faker.internet().password(minimumLength,
                                         maximumLength,
                                         includeUppercase,
                                         includeSpecial,
                                         includeDigit);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            MINIMUM_LENGTH_PROPERTY,
            MAXIMUM_LENGTH_PROPERTY,
            INCLUDE_DIGITS_PROPERTY,
            INCLUDE_SPECIAL_CHARACTER_PROPERTY,
            INCLUDE_UPPERCASE_PROPERTY
        );
    }

    public void setIncludeDigit(boolean includeDigit) {
        config.put(INCLUDE_DIGITS_PROPERTY, includeDigit);
    }

    public void setIncludeUppercase(boolean includeUppercase) {
        config.put(INCLUDE_UPPERCASE_PROPERTY, includeUppercase);
    }

    public void setIncludeSpecial(boolean includeSpecial) {
        config.put(INCLUDE_SPECIAL_CHARACTER_PROPERTY, includeSpecial);
    }

    public void setMinimumLength(int minimumLength) {
        config.put(MINIMUM_LENGTH_PROPERTY, minimumLength);
    }

    public void setMaximumLength(int maximumLength) {
        config.put(MAXIMUM_LENGTH_PROPERTY, maximumLength);
    }
}
