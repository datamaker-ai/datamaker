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
import com.google.common.collect.Lists;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * See {@link DateTimeFormatter}
 */
@Data
public class DateTimeNumericFormatter implements FieldFormatter<Date> {

    public static final PropertyConfig NUMERIC_TYPE_PROPERTY =
            new PropertyConfig("formatter.datetime.numeric.type",
                               "Timezone",
                               PropertyConfig.ValueType.STRING,
                               "Long",
                               Lists.newArrayList("Integer", "Long"));

    @Override
    public Object format(Date value, FieldConfig config) {
        String type = (String) config.getConfigProperty(NUMERIC_TYPE_PROPERTY);
        if ("Integer".equals(type)) {
            return (int) (value.getTime() / 1000L);
        }
        return value.getTime();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(NUMERIC_TYPE_PROPERTY);
    }

}
