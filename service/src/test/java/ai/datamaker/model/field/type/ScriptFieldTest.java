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

import ai.datamaker.model.field.type.ScriptField;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptFieldTest {

    @Test
    void generateData() {
        ScriptField field = new ScriptField();

        assertNotNull(field.generateData());
    }

    @Test
    void generateData_withVariables() {
        ScriptField field = new ScriptField();

        field.getConfig().put(ScriptField.EXPRESSION_PROPERTY.getKey(), "#name + ' Doe'");
        field.getConfig().put(ScriptField.EXPRESSION_VARIABLES_NAME.getKey(), Lists.newArrayList("name"));
        field.getConfig().put(ScriptField.EXPRESSION_VARIABLES_VALUE.getKey(), Lists.newArrayList("'John'"));

        System.out.println(field.generateData());
        assertNotNull(field.generateData());
    }

    @Test
    void getConfigProperties() {
    }
}