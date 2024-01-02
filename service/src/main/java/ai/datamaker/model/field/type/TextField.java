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

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.DictionaryService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Generated text (locale based dictionary)", localizationKey = "field.group.text", group = FieldGroup.CUSTOM)
public class TextField extends Field<String> {

    static final PropertyConfig TEXT_TYPE_PROPERTY =
        new PropertyConfig("field.text.type",
            "Text type",
            PropertyConfig.ValueType.STRING,
            TextType.WORD.toString(),
            Arrays.stream(TextType.values()).map(TextType::toString).collect(Collectors.toList()));

    static final PropertyConfig LENGTH_PROPERTY =
        new PropertyConfig("field.text.length",
            "Length",
            ValueType.NUMERIC,
            25,
            Collections.emptyList());

    static final PropertyConfig EXCLUDE_STOP_WORDS =
        new PropertyConfig("field.text.exclude.stop.words",
            "Exclude stop words",
            ValueType.BOOLEAN,
            false,
            Collections.emptyList());

    public enum TextType {
        WORD, WORDS, SENTENCES, PARAGRAPHS
    }

    public TextField(String name, Locale locale) {
        super(name,
            locale);
    }

    public TextField(String name, Locale locale, TextType textType) {
        super(name,
              locale);
        setType(textType);
    }

    @Override
    protected String generateData() {

        int length = ((Number) config.getConfigProperty(LENGTH_PROPERTY)).intValue();

        TextType type = TextType.valueOf((String) config.getConfigProperty(TEXT_TYPE_PROPERTY));
        int maxSentenceLength = Math.min(length, 15 * 5);

        switch (type) {
            default:
            case WORD:
                return getWordForLocale(getLocale(), length);
            case WORDS:
                // average word length = 5
                return getSentence(length);
            case SENTENCES:
                // 5-20 words per sentence
                int numberOfSentences = ((length / 5) / 15) + 1;
                StringBuilder sentenceBuilder = new StringBuilder();
                for (int i = 0; i < numberOfSentences; i++) {
                    String word = getSentence(maxSentenceLength) + ".";
                    if (sentenceBuilder.length() + word.length() < length) {
                        sentenceBuilder.append(word);
                    }
                }
                return sentenceBuilder.toString();
            case PARAGRAPHS:
                // 100 to 150 words
                int numberOfParagraphs = ((length / 5) / 125) + 1;
                StringBuilder paragraphBuilder = new StringBuilder();
                for (int i = 0; i < numberOfParagraphs; i++) {

                    for (int j = 0; j < ThreadLocalRandom.current().nextInt(6, 10); j++) {
                        String word = getSentence(maxSentenceLength) + " ";
                        if (paragraphBuilder.length() + word.length() < length) {
                            paragraphBuilder.append(word);
                        } else {
                            paragraphBuilder.deleteCharAt(paragraphBuilder.length() - 1);
                            break;
                        }
                    }
                    if (paragraphBuilder.length() < length) {
                        paragraphBuilder.append("\n");
                    } else {
                        break;
                    }
                }
                return paragraphBuilder.toString();
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            TEXT_TYPE_PROPERTY,
            LENGTH_PROPERTY,
            EXCLUDE_STOP_WORDS);
    }

    public void setLength(int maxLength) {
        config.put(LENGTH_PROPERTY.getKey(), maxLength);
    }

    public int getLength() {
        return (int) config.get(LENGTH_PROPERTY.getKey());
    }

    public void setType(TextType type) {
        config.put(TEXT_TYPE_PROPERTY, type.toString());
    }

    private String getSentence(int length) {
        int numberOfWords = (length / 5) + 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numberOfWords; i++) {
            String word = getWordForLocale(getLocale()) + " ";
            if (builder.length() + word.length() < length) {
                builder.append(word);
            } else {
                builder.deleteCharAt(builder.length() - 1);
                break;
            }
        }
        // builder.append(".");
        return StringUtils.capitalize(builder.toString());
    }

    @VisibleForTesting
    String getWordForLocale(Locale locale) {
        DictionaryService dictionaryService = BeanService.getBean(DictionaryService.class);

        return dictionaryService.getWordForLocale(locale);
    }

    @VisibleForTesting
    String getWordForLocale(Locale locale, int maxLength) {
        DictionaryService dictionaryService = BeanService.getBean(DictionaryService.class);

        return dictionaryService.getWordForLocale(locale, maxLength);
    }

}
