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

import ai.datamaker.model.Constants;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.support.CronSequenceGenerator;

public class JobScheduleValidator implements ConstraintValidator<JobScheduleConstraint, String> {
 
    @Override
    public void initialize(JobScheduleConstraint contactNumber) {
    }
 
    @Override
    public boolean isValid(String sequence, ConstraintValidatorContext cxt) {
        if (StringUtils.isBlank(sequence)) {
            return false;
        }

        if (Constants.SCHEDULE_ONCE.equals(sequence)) {
            return true;
        }

        if (sequence.startsWith("random")) {
            return sequence.matches("random \\d+ \\d+");
        }

        return CronSequenceGenerator.isValidExpression(sequence);
    }
 
}