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

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.CapitalizeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapitalizeFormatterTest {

    private final CapitalizeFormatter formatter = new CapitalizeFormatter();

    @Test
    void format() {
        String first = "allo mon coco";
        FieldConfig config = new FieldConfig();
        assertEquals("Allo mon coco", formatter.format(first, config));

        config.put(CapitalizeFormatter.CAPITALIZE_FULLY_PROPERTY, true);
        assertEquals("Allo Mon Coco", formatter.format(first, config));
    }

    @Test
    void getConfigProperties() {
        assertEquals(2, formatter.getConfigProperties().size());
        assertEquals(CapitalizeFormatter.LOCALE_PROPERTY, formatter.getConfigProperties().get(0));
        assertEquals(CapitalizeFormatter.CAPITALIZE_FULLY_PROPERTY, formatter.getConfigProperties().get(1));
    }
}