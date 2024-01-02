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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Number", localizationKey = "field.group.primitive.long", group = FieldGroup.PRIMITIVE)
public class LongField extends Field<Long> {

    static final PropertyConfig MIN_VALUE_PROPERTY =
        new PropertyConfig(Constants.MIN_VALUE_PROPERTY,
            "Minimum value",
            ValueType.NUMERIC,
            0L,
            Lists.newArrayList(Integer.MIN_VALUE, 0));

    static final PropertyConfig MAX_VALUE_PROPERTY =
        new PropertyConfig(Constants.MAX_VALUE_PROPERTY,
            "Maximum value",
            ValueType.NUMERIC,
            1000L,
            Lists.newArrayList(0, Integer.MAX_VALUE));

    static final PropertyConfig NEGATIVE_VALUES_PROPERTY =
        new PropertyConfig(Constants.NEGATIVE_VALUE_PROPERTY,
            "Negative value",
            ValueType.BOOLEAN,
            false,
            Collections.emptyList());

    public LongField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected Long generateData() {

        long minValue = ((Number) config.getConfigProperty(MIN_VALUE_PROPERTY)).longValue();
        long maxValue = ((Number) config.getConfigProperty(MAX_VALUE_PROPERTY)).longValue();

        return ThreadLocalRandom.current().nextLong(minValue, maxValue);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            MIN_VALUE_PROPERTY,
            MAX_VALUE_PROPERTY,
            NEGATIVE_VALUES_PROPERTY
        );
    }
}
