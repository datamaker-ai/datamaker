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

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Business/legal entity/company name", localizationKey = "field.group.company", group = FieldGroup.BUSINESS)
public class CompanyField extends Field<String> {

    static final PropertyConfig COMPANY_TYPE_PROPERTY =
            new PropertyConfig("field.company.type",
                               "Company type",
                               PropertyConfig.ValueType.STRING,
                               CompanyDataType.NAME.toString(),
                               Arrays.stream(CompanyDataType.values()).map(
                                       CompanyDataType::toString).collect(Collectors.toList()));

    public CompanyField(String name, Locale locale) {
        super(name, locale);
    }

    public void setType(CompanyDataType companyDataType) {
        config.put(COMPANY_TYPE_PROPERTY.getKey(), companyDataType.toString());
    }

    public CompanyDataType getType() {
        return CompanyDataType.valueOf((String) config.getConfigProperty(COMPANY_TYPE_PROPERTY));
    }

    public enum CompanyDataType {
        NAME, BUZZWORD, CATCH_PHRASE, PROFESSION, LOGO, INDUSTRY, URL, SUFFIX;

    }
    @Override
    protected String generateData() {

        CompanyDataType type = CompanyDataType.valueOf((String) config.getConfigProperty(COMPANY_TYPE_PROPERTY));

        switch (type) {
            default:
            case NAME:
                return faker.company().name();
            case BUZZWORD:
                return faker.company().buzzword();
            case CATCH_PHRASE:
                return faker.company().catchPhrase();
            case PROFESSION:
                return faker.company().profession();
            case LOGO:
                return faker.company().logo();
            case INDUSTRY:
                return faker.company().industry();
            case URL:
                return faker.company().url();
            case SUFFIX:
                return faker.company().suffix();
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(COMPANY_TYPE_PROPERTY);
    }

}
