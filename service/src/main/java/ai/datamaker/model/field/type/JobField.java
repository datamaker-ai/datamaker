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

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Job position", localizationKey = "field.group.job", group = FieldGroup.JOB)
public class JobField extends Field<String> {

    static final PropertyConfig JOB_FIELD_TYPE_PROPERTY =
        new PropertyConfig("field.job.type",
            "Job type",
            PropertyConfig.ValueType.STRING,
            JobFieldType.TITLE.toString(),
            Arrays.stream(JobFieldType.values()).map(JobFieldType::toString).collect(Collectors.toList()));

    public enum JobFieldType {
        TITLE, SENIORITY, KEY_SKILLS, POSITION, FIELD;
    }

    @Override
    protected String generateData() {

        JobFieldType type = JobFieldType.valueOf((String) config.getConfigProperty(JOB_FIELD_TYPE_PROPERTY));

        switch(type) {
            case TITLE:
                return faker.job().title();
            case SENIORITY:
                return faker.job().seniority();
            case KEY_SKILLS:
                return faker.job().keySkills();
            case POSITION:
                return faker.job().position();
            case FIELD:
                return faker.job().field();
        }
        return faker.job().title();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(JOB_FIELD_TYPE_PROPERTY);
    }

}

