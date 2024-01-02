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

import ai.datamaker.model.Workspace;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WorkspaceForm {
    @NotBlank(message = "{name.mandatory}")
    private String name;

    private String description;

    @NotBlank
    private String owner;

    private String group;

    private Workspace.WorkspacePermissions groupPermissions = Workspace.WorkspacePermissions.NONE;
}
