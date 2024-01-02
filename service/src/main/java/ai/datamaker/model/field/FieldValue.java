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

package ai.datamaker.model.field;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Represents a {@link Field} with a corresponding generated value.
 */
@RequiredArgsConstructor
@Getter
public class FieldValue implements Serializable {

    private static final long serialVersionUID = -1L;

    private final Field field;

    private final Object value;

    public static FieldValue of(Field field, Object value) {
        return new FieldValue(field, value);
    }

}
