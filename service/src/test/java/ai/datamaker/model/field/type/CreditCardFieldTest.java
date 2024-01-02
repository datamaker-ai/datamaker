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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.datamaker.model.field.formatter.CreditCardFormatter;
import ai.datamaker.model.field.formatter.CreditCardFormatter.CreditCardFormatType;
import java.util.Locale;

import ai.datamaker.model.field.type.CreditCardField;
import com.github.javafaker.CreditCardType;
import org.junit.jupiter.api.Test;

class CreditCardFieldTest {

    @Test
    void generateData() {
        CreditCardField field = new CreditCardField("cc", Locale.ENGLISH);
        field.setCreditCardType(CreditCardType.MASTERCARD);
        field.getConfig().put(CreditCardFormatter.CREDIT_CARD_FORMAT_TYPE_PROPERTY.getKey(),
                              CreditCardFormatType.NONE.toString());

        CreditCardFormatter formatter = new CreditCardFormatter();
        field.setFormatter(formatter);

        assertEquals(16, field.getData().toString().length());
    }
}