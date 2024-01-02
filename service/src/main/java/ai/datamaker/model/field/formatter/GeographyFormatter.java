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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.Assert;

@Data
public class GeographyFormatter implements FieldFormatter<Object> {
    // Formatter will take care of that
    public enum FormatType {
        GEO_JSON, LAT_LONG, LATITUDE, LONGITUDE, AS_STRING, AS_DOUBLE, NONE
    }

    public static final String GEO_JSON_TEMPLATE = "{\n" +
            "    \"type\": \"%s\",\n" +
            "    \"coordinates\": %s\n" +
            "}";

    public static final String POINT_TEMPLATE = "{\n" +
            "    \"type\": \"Point\",\n" +
            "    \"coordinates\": %s\n" +
            "}";

    public static final String POLYGON_TEMPLATE = "{\n" +
            "    \"type\": \"Polygon\",\n" +
            "    \"coordinates\": %s\n" +
            "}";

    public static final String LINE_STRING_TEMPLATE = "{\n" +
            "    \"type\": \"LineString\",\n" +
            "    \"coordinates\": %s\n" +
            "}";

    public static final PropertyConfig TEMPLATE_PROPERTY =
            new PropertyConfig("formatter.geography.template",
                               "Template",
                               PropertyConfig.ValueType.STRING,
                               GEO_JSON_TEMPLATE,
                               Collections.emptyList());

    public static final PropertyConfig GEOGRAPHY_FORMAT_TYPE_PROPERTY =
            new PropertyConfig("formatter.geography.format.type",
                               "Format type",
                               PropertyConfig.ValueType.STRING,
                               FormatType.NONE.toString(),
                               Arrays.stream(FormatType.values()).map(FormatType::toString).collect(Collectors.toList()));

    @Override
    public Object format(Object value, FieldConfig config) {

        if (value == null || !value.getClass().isArray()) {
            throw new IllegalArgumentException("not a double array");
        }

        FormatType type = FormatType.valueOf((String) config.getConfigProperty(GEOGRAPHY_FORMAT_TYPE_PROPERTY));

        String template = (String) config.getConfigProperty(TEMPLATE_PROPERTY);

        final Object[] values = (Object[]) value;

        switch (type) {
            case GEO_JSON:
                Assert.notNull(template, "template cannot be null");
                return String.format(template, java.util.Arrays.deepToString(values));
            case LATITUDE:
                return values[1];
            case LONGITUDE:
                return values[0];
            case AS_STRING:
                return String.format("%.8g,%.8g", (Double) values[0], (Double) values[1]);
            case AS_DOUBLE:
            default:
                return value;
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
                GEOGRAPHY_FORMAT_TYPE_PROPERTY,
                TEMPLATE_PROPERTY);
    }
}
