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

package ai.datamaker.utils.scheduling;

import ai.datamaker.utils.scheduling.RandomTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTriggerTest {

    @Test
    void nextExecutionTime() {
        Date startDate = new Date();

        RandomTrigger trigger = new RandomTrigger();

        SimpleTriggerContext triggerContext = new SimpleTriggerContext(startDate, new Date(), new Date());
        Date endDate = trigger.nextExecutionTime(triggerContext);

        long duration  = endDate.getTime() - startDate.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        assertNotNull(endDate);
        assertTrue(diffInMinutes >= 10 && diffInMinutes <= 60, "Date should be between 10 minutes and 60 minutes from now");
    }

    @Test
    void nextExecutionTime_noPreviousSchedule() {
        RandomTrigger trigger = new RandomTrigger();

        SimpleTriggerContext triggerContext = new SimpleTriggerContext();
        Date date = trigger.nextExecutionTime(triggerContext);

        Date afterDate = Timestamp.valueOf(LocalDateTime.now().minusMinutes(1));
        Date beforeDate = Timestamp.valueOf(LocalDateTime.now().plusMinutes(1));

        assertNotNull(date);
        assertTrue(date.after(afterDate) && date.before(beforeDate), "Date should be between 1 minutes from now");
    }
}