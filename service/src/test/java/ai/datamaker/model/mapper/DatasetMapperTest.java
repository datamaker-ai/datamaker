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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.mapper.DatasetMapper;
import ai.datamaker.model.response.DatasetResponse;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DatasetMapperTest {

    @Test
    void datasetToDatasetResponse() {
        Dataset dataset = new Dataset();
        dataset.setName("test");
        dataset.setDescription("test description");
        dataset.setLocale(Locale.US);
        Workspace workspace = new Workspace();
        workspace.setName("test");
        dataset.setWorkspace(workspace);

        DatasetResponse response = DatasetMapper.INSTANCE.datasetToDatasetResponse(dataset);
        assertEquals("test", response.getName());
        assertEquals(dataset.getDateCreated(), response.getDateCreated());
        assertEquals("test description", response.getDescription());
        assertEquals("en-US", response.getLanguageTag());
    }
}