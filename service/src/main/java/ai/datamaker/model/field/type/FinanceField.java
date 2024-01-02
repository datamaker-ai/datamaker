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
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Financial", localizationKey = "field.group.finance", group = FieldGroup.CUSTOM)
public class FinanceField extends Field<String> {

    static final PropertyConfig FINANCE_TYPE_PROPERTY =
        new PropertyConfig("field.finance.type",
            "Finance type",
            PropertyConfig.ValueType.STRING,
            FinanceType.BIC.toString(),
            Arrays.stream(FinanceType.values()).map(FinanceType::toString).collect(Collectors.toList()));

    public enum FinanceType {
        VAT_NUMBER, IBAN, BIC, ACCOUNT_NUMBER, TAX_NUMBER
    }

    @Override
    protected String generateData() {

        FinanceType type = FinanceType.valueOf((String) config.getConfigProperty(FINANCE_TYPE_PROPERTY));

        switch(type) {
            case VAT_NUMBER:
                // FIXME implement
                return "";
            case IBAN:
                return faker.finance().iban();
            default:
            case BIC:
                return faker.finance().bic();
            case ACCOUNT_NUMBER:
                return "";
            case TAX_NUMBER:
                // TODO based on locale
                return "";
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(FINANCE_TYPE_PROPERTY);
    }

}
