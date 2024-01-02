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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.service.DictionaryService;
import com.google.common.collect.Sets;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DictionaryServiceTest {

    private DictionaryService dictionaryService;

    @BeforeEach
    public void init() throws Exception {
        dictionaryService = new DictionaryService();
        ReflectionTestUtils.setField(dictionaryService, "supportedLanguages", Sets.newHashSet("en"));
        dictionaryService.init();
    }

    @Test
    void getWordForLocale() {
        assertNotNull(dictionaryService.getWordForLocale(Locale.ENGLISH));
    }

    @Test
    void testGetWordForLocale() {
        assertTrue(dictionaryService.getWordForLocale(Locale.ENGLISH, 25).length() < 25);
    }
}