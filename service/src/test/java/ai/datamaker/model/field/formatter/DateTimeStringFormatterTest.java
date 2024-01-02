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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.DateTimeStringFormatter;
import org.junit.jupiter.api.Test;

class DateTimeStringFormatterTest {

    LocalDateTime localDateTime = LocalDateTime.of(2008, 8, 8, 8, 8, 8);
    Date testDate = Date.from(localDateTime.toInstant(ZoneOffset.UTC));

    @Test
    void format_default() {
        FieldConfig fieldConfig = new FieldConfig();
        DateTimeStringFormatter formatter = new DateTimeStringFormatter();
        String value = (String) formatter.format(testDate, fieldConfig);
        assertEquals("2008-08-08T08:08:08.000Z", value);
    }

    @Test
    void format_customFormatter() {
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(DateTimeStringFormatter.OUTPUT_FORMAT_PROPERTY.getKey(), "dd-MM-yyyy hh-MM-ss");
        DateTimeStringFormatter formatter = new DateTimeStringFormatter();
        String value = (String) formatter.format(testDate, fieldConfig);
        assertEquals("08-08-2008 08-08-08", value);
    }
}