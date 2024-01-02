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

package ai.datamaker.model.forms;

import ai.datamaker.validator.JobScheduleConstraint;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class GenerateDataJobForm {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}")
    private String workspaceId;

    private String description;

    private Long numberOfRecords = 10L;

    private Boolean randomizeNumberOfRecords = false;

    private Boolean streamForever;

    @NotBlank
    //@Pattern(regexp = "(random \\d+ \\d+|once|(((\\d+,)+\\d+|(\\d+(\\/|-)\\d+)|\\d+|\\*|\\?) ?){5,7})")
    @JobScheduleConstraint
    private String schedule;

    private Boolean runStatus;

    private Long size;

    private Boolean useBuffer = false;

    private Integer threadPoolSize;

    private Integer bufferSize = 1024;

    private Boolean flushOnEveryRecord = true;

    private Boolean replayable = false;

    private Integer replayHistorySize = 10;

}
