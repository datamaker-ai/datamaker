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
import com.mifmif.common.regex.Generex;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Regular expression", localizationKey = "field.group.regex", group = FieldGroup.CUSTOM)
public class RegexField extends Field<String> {

    static final PropertyConfig REGEX_VALUE_PROPERTY =
        new PropertyConfig("field.regex.value",
            "Pattern",
            PropertyConfig.ValueType.STRING,
            "\\w{10}",
            Collections.emptyList());

    public RegexField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    public String generateData() {
        String regex = (String) config.getConfigProperty(REGEX_VALUE_PROPERTY);
        Generex generex = new Generex(regex);
        //generex.setSeed(System.currentTimeMillis());
        // Generate random String
        return generex.random();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(REGEX_VALUE_PROPERTY);
    }

    public void setRegex(String pattern) {
        config.put(REGEX_VALUE_PROPERTY, pattern);
    }
}
