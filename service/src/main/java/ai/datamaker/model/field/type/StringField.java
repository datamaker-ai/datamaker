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
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.search.annotations.Indexed;

@Indexed
@Entity
@NoArgsConstructor
@FieldType(description = "Random chain of characters", localizationKey = "field.group.primitive.string", group = FieldGroup.PRIMITIVE)
public class StringField extends Field<String> {

    public static final PropertyConfig LENGTH_PROPERTY =
        new PropertyConfig("field.text.length",
            "Length",
            ValueType.NUMERIC,
            25,
            Collections.emptyList());

    public static final PropertyConfig ASCII_ONLY_PROPERTY =
        new PropertyConfig("field.text.ascii.only",
            "Ascii only",
            ValueType.BOOLEAN,
            true,
            Collections.emptyList());

    public static final PropertyConfig ALPHA_NUMERIC_PROPERTY =
        new PropertyConfig("field.text.alpha.numeric",
            "Alpha numeric",
            ValueType.BOOLEAN,
            true,
            Collections.emptyList());

    public StringField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected String generateData() {

        int length = (int) config.getConfigProperty(LENGTH_PROPERTY);

        boolean alphaNumeric = (boolean) config.getConfigProperty(ASCII_ONLY_PROPERTY);

        //if (constraint instanceof LengthConstraint) {
        //    length = ((LengthConstraint) constraint).getLength().intValue();
        //}

        return alphaNumeric ? RandomStringUtils.randomAlphanumeric(length) : RandomStringUtils.random(length);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            LENGTH_PROPERTY,
            ALPHA_NUMERIC_PROPERTY,
            ASCII_ONLY_PROPERTY
        );
    }

    public void setLength(int maxLength) {
        config.put(LENGTH_PROPERTY.getKey(), maxLength);
    }

    public int getLength() {
        return (int) config.get(LENGTH_PROPERTY.getKey());
    }

    public void setAsciiOnly(boolean enabled) {
        config.put(ASCII_ONLY_PROPERTY, enabled);
    }

    public void setAlphaNumeric(boolean enabled) {
        config.put(ALPHA_NUMERIC_PROPERTY, enabled);
    }
}
