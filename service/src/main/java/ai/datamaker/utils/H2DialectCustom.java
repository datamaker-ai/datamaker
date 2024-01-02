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

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class H2DialectCustom extends H2Dialect {

    public H2DialectCustom() {
        super();
        registerColumnType(Types.FLOAT, "double");
        registerColumnType(Types.BINARY, "varbinary");
    }

}
