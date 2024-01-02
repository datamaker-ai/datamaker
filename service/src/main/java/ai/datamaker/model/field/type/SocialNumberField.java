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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Social security number", localizationKey = "field.group.ssn", group = FieldGroup.IDENTITY)
public class SocialNumberField extends Field<String> {

    static final PropertyConfig SIN_SEPARATOR_PROPERTY =
            new PropertyConfig("field.socialnumberfield.sin.separator.type",
                               "Social insurance number separator",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.EMPTY_LIST);

    static final PropertyConfig SIN_REGION_PROPERTY =
            new PropertyConfig("field.socialnumberfield.sin.region.type",
                               "Region prefix  0: Fictious purposes " + "         1: New Brunswick, Nova Scotia, Prince Edward Island, and Labrador and Newfoundland " + "         2–3: Québec " + "         4–5: overseas forces, and Ontario (excluding Northwestern Ontario) " + "         6: Manitoba, Northwestern Ontario, Alberta, Saskatchewan, Nunavut, and Northwest Territories " + "         7: Yukon and British Columbia " + "         8: Not used " + "         9: Temporary resident",
                               PropertyConfig.ValueType.STRING,
                               4,
                               Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

    public SocialNumberField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected String generateData() {
        if ("CA".equals(getLocale().getCountry())) {
            Integer prefix = (Integer) config.getConfigProperty(SIN_REGION_PROPERTY);

            return sin(prefix);
        }
        if ("FR".equals(getLocale().getCountry())) {
            return "NIR";
        }
        if ("sv-SE".equals(getLocale().toLanguageTag())) {
            return  faker.idNumber().validSvSeSsn();
        }

        // LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(creditCardNumber);
        return faker.idNumber().ssnValid();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(SIN_SEPARATOR_PROPERTY,
                                  SIN_REGION_PROPERTY);
    }

    public String sin(int prefix) {
        String template = this.faker.numerify(String.format("/%d#######L/", prefix));
        String[] split = template.replaceAll("[^0-9]", "").split("");
        List<Integer> reversedAsInt = new ArrayList();

        int luhnSum;
        for(luhnSum = 0; luhnSum < split.length; ++luhnSum) {
            String current = split[split.length - 1 - luhnSum];
            if (!current.isEmpty()) {
                reversedAsInt.add(Integer.valueOf(current));
            }
        }

        luhnSum = 0;
        int multiplier = 1;

        Integer digit;
        for(Iterator var9 = reversedAsInt.iterator(); var9.hasNext(); luhnSum += sum(String.valueOf(digit * multiplier).split(""))) {
            digit = (Integer)var9.next();
            multiplier = multiplier == 2 ? 1 : 2;
        }

        int luhnDigit = (10 - luhnSum % 10) % 10;
        return template.replace('\\', ' ').replace('/', ' ').trim().replace('L', String.valueOf(luhnDigit).charAt(0));
    }

    private static int sum(String[] string) {
        int sum = 0;
        String[] var2 = string;
        int var3 = string.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String s = var2[var4];
            if (!s.isEmpty()) {
                sum += Integer.valueOf(s);
            }
        }

        return sum;
    }
}
