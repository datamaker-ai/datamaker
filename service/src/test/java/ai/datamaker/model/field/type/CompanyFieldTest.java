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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ai.datamaker.model.field.type.CompanyField;
import ai.datamaker.model.field.type.CompanyField.CompanyDataType;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class CompanyFieldTest {

    @Test
    void generateData() {
        CompanyField field = new CompanyField("test", Locale.ENGLISH);
        field.setType(CompanyDataType.NAME);
        assertEquals("test", field.getName());
        assertEquals(CompanyDataType.NAME, field.getType());
        assertFalse(field.generateData().isEmpty());
    }
}