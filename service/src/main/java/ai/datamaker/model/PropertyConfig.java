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

package ai.datamaker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
@ToString
public class PropertyConfig {
    private final String key;
    private final String description;
    // string, numeric, enum, list, map
    private final ValueType type;
    private final Object defaultValue;
    private final Collection<?> possibleValues;
    // private final Class<V> classType;
    // TODO nice to have validator

    public enum ValueType {
        STRING, NUMERIC, LIST, BOOLEAN, SECRET, OBJECT, PASSWORD, REFERENCE, REFERENCES, DATE, FIELD, FIELDS, EXPRESSION, TIME, FILE
    }
}
