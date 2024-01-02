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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class NumberFormatter<N extends Number> implements FieldFormatter<N> {

    public static final PropertyConfig NUMBER_PATTERN_PROPERTY =
            new PropertyConfig("formatter.number.pattern",
                               "Number pattern",
                               PropertyConfig.ValueType.STRING,
                               "#,###.00",
                               Collections.emptyList());

    public static final PropertyConfig ROUNDING_MODE_PROPERTY =
            new PropertyConfig("formatter.number.rounding.mode",
                               "Rounding mode",
                               PropertyConfig.ValueType.STRING,
                               RoundingMode.HALF_EVEN.toString(),
                               Arrays.stream(RoundingMode.values()).map(RoundingMode::toString).collect(Collectors.toList()));

    @Override
    public Object format(N value, FieldConfig config) {
        String pattern = (String) config.getConfigProperty(NUMBER_PATTERN_PROPERTY);

        DecimalFormat df = new DecimalFormat(pattern);
        df.setRoundingMode(RoundingMode.valueOf((String) config.getConfigProperty(ROUNDING_MODE_PROPERTY)));
        // NumberFormat df = formatter.get();
        return df.format(value);
    }

//    public void setPattern(String pattern) {
//        this.pattern = pattern;
//        formatter = ThreadLocal.withInitial(() -> {
//            DecimalFormat df = new DecimalFormat(pattern);
//            df.setRoundingMode(roundingMode);
//            return df;
//        });
//    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(NUMBER_PATTERN_PROPERTY, ROUNDING_MODE_PROPERTY);
    }

}
