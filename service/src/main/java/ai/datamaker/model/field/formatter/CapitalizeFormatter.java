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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Collections;
import java.util.List;

@Data
public class CapitalizeFormatter implements FieldFormatter<String> {

    public static final PropertyConfig CAPITALIZE_FULLY_PROPERTY =
            new PropertyConfig("formatter.capitalize.fully",
                               "Capitalize fully",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Collections.emptyList());

    @Override
    public Object format(String value, FieldConfig config) {
        boolean capitalizeFully = (boolean) config.getConfigProperty(CAPITALIZE_FULLY_PROPERTY);

        return capitalizeFully ? WordUtils.capitalizeFully(value) : StringUtils.capitalize(value);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY,
                                  CAPITALIZE_FULLY_PROPERTY);
    }
}
