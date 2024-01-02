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

import java.util.UUID;

public final class UUIDUtils {

    private UUIDUtils() {

    }

    public static UUID format(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        final String res = sb.insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-")
                .toString();

        return UUID.fromString(res);
    }

    public static void main(String[] args) {
        System.out.println(UUIDUtils.format("16ff2f43c66146759dae34c625dc0996"));
    }
}
