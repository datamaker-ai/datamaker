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

package ai.datamaker.config;

import ai.datamaker.service.FieldDetectorService;
import ai.datamaker.service.HibernateSearchService;
import ai.datamaker.service.JobSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupApplicationListener {

    @Autowired
    private JobSchedulerService jobSchedulerService;

    @Autowired
    private HibernateSearchService hibernateSearchService;

    @Autowired
    private FieldDetectorService fieldDetectorService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting up");
        jobSchedulerService.init();
        hibernateSearchService.initializeHibernateSearch();
        fieldDetectorService.init();
    }
}