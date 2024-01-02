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
import java.util.stream.Collectors;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Item identification number", localizationKey = "field.group.item.id", group = FieldGroup.CUSTOM)
public class IdentificationNumberField extends Field<String> {

    static final PropertyConfig IDENTIFICATION_NUMBER_TYPE_PROPERTY =
        new PropertyConfig("field.itemnumber.type",
            "Identification number type",
            PropertyConfig.ValueType.STRING,
            IdentificationNumberType.UPC.toString(),
            Arrays.stream(IdentificationNumberType.values()).map(IdentificationNumberType::toString).collect(Collectors.toList()));

    public enum IdentificationNumberType {
        BARCODE, UPC, ISBN, ASIN, ISBN10, ISBN13, IMEI, EAN8, EAN13, GTIN8, GTIN13;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(IDENTIFICATION_NUMBER_TYPE_PROPERTY);
    }

    @Override
    protected String generateData() {
        // UPC = GTIN12?
        IdentificationNumberType type = IdentificationNumberType.valueOf((String) config.getConfigProperty(IDENTIFICATION_NUMBER_TYPE_PROPERTY));

        switch(type) {
            default:
                //TODO implement
            case BARCODE:
            case UPC:
                return "";
            case ASIN:
                return faker.code().asin();
            case ISBN10:
                return faker.code().isbn10();
            case ISBN:
            case ISBN13:
                return faker.code().isbn13();
            case IMEI:
                return faker.code().imei();
            case EAN8:
                return faker.code().ean8();
            case EAN13:
                return faker.code().ean13();
            case GTIN8:
                return faker.code().gtin8();
            case GTIN13:
                return faker.code().gtin13();
        }
    }
}
