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

import ai.datamaker.model.User;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.mapper.WorkspaceMapper;
import ai.datamaker.model.response.WorkspaceResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceMapperTest {

    @Test
    void workspaceToWorkspaceResponse() {

        Workspace workspace = new Workspace();
        workspace.setName("test");
        workspace.setDescription("test description");

        User user = new User();
        user.setUsername("user");
        workspace.setOwner(user);

        WorkspaceResponse response = WorkspaceMapper.INSTANCE.workspaceToWorkspaceResponse(workspace);
        assertEquals("test", response.getName());
        assertEquals("user", response.getOwner());
        assertEquals(workspace.getDateCreated(), response.getDateCreated());
        assertEquals("test description", response.getDescription());
        assertEquals("NONE", response.getGroupPermissions());
    }
}