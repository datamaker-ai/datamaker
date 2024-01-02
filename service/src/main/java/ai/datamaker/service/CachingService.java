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

package ai.datamaker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CachingService {

    @Autowired
    private CacheManager cacheManager;

    public void putToCache(String cacheName, String key, String value) {
        cacheManager.getCache(cacheName).put(key, value);
    }

    public String getFromCache(String cacheName, String key) {
        String value = null;
        if (cacheManager.getCache(cacheName).get(key) != null) {
            value = cacheManager.getCache(cacheName).get(key).get().toString();
        }
        return value;
    }

    @CacheEvict(value = "first", key = "#cacheKey")
    public void evictSingleCacheValue(String cacheKey) {
    }

    @CacheEvict(value = "first", allEntries = true)
    public void evictAllCacheValues() {
    }

    public void evictSingleCacheValue(String cacheName, String cacheKey) {
        cacheManager.getCache(cacheName).evict(cacheKey);
    }

    public void evictAllCacheValues(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }

    public void evictAllCaches() {
        cacheManager.getCacheNames()
          .parallelStream()
          .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void evictAllCachesAtIntervals() {
        evictAllCaches();
    }
}