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

package ai.datamaker.integration;

import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.TextField.TextType;
import ai.datamaker.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
public class TextFieldIntegrationTest {

    @Autowired
    private DictionaryService dictionaryService;

    @Test
    void generateData_word() {
        TextField field = new TextField();
        field.setLength(20);

        String value = (String) field.getData();

        assertTrue(value.length() <= 20, "length below 20");
    }

    @Test
    void generateData_words() {
        TextField field = new TextField();
        field.setType(TextType.WORDS);
        field.setLength(100);

        String value = (String) field.getData();

        assertTrue(value.length() <= 100, "length below 100");
    }

    @Test
    void generateData_sentence() {
        TextField field = new TextField();
        field.setLength(125);
        field.setType(TextType.SENTENCES);

        String value = (String) field.getData();

        assertTrue(Character.isUpperCase(value.charAt(0)));
        assertTrue(value.length() <= 125, "length below 125");
    }

    @Test
    void generateData_paragraph() {
        TextField field = new TextField();
        field.setLength(1024);
        field.setType(TextType.PARAGRAPHS);

        String value = (String) field.getData();

        assertTrue(Character.isUpperCase(value.charAt(0)));
        assertTrue(value.length() <= 1024, "length below 1024");
    }
}
