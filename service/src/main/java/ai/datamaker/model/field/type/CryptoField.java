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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.search.annotations.Indexed;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Hashed value: MD5, SHA-256", localizationKey = "field.group.crypto.value", group = FieldGroup.CUSTOM)
public class CryptoField extends Field<String> {

    static final PropertyConfig ALGORITHM_TYPE_PROPERTY =
            new PropertyConfig("field.crypto.type",
                               "Algorithm",
                               PropertyConfig.ValueType.STRING,
                               Algorithm.SHA256.toString(),
                               Arrays.stream(Algorithm.values()).map(
                                       Algorithm::toString).collect(Collectors.toList()));

    @Transient
    @JsonIgnore
    private transient PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(ALGORITHM_TYPE_PROPERTY);
    }

    public void setType(Algorithm algorithm) {
        config.put(ALGORITHM_TYPE_PROPERTY.getKey(), algorithm.toString());
    }

    public enum Algorithm {
        BCRYPT, MD5, SHA1, SHA256, SHA512, SIMPLE
    }

    @Override
    protected String generateData() {

        Algorithm type = Algorithm.valueOf((String) config.getConfigProperty(ALGORITHM_TYPE_PROPERTY));

        switch (type) {
            case BCRYPT:
                return encoder.encode(faker.lorem().word());
            case MD5:
                return faker.crypto().md5();
            case SHA1:
                return faker.crypto().sha1();
            default:
            case SHA256:
                return faker.crypto().sha256();
            case SHA512:
                return faker.crypto().sha512();
            case SIMPLE:
                return String.valueOf(RandomStringUtils.randomAlphabetic(24).hashCode());
        }
    }

}
