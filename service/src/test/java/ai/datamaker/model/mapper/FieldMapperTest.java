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

package ai.datamaker.model.mapper;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.mapper.FieldMapper;
import ai.datamaker.model.response.FieldResponse;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldMapperTest {

    @Test
    void fieldToFieldResponse() {
        Field field =  new StringField();
        field.setName("test");
        field.setDescription("test description");
        field.setLocale(Locale.US);
        Dataset dataset = new Dataset("dataset", Locale.getDefault());
        field.setDataset(dataset);

        FieldResponse response = FieldMapper.INSTANCE.fieldToFieldResponse(field);
        assertEquals("test", response.getName());
        assertEquals("test description", response.getDescription());
        assertEquals("en-US", response.getLanguageTag());
    }
}