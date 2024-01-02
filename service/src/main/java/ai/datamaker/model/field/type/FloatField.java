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

import ai.datamaker.model.Constants;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Floating point", localizationKey = "field.group.primitive.float", group = FieldGroup.PRIMITIVE)
public class FloatField extends Field<Float> {

    static final PropertyConfig MIN_VALUE_PROPERTY =
        new PropertyConfig(Constants.MIN_VALUE_PROPERTY,
            "Minimum value",
            ValueType.NUMERIC,
            0L,
            Lists.newArrayList(0, Float.MIN_VALUE));

    static final PropertyConfig MAX_VALUE_PROPERTY =
        new PropertyConfig(Constants.MAX_VALUE_PROPERTY,
            "Maximum value",
            ValueType.NUMERIC,
            (long) Short.MAX_VALUE,
            Lists.newArrayList(0, Float.MAX_VALUE));

    static final PropertyConfig MAX_NUMBER_DECIMALS_PROPERTY =
        new PropertyConfig(Constants.NUMBER_DECIMALS_PROPERTY,
            "Maximum number of decimals",
            ValueType.NUMERIC,
            7,
            Lists.newArrayList(0, 7));

    public FloatField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    public Float generateData() {
        // This is possible to do because a float value can hold only a maximum of 7 digits after the decimal,
        // while a double value in Java can hold a maximum of 16 digits after the decimal.
        int maxNumberOfDecimals = (int) config.getConfigProperty(MAX_NUMBER_DECIMALS_PROPERTY);

        // FIXME should be float instead
        long minValue = (long) config.getConfigProperty(MIN_VALUE_PROPERTY);
        long maxValue = (long) config.getConfigProperty(MAX_VALUE_PROPERTY);

        double value = faker.number().randomDouble(maxNumberOfDecimals, minValue, maxValue);
        return (float) value;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            MIN_VALUE_PROPERTY,
            MAX_VALUE_PROPERTY,
            MAX_NUMBER_DECIMALS_PROPERTY
        );
    }

    public void setMaxNumberOfDecimals(int numberDecimals) {
        config.put(MAX_NUMBER_DECIMALS_PROPERTY, numberDecimals);
    }
}
