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
@FieldType(description = "Demographic", localizationKey = "field.group.demographic", group = FieldGroup.IDENTITY)
public class DemographicField extends Field<String> {

    static final PropertyConfig DEMOGRAPHIC_TYPE_PROPERTY =
        new PropertyConfig("field.demographic.type",
            "Demographic type",
            PropertyConfig.ValueType.STRING,
            DemographicType.GENDER.toString(),
            Arrays.stream(DemographicType.values()).map(DemographicType::toString).collect(Collectors.toList()));

    public enum DemographicType {
        MARITAL_STATUS, GENDER, RACE, EDUCATION, DEMONYM;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(DEMOGRAPHIC_TYPE_PROPERTY);
    }

    @Override
    protected String generateData() {

        DemographicType type = DemographicType.valueOf((String) config.getConfigProperty(DEMOGRAPHIC_TYPE_PROPERTY));

        switch (type) {
            case MARITAL_STATUS:
                return faker.demographic().maritalStatus();
            default:
            case GENDER:
                return faker.demographic().sex();
            case RACE:
                return faker.demographic().race();
            case EDUCATION:
                return faker.demographic().educationalAttainment();
            case DEMONYM:
                return faker.demographic().demonym();
        }
    }

}
