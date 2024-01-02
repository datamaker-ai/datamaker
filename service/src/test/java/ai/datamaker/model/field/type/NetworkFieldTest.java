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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ai.datamaker.model.field.type.NetworkField;
import ai.datamaker.model.field.type.NetworkField.NetworkAddressType;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

class NetworkFieldTest {

    @Test
    void generateData() {
        NetworkField field = new NetworkField();
        field.setType(NetworkAddressType.IPv6);

        String value = field.generateData();

        assertNotNull(value);
        assertEquals(7, StringUtils.countMatches(value, ":"));
    }

    @Test
    void generateData_publicIpv4() {
        NetworkField field = new NetworkField();
        field.setType(NetworkAddressType.IPv4);
        field.setPrivateRange(false);

        String value = field.generateData();

        assertNotNull(value);
        assertEquals(3, StringUtils.countMatches(value, "."));
    }

    @Test
    void generateData_publicIpv4Cidr() {
        NetworkField field = new NetworkField();
        field.setType(NetworkAddressType.IPv4_CIDR);
        field.setPrivateRange(false);

        String value = field.generateData();

        assertNotNull(value);
        assertEquals(3, StringUtils.countMatches(value, "."));
        assertEquals(1, StringUtils.countMatches(value, "/"));
    }
}