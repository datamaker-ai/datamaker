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

import ai.datamaker.model.Constants;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Big integer", localizationKey = "field.group.primitive.biginteger", group = FieldGroup.PRIMITIVE)
public class BigIntegerField extends Field<BigInteger> {

    static final BigInteger MAX_VALUE = new BigInteger("9999999999999999999999999999999999999999999999");

    static final PropertyConfig MIN_VALUE_PROPERTY =
            new PropertyConfig(Constants.MIN_VALUE_PROPERTY,
                        "Minimum value",
                        ValueType.STRING,
                        BigInteger.ZERO.toString(),
                        Collections.emptyList());

    static final PropertyConfig MAX_VALUE_PROPERTY =
            new PropertyConfig(Constants.MAX_VALUE_PROPERTY,
                        "Maximum value",
                        ValueType.STRING,
                        MAX_VALUE.toString(),
                        Collections.emptyList());

    static final PropertyConfig NEGATIVE_VALUES_PROPERTY =
            new PropertyConfig(Constants.NEGATIVE_VALUE_PROPERTY,
                               "Negative values",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Collections.emptyList());

    static final PropertyConfig BIT_LENGTH_PROPERTY =
            new PropertyConfig("field.bigint.bit.length",
                               "Bit length",
                               PropertyConfig.ValueType.NUMERIC,
                               64,
                               Collections.emptyList());

    public BigIntegerField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected BigInteger generateData() {
        BigInteger lowerLimit = new BigInteger((String) config.getConfigProperty(MIN_VALUE_PROPERTY));
        BigInteger upperLimit = new BigInteger((String) config.getConfigProperty(MAX_VALUE_PROPERTY));

        BigInteger randomNumber;
        do {
            int bitLength = (int) config.getConfigProperty(BIT_LENGTH_PROPERTY);
            randomNumber = new BigInteger(bitLength, ThreadLocalRandom.current());
        } while (randomNumber.compareTo(upperLimit) >= 0 && randomNumber.compareTo(lowerLimit) <= 0);

        return randomNumber;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(BIT_LENGTH_PROPERTY,
                                MIN_VALUE_PROPERTY,
                                MAX_VALUE_PROPERTY,
                                NEGATIVE_VALUES_PROPERTY);
    }

    public void setBitLength(int bitLength) {
        config.put(BIT_LENGTH_PROPERTY.getKey(), bitLength);
    }
}
