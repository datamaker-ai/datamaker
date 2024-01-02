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
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Date", localizationKey = "field.group.primitive.date", group = FieldGroup.PRIMITIVE)
public class DateTimeField extends Field<Date> {

    public static final String DEFAULT_OUTPUT_FORMAT = "dd/MM/yyyy";

    public static final PropertyConfig TIMEZONE_PROPERTY =
        new PropertyConfig("field.datetime.timezone",
                            "Timezone",
                            PropertyConfig.ValueType.STRING,
                            "UTC",
                            ZoneId.getAvailableZoneIds());

    public static final PropertyConfig DATETIME_TYPE_PROPERTY =
        new PropertyConfig("field.datetime.type",
                        "Datetime type",
                        PropertyConfig.ValueType.STRING,
                        DateType.CURRENT.toString(),
                        Arrays.stream(DateType.values()).map(DateType::toString).collect(Collectors.toList()));

    public static final PropertyConfig AT_MOST_PROPERTY =
            new PropertyConfig("field.datetime.atmost",
                               "At most (used with past and future option)",
                               PropertyConfig.ValueType.NUMERIC,
                               365,
                               Collections.emptyList());

    public static final PropertyConfig TIME_UNIT_PROPERTY =
            new PropertyConfig("field.datetime.time.unit",
                               "Time unit",
                               PropertyConfig.ValueType.STRING,
                               TimeUnit.DAYS.toString(),
                               Arrays.stream(TimeUnit.values()).map(TimeUnit::toString).collect(Collectors.toList()));

    public static final PropertyConfig START_DATE_PROPERTY =
            new PropertyConfig("field.datetime.start.date",
                               "Start date",
                               PropertyConfig.ValueType.DATE,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig END_DATE_PROPERTY =
            new PropertyConfig("field.datetime.end.date",
                               "End date",
                               PropertyConfig.ValueType.DATE,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig START_TIME_PROPERTY =
            new PropertyConfig("field.datetime.start.time",
                               "Start time",
                               PropertyConfig.ValueType.TIME,
                               "00:00:00",
                               Collections.emptyList());

    public static final PropertyConfig END_TIME_PROPERTY =
            new PropertyConfig("field.datetime.end.time",
                               "End time",
                               PropertyConfig.ValueType.TIME,
                               "23:59:59",
                               Collections.emptyList());

    public DateTimeField(String name, Locale locale) {
        super(name, locale);
    }

    public enum DateType {
        DATE_OF_BIRTH, PAST, FUTURE, CURRENT, DATE_ONLY, TIME_ONLY;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            TIMEZONE_PROPERTY,
            DATETIME_TYPE_PROPERTY,
            START_DATE_PROPERTY,
            START_TIME_PROPERTY,
            END_DATE_PROPERTY,
            END_TIME_PROPERTY,
            TIME_UNIT_PROPERTY,
            AT_MOST_PROPERTY
        );
    }

    @Override
    protected Date generateData() {

        DateType type = DateType.valueOf((String) config.getConfigProperty(DATETIME_TYPE_PROPERTY));
        ZoneId timezone = ZoneId.of((String) config.getConfigProperty(TIMEZONE_PROPERTY));
        int atMostUnit = (int) config.getConfigProperty(AT_MOST_PROPERTY);
        TimeUnit timeUnit = TimeUnit.valueOf((String) config.getConfigProperty(TIME_UNIT_PROPERTY));

        switch (type) {
            case DATE_OF_BIRTH:
                return faker.date().birthday();
            case PAST:
                return faker.date().past(atMostUnit, timeUnit);
            case FUTURE:
                return faker.date().future(atMostUnit, timeUnit);
            case CURRENT:
                return getDate();
            case DATE_ONLY:
                setOutputFormat("dd/MM/yyyy");
                return getDate();
            case TIME_ONLY:
                setOutputFormat("HH:mm:ss");
                if (config.containsKey(START_TIME_PROPERTY.getKey()) && config.containsKey(END_TIME_PROPERTY.getKey())) {
                    return getDate();
                }
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), timezone);
        return Date.from(zonedDateTime.toInstant());
    }

    private Date getDate() {
        String startDate = (String) config.getConfigProperty(START_DATE_PROPERTY);
        String endDate = (String) config.getConfigProperty(END_DATE_PROPERTY);
        String startTime = (String) config.getConfigProperty(START_TIME_PROPERTY);
        String endTime = (String) config.getConfigProperty(END_TIME_PROPERTY);
        ZoneId timezone = ZoneId.of((String) config.getConfigProperty(TIMEZONE_PROPERTY));

        LocalDate now = LocalDate.now();
        if (StringUtils.isBlank(startDate)) {
            startDate = now.getYear() - 1 + "-01-01";
        }
        if (StringUtils.isBlank(endDate)) {
            endDate = now.getYear() + "-" + String.format("%02d", now.getMonthValue()) + "-" +  String.format("%02d", now.getDayOfMonth());
        }

        LocalDateTime zonedStartDate = LocalDateTime.parse(startDate + "T" + startTime, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime zonedEndDate = LocalDateTime.parse(endDate + "T" + endTime, DateTimeFormatter.ISO_DATE_TIME);

        return faker.date().between(Date.from(zonedStartDate.toInstant(ZoneOffset.UTC)),
                                    Date.from(zonedEndDate.toInstant(ZoneOffset.UTC)));
    }

    public void setType(DateType dateOnly) {
        config.put(DATETIME_TYPE_PROPERTY.getKey(), dateOnly.toString());
    }

    public void setTimezone(ZoneId zone) {
        config.put(TIMEZONE_PROPERTY.getKey(), zone.toString());
    }

    public void setOutputFormat(String outputFormat) {
        config.put(DateTimeStringFormatter.OUTPUT_FORMAT_PROPERTY.getKey(), outputFormat);
        setFormatterClassName(DateTimeStringFormatter.class.getName());
    }

}
