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

import static ai.datamaker.model.field.type.AddressField.AddressType.FULL;

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Fully formatted address using locale", localizationKey = "field.group.address", group = FieldGroup.PHYSICAL_LOCATION)
public class AddressField extends Field<String> {

    static final PropertyConfig ADDRESS_TYPE_PROPERTY =
            new PropertyConfig("field.address.type",
                               "Address type",
                               PropertyConfig.ValueType.STRING,
                               FULL.toString(),
                               Arrays.stream(AddressType.values()).map(AddressType::toString).collect(Collectors.toList()));

    public enum AddressType {
        FULL, STREET_NAME, STREET_WITH_NUMBER, ZIP_CODE, CEDEX, POSTAL_CODE, COUNTRY, STATE, PROVINCE, CITY;
    }

    public AddressField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(ADDRESS_TYPE_PROPERTY);
    }

    @Override
    protected String generateData() {

        AddressType type = AddressType.valueOf((String) config.getConfigProperty(ADDRESS_TYPE_PROPERTY));

        switch (type){
            default:
            case FULL:
                // FIXME extremely slow because of postal code/zip regex
                return faker.address().fullAddress();
            case STREET_NAME:
                return faker.address().streetName();
            case STREET_WITH_NUMBER:
                return faker.address().streetAddress();
            case ZIP_CODE:
            case CEDEX:
            case POSTAL_CODE:
                return faker.address().zipCode();
            case COUNTRY:
                return faker.address().country();
            case STATE:
            case PROVINCE:
                return faker.address().state();
            case CITY:
                return faker.address().cityName();
        }
    }
}
