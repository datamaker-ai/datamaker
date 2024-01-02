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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;

import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.TextField.TextType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TextFieldTest {

    @Test
    void generateData_word() {
        TextField field = Mockito.spy(TextField.class);
        doReturn("test").when(field).getWordForLocale(any(), anyInt());

        String value = field.generateData();

        assertEquals("test", value);
    }

    @Test
    void generateData_words() {
        TextField field = Mockito.spy(TextField.class);
        doReturn("test", "test1", "test2", "test3").when(field).getWordForLocale(any());
        field.setLength(100);
        field.setType(TextType.WORDS);

        String value = field.generateData();

        assertEquals(16, value.split(" ").length);
    }

    @Test
    void generateData_sentence() {
        TextField field = Mockito.spy(TextField.class);
        doReturn("test", "test1", "test2", "test3").when(field).getWordForLocale(any());
        field.setLength(125);
        field.setType(TextType.SENTENCES);

        String value = field.generateData();

        assertTrue(Character.isUpperCase(value.charAt(0)));
        assertEquals('.', value.charAt(value.length() - 1));
    }

    @Test
    void generateData_paragraph() {
        TextField field = Mockito.spy(TextField.class);
        doReturn("test", "test1", "test2", "test3").when(field).getWordForLocale(any());
        field.setLength(1024);
        field.setType(TextType.PARAGRAPHS);

        String value = field.generateData();

        assertTrue(Character.isUpperCase(value.charAt(0)));
        assertEquals(2, StringUtils.countMatches(value, "\n"));
    }

}