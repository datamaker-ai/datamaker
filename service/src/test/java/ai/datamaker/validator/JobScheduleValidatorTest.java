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

package ai.datamaker.validator;

import ai.datamaker.validator.JobScheduleValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JobScheduleValidatorTest {

    private final JobScheduleValidator validator = new JobScheduleValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Test
    void isValid_blank() {
        assertFalse(validator.isValid("   ", context), "blank is invalid");
    }

    @Test
    void isValid_once() {
        assertTrue(validator.isValid("once", context), "once is valid");
    }

    @Test
    void isValid_random() {
        assertTrue(validator.isValid("random 5 10", context), "valid random pattern");
        assertFalse(validator.isValid("random foo bar", context), "invalid random pattern");
    }

    @Test
    @Deprecated
    void isValid_cron() {
        assertFalse(validator.isValid("*/5 * * * *", context), "valid cron pattern");
        assertFalse(validator.isValid("* * *", context), "invalid cron pattern");
    }
}