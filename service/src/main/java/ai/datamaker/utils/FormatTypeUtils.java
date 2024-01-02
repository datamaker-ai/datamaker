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

import ai.datamaker.generator.FormatType;

public abstract class FormatTypeUtils {

    private FormatTypeUtils() {
    }

    public static String getContentType(FormatType formatType) {
        switch (formatType) {
            case AVRO:
                return "application/avro-binary";
            case CSV:
                return "text/csv";
            case EXCEL:
                return "application/x-msexcel";
            case JSON:
                return "application/json";
            case PARQUET:
            case ORC:
                return "application/octet-stream";
            case PDF:
                return "application/pdf";
            case XML:
                return "application/xml";
            case SQL:
                return "application/sql";
        }
        return "text/plain";
    }
}
