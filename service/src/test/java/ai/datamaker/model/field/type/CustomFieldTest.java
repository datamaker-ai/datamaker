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

import ai.datamaker.model.field.type.CustomField;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomFieldTest {

    @Test
    void generateData_flightNumber() {
        CustomField flightNumber = new CustomField("flight",
                                                   Locale.getDefault());
        flightNumber.setPattern("FLIGHT-???-####");
        flightNumber.setUpper(true);
        String flightResult = flightNumber.generateData();
        assertTrue(flightResult.matches("FLIGHT-\\w{3}-\\d{4}"));
    }

    @Test
    void generateData_pdoNumber() {
        CustomField pdoNumber = new CustomField("pdo",
                                                Locale.getDefault());
        pdoNumber.setPattern("###########");
        pdoNumber.setUpper(true);
        String pdoResult = pdoNumber.generateData();
        assertTrue(pdoResult.matches("\\d{11}"));
    }
}