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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Age, Integer [1-100]", localizationKey = "field.group.age", group = FieldGroup.IDENTITY)
public class AgeField extends Field<Integer> {

    static final PropertyConfig MINIMUM_AGE_PROPERTY =
            new PropertyConfig("field.age.minAge",
                               "Mininum age",
                               PropertyConfig.ValueType.NUMERIC,
                               1,
                               Arrays.asList(1, 125));

    static final PropertyConfig MAXIMUM_AGE_PROPERTY =
            new PropertyConfig("field.age.maxAge",
                               "Maximum age",
                               PropertyConfig.ValueType.NUMERIC,
                               125,
                               Arrays.asList(1, 125));

    public AgeField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    public Integer generateData() {
        int minAge = (int) config.getConfigProperty(MINIMUM_AGE_PROPERTY);
        int maxAge = (int) config.getConfigProperty(MAXIMUM_AGE_PROPERTY);
        if (maxAge < minAge) {
            throw new IllegalArgumentException("Maximum age should be greather than minimum age");
        }

        return ThreadLocalRandom.current().nextInt(minAge,
                                            maxAge + 1);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(MINIMUM_AGE_PROPERTY, MAXIMUM_AGE_PROPERTY);
    }
}
