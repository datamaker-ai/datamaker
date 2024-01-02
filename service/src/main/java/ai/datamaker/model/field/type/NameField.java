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
import java.util.stream.Collectors;

@Entity
@Indexed
@NoArgsConstructor
@FieldType(description = "Full name", localizationKey = "field.group.fullName", group = FieldGroup.IDENTITY)
public class NameField extends Field<String> {

    static final PropertyConfig NAME_TYPE_PROPERTY =
            new PropertyConfig("field.name.type",
                               "Name type",
                               PropertyConfig.ValueType.STRING,
                               NameType.FULL.toString(),
                               Arrays.stream(NameType.values()).map(NameType::toString).collect(Collectors.toList()));

    public enum NameType {
        FULL, FIRST, LAST, MIDDLE, TITLE, PREFIX, SUFFIX
    }

    public NameField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(NAME_TYPE_PROPERTY);
    }

    @Override
    protected String generateData() {

        NameType type = NameType.valueOf((String) config.getConfigProperty(NAME_TYPE_PROPERTY));

        switch(type) {
            default:
            case FULL:
                return faker.name().fullName();
            case FIRST:
                return faker.name().firstName();
            case LAST:
                return faker.name().lastName();
            case MIDDLE:
                return faker.name().nameWithMiddle();
            case TITLE:
                return faker.name().title();
            case PREFIX:
                return faker.name().prefix();
            case SUFFIX:
                return faker.name().suffix();
        }
    }

}
