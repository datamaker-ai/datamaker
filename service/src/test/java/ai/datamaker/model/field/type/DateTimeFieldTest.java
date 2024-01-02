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

import ai.datamaker.model.field.formatter.DateTimeStringFormatter;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DateTimeField.DateType;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeFieldTest {

    @Test
    void generateData_dob() {
        DateTimeField birthDate = new DateTimeField("birthday", Locale.CANADA_FRENCH);

        birthDate.setType(DateType.DATE_OF_BIRTH);

        assertNotNull(birthDate.generateData());
    }

    @Test
    void generateData_past() {
        DateTimeField date = new DateTimeField("past", Locale.CANADA_FRENCH);
        date.setType(DateType.PAST);

        assertTrue(date.generateData().before(new Date()));
    }

    @Test
    void generateData_future() {
        DateTimeField date = new DateTimeField("future", Locale.CANADA_FRENCH);
        date.setType(DateType.FUTURE);

        assertTrue(date.generateData().after(new Date()));
    }

    @Test
    void generateData_between() throws ParseException {
        DateTimeField date = new DateTimeField("between", Locale.CANADA_FRENCH);
        date.getConfig().put(DateTimeField.START_DATE_PROPERTY.getKey(), "2019-01-01");
        date.getConfig().put(DateTimeField.START_TIME_PROPERTY.getKey(), "09:08:23");
        date.getConfig().put(DateTimeField.END_DATE_PROPERTY.getKey(), "2019-08-01");
        date.getConfig().put(DateTimeField.END_TIME_PROPERTY.getKey(), "19:28:23");
        date.getConfig().put(DateTimeField.DATETIME_TYPE_PROPERTY.getKey(), "CURRENT");

        Date dateValue = date.generateData();
        assertTrue(dateValue.after(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                           .parse("2019-01-01T09:08:23")), "After limit");
        assertTrue(dateValue.before(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                            .parse("2019-08-01T19:28:23")), "Before limit");
    }

    @Test
    void generateData_timeOnly() {
        DateTimeField field = new DateTimeField("time", Locale.CANADA_FRENCH);
        field.setType(DateType.TIME_ONLY);
        field.getConfig().put(DateTimeStringFormatter.OUTPUT_FORMAT_PROPERTY.getKey(), "HH:mm:ss");

        DateTimeStringFormatter formatter = new DateTimeStringFormatter();
        field.setFormatter(formatter);

        assertTrue(field.getData().toString().contains(":"));
    }

    @Test
    void generateData_dateOnly() {
        DateTimeField field = new DateTimeField("time", Locale.CANADA_FRENCH);
        field.setType(DateType.TIME_ONLY);
        field.getConfig().put(DateTimeStringFormatter.OUTPUT_FORMAT_PROPERTY.getKey(), "dd/MM/yyyy");

        DateTimeStringFormatter formatter = new DateTimeStringFormatter();
        field.setFormatter(formatter);

        assertTrue(field.getData().toString().matches("\\d{2}:\\d{2}:\\d{2}"));
    }

}