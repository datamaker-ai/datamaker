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
import java.time.Period;
import java.util.Collections;
import java.util.List;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Period", localizationKey = "field.group.primitive.period", group = FieldGroup.PRIMITIVE)
public class PeriodField extends Field<Period> {

    public static final PropertyConfig PERIOD_FORMAT_PROPERTY =
            new PropertyConfig("field.period.format",
                               "Format",
                               PropertyConfig.ValueType.STRING,
                               "P1Y2M3D",
                               Collections.emptyList());
    @Override
    protected Period generateData() {
        String format = (String) config.getConfigProperty(PERIOD_FORMAT_PROPERTY);

        return Period.parse(format);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(PERIOD_FORMAT_PROPERTY);
    }
}
