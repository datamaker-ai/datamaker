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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class DictionaryService {

    private Map<String, List<String>> dictionaryPerLanguage = new HashMap<>();

    private Map<String, List<String>> stopWordsPerLanguage = new HashMap<>();

    @Setter
    @Value("#{'${dictionary.supported.lang}'.split(',')}")
    private Set<String> supportedLanguages;

    @PostConstruct
    public void init() throws Exception {
        supportedLanguages.forEach(lang -> {
            try (
                InputStream inputStream = getClass().getResourceAsStream(lang + ".txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            ) {
                String line = null;
                List<String> words = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    words.add(line);
                }
                dictionaryPerLanguage.put(lang, words);

            } catch (IOException e) {
                log.error("Error while reading file: {}.txt", lang, e);
                throw new IllegalStateException(e);
            }
        });
    }

    public String getWordForLocale(Locale locale, boolean excludeStopWords) {
        return "";
    }

    public String getWordForLocale(Locale locale) {
        List<String> dictionary = dictionaryPerLanguage.get(locale.getLanguage());

        return dictionary.get(ThreadLocalRandom.current().nextInt(0, dictionary.size()));
    }

    public String getWordForLocale(Locale locale, int maxLength) {
        if (maxLength <= 3) {
            return RandomStringUtils.randomAlphabetic(maxLength);
        }

        List<String> dictionary = dictionaryPerLanguage.get(locale.getLanguage());

        int maxRetry = 1000;
        int currentCount = 0;
        while (currentCount++ < maxRetry) {
            String wordFound = dictionary.get(ThreadLocalRandom.current().nextInt(0, dictionary.size()));
            if (wordFound.length() <= maxLength) {
                return wordFound;
            }
        }

        return RandomStringUtils.randomAlphabetic(maxLength);
    }

    public List<String> getWordsForLocale(Locale locale, int totalLength) {
        return Collections.emptyList();
    }
}
