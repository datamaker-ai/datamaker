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

import ai.datamaker.model.Configurable;
import ai.datamaker.model.Constants;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.FieldConfig;

import java.util.Arrays;
import java.util.Locale;

public interface FieldFormatter<V> extends Configurable {

    PropertyConfig LOCALE_PROPERTY =
            new PropertyConfig(Constants.LOCALE,
                               "Locale",
                               PropertyConfig.ValueType.STRING,
                               Locale.ENGLISH.toLanguageTag(),
                               Arrays.asList(Locale.ENGLISH.toLanguageTag(), Locale.FRENCH.toLanguageTag()));

    Object format(V value, FieldConfig config);
}
