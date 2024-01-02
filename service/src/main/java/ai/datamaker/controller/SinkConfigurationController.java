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

package ai.datamaker.controller;

import ai.datamaker.model.Constants;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.SinkConfigurationForm;
import ai.datamaker.model.mapper.SinkConfigurationMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.model.response.SinkConfigurationResponse;
import ai.datamaker.repository.SinkConfigurationRepository;
import ai.datamaker.repository.WorkspaceRepository;
import ai.datamaker.sink.SinkConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/sinks")
public class SinkConfigurationController extends AbstractRestController {

    @Autowired
    private SinkConfigurationRepository sinkConfigurationRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String className,
                                            @RequestParam(required = false) String workspaceId) {

        List<SinkConfigurationResponse> sinkConfigurations = StreamSupport
                .stream(sinkConfigurationRepository.findAll().spliterator(), false)
                .filter(sc -> isAuthorized(sc.getWorkspace(), false))
                .filter(sc -> {
                    if (StringUtils.isNotBlank(className)) {
                        return sc.getSinkClassName().equals(className);
                    }
                    return true;
                })
                .filter(sc -> {
                    if (StringUtils.isNotBlank(workspaceId)) {
                        return sc.getWorkspace().getExternalId().toString().equals(workspaceId);
                    }
                    return true;
                })
                .map(SinkConfigurationMapper.INSTANCE::sinkConfigurationToSinkConfigurationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.SINK_CONFIGURATION_OBJECT)
                                         .payload(sinkConfigurations)
                                         .build());
    }

    @GetMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String externalId) {

        SinkConfiguration sinkConfiguration = sinkConfigurationRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(sinkConfiguration.getWorkspace(), false);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(sinkConfiguration.getExternalId().toString())
                                         .objectType(Constants.SINK_CONFIGURATION_OBJECT)
                                         .payload(SinkConfigurationMapper.INSTANCE.sinkConfigurationToSinkConfigurationResponse(sinkConfiguration))
                                         .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody SinkConfigurationForm sinkConfigurationForm) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(sinkConfigurationForm.getWorkspaceId())).orElseThrow();
        authorize(workspace, true);

        SinkConfiguration sinkConfiguration = SinkConfigurationMapper.INSTANCE.sinkConfigurationFormToSinkConfiguration(sinkConfigurationForm);
        sinkConfiguration.setWorkspace(workspace);
        sinkConfigurationRepository.save(sinkConfiguration);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(sinkConfiguration.getExternalId().toString())
                                         .objectType(Constants.SINK_CONFIGURATION_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{externalId}")
    public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String externalId, @Valid @RequestBody SinkConfigurationForm sinkConfigurationForm) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(sinkConfigurationForm.getWorkspaceId())).orElseThrow();
        authorize(workspace, true);

        SinkConfiguration sinkConfiguration = sinkConfigurationRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        SinkConfiguration updatedSinkConfiguration = SinkConfigurationMapper.INSTANCE.sinkConfigurationFormToSinkConfiguration(sinkConfigurationForm);
        updatedSinkConfiguration.setWorkspace(workspace);
        updatedSinkConfiguration.setId(sinkConfiguration.getId());
        updatedSinkConfiguration.setDateModified(new Date());
        sinkConfigurationRepository.save(updatedSinkConfiguration);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(updatedSinkConfiguration.getExternalId().toString())
                                         .objectType(Constants.SINK_CONFIGURATION_OBJECT)
                                         .build());
    }

    @DeleteMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String externalId) {
        SinkConfiguration sinkConfiguration = sinkConfigurationRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(sinkConfiguration.getWorkspace(), true);

        sinkConfigurationRepository.delete(sinkConfiguration);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(sinkConfiguration.getExternalId().toString())
                                         .objectType(Constants.SINK_CONFIGURATION_OBJECT)
                                         .build());
    }
}
