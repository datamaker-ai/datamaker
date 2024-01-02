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
import com.github.javafaker.CreditCardType;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Credit card number", localizationKey = "field.group.creditCard", group = FieldGroup.MONEY)
public class CreditCardField extends Field<String> {

    static final PropertyConfig CREDIT_CARD_TYPE_PROPERTY =
            new PropertyConfig("field.credit.card.type",
                               "Credit card type",
                               PropertyConfig.ValueType.STRING,
                               "RANDOM",
                               Lists.newArrayList("RANDOM",
                                                  CreditCardType.VISA.toString(),
                                                  CreditCardType.AMERICAN_EXPRESS.toString(),
                                                  CreditCardType.DANKORT.toString(),
                                                  CreditCardType.DINERS_CLUB.toString(),
                                                  CreditCardType.DISCOVER.toString(),
                                                  CreditCardType.FORBRUGSFORENINGEN.toString(),
                                                  CreditCardType.JCB.toString(),
                                                  CreditCardType.LASER.toString(),
                                                  CreditCardType.MASTERCARD.toString(),
                                                  CreditCardType.SOLO.toString(),
                                                  CreditCardType.SWITCH.toString()));

    public CreditCardField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected String generateData() {
        //creditCardExpiry
        String creditCardType = (String) config.getConfigProperty(CREDIT_CARD_TYPE_PROPERTY);
        return creditCardType.equals("RANDOM") ? faker.finance().creditCard() : faker.finance().creditCard(CreditCardType.valueOf(creditCardType));
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(CREDIT_CARD_TYPE_PROPERTY);
    }

    public void setCreditCardType(CreditCardType creditCardType) {
        config.put(CREDIT_CARD_TYPE_PROPERTY.getKey(), creditCardType.toString());
    }
}
