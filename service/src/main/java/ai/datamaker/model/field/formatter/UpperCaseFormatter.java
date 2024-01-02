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

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import lombok.Data;

@Data
public class UpperCaseFormatter implements FieldFormatter<String> {

    @Override
    public Object format(String value, FieldConfig config) {
        Locale locale = Locale.forLanguageTag((String) config.getConfigProperty(LOCALE_PROPERTY));
        return value.toUpperCase(locale);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY);
    }
}
