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

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DatasetForm {
    @NotBlank
    @Pattern(regexp = "(?:[a-zA-Z]{2,3}(?:-[a-zA-Z]{2,3}){0,3})")
    private String languageTag;

    @NotBlank
    private String name;

    @NotBlank
    private String workspaceId;

    private String description;

    private String fieldFormatterId;

    private String constraintId;

    private Set<String> tags;

    private boolean exportHeader = true;

    private float nullablePercentLimit = 0.0f;

    private float duplicatesPercentLimit = 0.0f;

    private boolean allowDuplicates = true;

    private int numberOfRetries = 10;

}
