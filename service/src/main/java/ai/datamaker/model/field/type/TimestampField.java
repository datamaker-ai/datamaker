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

import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.sql.Timestamp;
import java.util.Locale;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Number of seconds after epoch", localizationKey = "field.group.timestamp", group = FieldGroup.PRIMITIVE)
public class TimestampField extends Field<Timestamp> {

    // TODO support current, past, future

    public TimestampField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected Timestamp generateData() {
        return new Timestamp(System.currentTimeMillis());
    }

}
