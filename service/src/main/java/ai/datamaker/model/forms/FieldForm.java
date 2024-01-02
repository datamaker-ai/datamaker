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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class FieldForm {
    @NotBlank(message = "{name.mandatory}")
    private String name;

    private String description;

    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}")
    private String datasetId;

    private String externalId;

    @NotBlank
    @Pattern(regexp = "ca\\.breakpoints\\.datamaker\\.model\\.field\\.type.*")
    private String className;

    @Pattern(regexp = "(|None|ca\\.breakpoints\\.datamaker\\.model\\.field\\.formatter.*)")
    private String formatterClassName;

    @NotBlank
    @Pattern(regexp = "(?:[a-zA-Z]{2,3}(?:-[a-zA-Z]{2,3}){0,3})")
    private String languageTag;

    private Boolean isNullable = false;

    private String nullValue = "";

    @Positive
    private Integer position = 0;

    private Boolean isAttribute = false;

    private Boolean isAlias = false;

    private Boolean isPrimaryKey = false;

    private Boolean isNested = false;

    private Map<String, Object> config = Maps.newHashMap();
}
