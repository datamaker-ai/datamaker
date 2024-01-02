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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CreditCardFormatter implements FieldFormatter<String> {

    public static final PropertyConfig CREDIT_CARD_FORMAT_TYPE_PROPERTY =
            new PropertyConfig("formatter.credit.card.format.type",
                               "Credit card format",
                               PropertyConfig.ValueType.STRING,
                               CreditCardFormatType.NONE.toString(),
                               Arrays.stream(CreditCardFormatType.values()).map(CreditCardFormatType::toString).collect(Collectors.toList()));

    public enum CreditCardFormatType {SPACES, HYPHENS, NONE}

    @Override
    public Object format(String value, FieldConfig config) {

        CreditCardFormatType formatType = CreditCardFormatType.valueOf(
                (String) config.getConfigProperty(CREDIT_CARD_FORMAT_TYPE_PROPERTY));

        String cleanValue = value
                .replace("-", "")
                .replace(" ", "");

        switch (formatType) {
            case SPACES:
                return String.format("%s %s %s %s", cleanValue.substring(0,4), cleanValue.substring(4,8), cleanValue.substring(8,12), cleanValue.substring(12,16));
            case HYPHENS:
                return String.format("%s-%s-%s-%s", cleanValue.substring(0,4), cleanValue.substring(4,8), cleanValue.substring(8,12), cleanValue.substring(12,16));
            default:
            case NONE:
            return cleanValue;
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(CREDIT_CARD_FORMAT_TYPE_PROPERTY);
    }
}
