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
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Number", localizationKey = "field.group.primitive.integer", group = FieldGroup.PRIMITIVE)
public class IntegerField extends Field<Integer> {

    public static final PropertyConfig MIN_VALUE_PROPERTY =
        new PropertyConfig(Constants.MIN_VALUE_PROPERTY,
            "Minimum value",
            ValueType.NUMERIC,
            0,
            Lists.newArrayList(Integer.MIN_VALUE, 0));

    public static final PropertyConfig MAX_VALUE_PROPERTY =
        new PropertyConfig(Constants.MAX_VALUE_PROPERTY,
            "Maximum value",
            ValueType.NUMERIC,
            100000,
            Lists.newArrayList(0, Integer.MAX_VALUE));

    public IntegerField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Integer generateData() {

        int minValue = (int) config.getConfigProperty(MIN_VALUE_PROPERTY);
        int maxValue = (int) config.getConfigProperty(MAX_VALUE_PROPERTY);

        return ThreadLocalRandom.current().nextInt(minValue, maxValue);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            MIN_VALUE_PROPERTY,
            MAX_VALUE_PROPERTY
        );
    }
}
