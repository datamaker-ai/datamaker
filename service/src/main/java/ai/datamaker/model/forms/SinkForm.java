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

import ai.datamaker.model.JobConfig;
import javax.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class SinkForm {

    @NotBlank
    @Pattern(regexp = "ca\\.breakpoints\\.datamaker\\.sink.*")
    private String sinkClassName;

    private JobConfig config;

}
