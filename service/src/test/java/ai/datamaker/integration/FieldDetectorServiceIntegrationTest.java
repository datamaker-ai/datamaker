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

import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.NameField;
import ai.datamaker.service.FieldDetectorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
public class FieldDetectorServiceIntegrationTest {

    @Autowired
    private FieldDetectorService fieldDetectorService;

    @Test
    void process() {
        Optional<Field> firstname = fieldDetectorService.detectTypeOnName("nome di battesimo", Locale.ITALIAN);

        assertTrue(firstname.isEmpty());

        Optional<Field> prenom = fieldDetectorService.detectTypeOnName("petitnom", Locale.FRANCE);

        prenom.ifPresentOrElse(f -> {
            assertEquals(Locale.FRANCE,f.getLocale());
            assertTrue(f instanceof NameField);
            },
                               () -> fail("not present"));
    }
}
