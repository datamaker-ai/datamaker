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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Home phone", localizationKey = "field.group.phone.home", group = FieldGroup.IDENTITY)
public class PhoneField extends Field<String> {

    static final PropertyConfig PHONE_TYPE_PROPERTY =
        new PropertyConfig("field.phone.type",
            "Name type",
            PropertyConfig.ValueType.STRING,
            PhoneType.HOME.toString(),
            Arrays.stream(PhoneType.values()).map(PhoneType::toString).collect(Collectors.toList()));

    public enum PhoneType {
        MOBILE, HOME, BUSINESS, FAX, EXTENSION;

    }
    public PhoneField(String name, Locale locale) {
        super(name,
              locale);
    }
    @Override
    protected String generateData() {
        PhoneType type = PhoneType.valueOf((String) config.getConfigProperty(PHONE_TYPE_PROPERTY));

        switch (type) {
            case MOBILE:
                return faker.phoneNumber().cellPhone();
            default:
            case HOME:
            case BUSINESS:
            case FAX:
                return faker.phoneNumber().phoneNumber();
            case EXTENSION:
                return faker.phoneNumber().subscriberNumber();
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(PHONE_TYPE_PROPERTY);
    }

    public void setType(PhoneType phoneType) {
        config.put(PHONE_TYPE_PROPERTY, phoneType.toString());
    }
}
