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
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Duration", localizationKey = "field.group.primitive.duration", group = FieldGroup.PRIMITIVE)
public class DurationField extends Field<Duration> {

    public static final PropertyConfig DURATION_FORMAT_PROPERTY =
            new PropertyConfig("field.duration.format",
                               "Format",
                               PropertyConfig.ValueType.STRING,
                               "P5DT5H5M5.4S",
                               Collections.emptyList());

    public DurationField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Duration generateData() {
        String format = (String) config.getConfigProperty(DURATION_FORMAT_PROPERTY);

        return Duration.parse(format);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(DURATION_FORMAT_PROPERTY);
    }
}
