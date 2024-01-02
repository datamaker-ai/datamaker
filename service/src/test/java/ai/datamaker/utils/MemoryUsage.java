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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.field.type.StringField;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Runtime.getRuntime;

public class MemoryUsage {

    private static long getFreeMB() {
        return (getRuntime().totalMemory() - getRuntime().freeMemory()) / (1024 * 1024);
    }

    public static void main2(String[] args) {
        System.out.println(String.format("%s %d",
                                         "initial",
                                         getFreeMB()));
        List<List<Object>> objects = Lists.newArrayList();
        objects.add(Lists.newArrayList(10,
                                       true,
                                       RandomStringUtils.random(100),
                                       42.432f,
                                       789.43242d,
                                       "a",
                                       new BigInteger("4242"),
                                       new BigDecimal(42.43),
                                       new Date()));
        System.out.println(String.format("%s %d",
                                         "after 1",
                                         getFreeMB()));
        for (int i = 0; i < 1000; i++) {
            objects.add(Lists.newArrayList(10,
                                           true,
                                           RandomStringUtils.random(100),
                                           42.432f,
                                           789.43242d,
                                           "a",
                                           new BigInteger("4242"),
                                           new BigDecimal(42.43),
                                           new Date()));
        }
        System.out.println(String.format("%s %d",
                                         "after 1000",
                                         getFreeMB()));
        for (int i = 0; i < 1000000; i++) {
            objects.add(Lists.newArrayList(10,
                                           true,
                                           RandomStringUtils.random(100),
                                           42.432f,
                                           789.43242d,
                                           "a",
                                           new BigInteger("4242"),
                                           new BigDecimal(42.43),
                                           new Date()));
        }
        System.out.println(String.format("%s %d",
                                         "after 1001000",
                                         getFreeMB()));
        for (int i = 0; i < 10000000; i++) {
            if (i % 1000000 == 0) {
                System.out.println(String.format("%s %d",
                                                 String.valueOf(i + 1001000),
                                                 getFreeMB()));
            }
            objects.add(Lists.newArrayList(10,
                                           true,
                                           RandomStringUtils.random(100),
                                           42.432f,
                                           789.43242d,
                                           "a",
                                           new BigInteger("4242"),
                                           new BigDecimal(42.43),
                                           new Date()));
        }
    }

    public static void main(String[] args) {
        Dataset dataset = new Dataset();
        dataset.setThreadPoolSize(10);
        dataset.setNumberOfRecords(10000000L);
        SequenceField sequenceField = new SequenceField("id",
                                                        Locale.getDefault());
        dataset.addField(sequenceField);
        AddressField country = new AddressField("country",
                       Locale.forLanguageTag("en-US"));
        dataset.addField(country);
        dataset.addField(new StringField("test",
                                         Locale.getDefault()));
        dataset.addField(new FloatField("number",
                                        Locale.getDefault()));

        System.out.println(String.format("%s %d",
                                         "initial",
                                         getFreeMB()));

        final int i[] = {0};
        long lastTimestamp[] = {System.currentTimeMillis()};
        long lastCount[] = {0};
        long startTimestamp = System.currentTimeMillis();

        dataset.processAllValues((a) -> {
            long currentPeriod = (System.currentTimeMillis() - lastTimestamp[0]);
            if (currentPeriod > 10 * 1000L) {
                System.out.println(String.format("%d %d",
                                                 i[0],
                                                 (i[0]-lastCount[0])/(currentPeriod/1000)));
                lastTimestamp[0] = System.currentTimeMillis();
                lastCount[0] = i[0];
            }

            if (i[0]++ % 1000000 == 0) {
                System.out.println(String.format("%s %d",
                                                 String.valueOf(i[0]),
                                                 getFreeMB()));
            }
        });

        System.out.println(String.format("Took %d seconds", (System.currentTimeMillis() - startTimestamp)/1000));
    }

}
