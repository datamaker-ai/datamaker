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

package ai.datamaker.controller;

import ai.datamaker.service.CachingService;
import ai.datamaker.service.HibernateSearchService;
import ai.datamaker.service.TranslatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TranslatorService translatorService;

    @Autowired
    private HibernateSearchService searchService;

    @Autowired
    private CachingService cachingService;

    @GetMapping("/clearAllCaches")
    public void clearAllCaches() {
        cachingService.evictAllCaches();
    }

    @GetMapping(path = "/backup")
    public void backupDatabase() {

        // TODO support mysql
        // TODO configure path
        jdbcTemplate.execute(String.format("BACKUP TO '~/backup-%s.zip'", System.currentTimeMillis()));
    }

    @GetMapping(path = "/search-index/rebuild")
    public void rebuildSearchIndex() {
        searchService.forceRefresh();
    }

    @GetMapping(path = "/generate/error")
    public void generateError() {
        throw new IllegalStateException("Error");
    }

    // TODO migrate config

    // TODO migrate db

}
