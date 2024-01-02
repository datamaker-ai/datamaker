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

package ai.datamaker.model.field.formatter;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.FieldConfig;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import lombok.Data;

/**
 * See {@link DateTimeFormatter}
 */
@Data
public class DateTimeStringFormatter implements FieldFormatter<Date> {

    public static final PropertyConfig TIMEZONE_PROPERTY =
            new PropertyConfig("formatter.datetime.timezone",
                               "Timezone",
                               PropertyConfig.ValueType.STRING,
                               "UTC",
                               ZoneId.getAvailableZoneIds());

    public static final PropertyConfig OUTPUT_FORMAT_PROPERTY =
            new PropertyConfig("formatter.datetime.output.format",
                               "Output format",
                               PropertyConfig.ValueType.STRING,
                               "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                               Collections.emptyList());

    @Override
    public Object format(Date value, FieldConfig config) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern((String) config.getConfigProperty(OUTPUT_FORMAT_PROPERTY))
                .withZone(ZoneId.of((String) config.getConfigProperty(TIMEZONE_PROPERTY)));

        return formatter.format(value.toInstant());
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(TIMEZONE_PROPERTY,
                                  OUTPUT_FORMAT_PROPERTY);
    }

}
