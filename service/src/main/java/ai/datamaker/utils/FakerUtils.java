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

package ai.datamaker.utils;

import com.github.javafaker.Faker;
import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

/**
 * Optimize {@link Faker} creation.
 */
public final class FakerUtils {

    private static Map<Locale, Faker> FAKER_PER_LOCALE = Maps.newHashMap();

    private FakerUtils() {

    }

    public static Faker getFakerForLocale(Locale locale) {
        if (!FAKER_PER_LOCALE.containsKey(locale)) {
            FAKER_PER_LOCALE.put(locale, new Faker(locale));
        }
        return FAKER_PER_LOCALE.get(locale);
    }
}
