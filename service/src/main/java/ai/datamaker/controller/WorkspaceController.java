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

import ai.datamaker.exception.InvalidParameterException;
import ai.datamaker.model.Constants;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.WorkspaceForm;
import ai.datamaker.model.mapper.DatasetMapper;
import ai.datamaker.model.mapper.GenerateDataJobMapper;
import ai.datamaker.model.mapper.WorkspaceMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.model.response.WorkspaceResponse;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.UserGroupRepository;
import ai.datamaker.repository.UserRepository;
import ai.datamaker.repository.WorkspaceRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController extends AbstractRestController {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private GenerateDataJobRepository generateDataJobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private BuildProperties buildProperties;

    @PostMapping(path = "/move/{workspaceFromId}/{workspaceToId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> moveAllDatasetFrom(@PathVariable @NotBlank String workspaceFromId,
                                                           @PathVariable @NotBlank String workspaceToId) {

        Workspace workspaceFrom = workspaceRepository.findByExternalId(UUID.fromString(workspaceFromId)).orElseThrow();
        authorize(workspaceFrom, false);

        Workspace workspaceTo = workspaceRepository.findByExternalId(UUID.fromString(workspaceToId)).orElseThrow();
        authorize(workspaceTo, true);

        Iterator<Dataset> iterator = workspaceFrom.getDatasets().iterator();
        while (iterator.hasNext()) {
            Dataset dataset = iterator.next();
            workspaceTo.addDataset(dataset);
            iterator.remove();
        }
        workspaceRepository.save(workspaceFrom);
        workspaceRepository.save(workspaceTo);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceTo.getExternalId().toString())
                                         .objectType(Constants.WORKSPACE_OBJECT)
                                         // .payload(workspace)
                                         .build());
    }

    /**
     * @deprecated use DatasetController:update
     * @param datasetId
     * @param workspaceToId
     * @return
     */
    @Deprecated
    @PostMapping(path = "/move/dataset/{datasetId}/{workspaceToId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> moveDatasetFrom(@PathVariable @NotBlank String datasetId,
                                                       @PathVariable @NotBlank String workspaceToId) {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(datasetId)).orElseThrow();
        Workspace workspaceFrom = dataset.getWorkspace();

        authorize(workspaceFrom, false);

        Workspace workspaceTo = workspaceRepository.findByExternalId(UUID.fromString(workspaceToId)).orElseThrow();
        authorize(workspaceTo, true);

        workspaceFrom.removeDataset(dataset);
        workspaceTo.getDatasets().add(dataset);
        workspaceRepository.save(workspaceTo);
        workspaceRepository.save(workspaceFrom);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceTo.getExternalId().toString())
                                         .objectType(Constants.WORKSPACE_OBJECT)
                                         // .payload(workspace)
                                         .build());
    }

    @GetMapping(path = "/{workspaceId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String workspaceId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, false);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(workspace.getExternalId().toString())
            .objectType(Constants.WORKSPACE_OBJECT)
            .payload(WorkspaceMapper.INSTANCE.workspaceToWorkspaceResponse(workspace))
            .build());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list(@RequestParam(name = "userId", required = false) String userId,
                                            @RequestParam(name = "userGroupId", required = false) String userGroupId) {

        List<WorkspaceResponse> workspaces = StreamSupport
            .stream(getIterator(userId, userGroupId), false)
            .filter(w -> isAuthorized(w, false))
            .map(WorkspaceMapper.INSTANCE::workspaceToWorkspaceResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.WORKSPACE_OBJECT)
            .payload(workspaces)
            .build());
    }

    Spliterator<Workspace> getIterator(String userId, String userGroupId) {
        if (StringUtils.isNotBlank(userId)) {
            Optional<User> user = userRepository.findByExternalId(UUID.fromString(userId));
            if (user.isPresent()) {
                return workspaceRepository.findAllByOwner(user.get()).spliterator();
            }
        }
        if (StringUtils.isNotBlank(userGroupId)) {
            Optional<UserGroup> userGroup = userGroupRepository.findByExternalId(UUID.fromString(userGroupId));
            if (userGroup.isPresent()) {
                return workspaceRepository.findAllByUserGroup(userGroup.get()).spliterator();
            }
        }

        return workspaceRepository.findAll().spliterator();
    }

    @GetMapping(path = "/{workspaceId}/datasets")
    @ResponseBody
    public ResponseEntity<ApiResponse> listDatasetForWorkspace(@PathVariable @NotBlank String workspaceId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, false);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.DATASET_OBJECT)
                                         .payload(workspace
                                                          .getDatasets()
                                                          .stream()
                                                          .map(DatasetMapper.INSTANCE::datasetToDatasetResponse)
                                                          .collect(Collectors.toList()))
                                         .build());
    }

    @GetMapping(path = "/{workspaceId}/data-jobs")
    @ResponseBody
    public ResponseEntity<ApiResponse> listDataGenerationJobs(@PathVariable @NotBlank String workspaceId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, false);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .payload(workspace
                                                          .getDataJobs()
                                                          .stream()
                                                          .map(GenerateDataJobMapper.INSTANCE::generateDataJobToGenerateDataJobResponse))
                                         .build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody final WorkspaceForm workspaceForm) {
        if ("DEMO".equalsIgnoreCase(buildProperties.get("profile"))) {
            if (workspaceRepository.count() >= 2) {
                throw new IllegalArgumentException("Workspace limit reached for demo version");
            }
        }

        User user = userRepository.findByUsername(workspaceForm.getOwner());
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        Workspace workspace = WorkspaceMapper.INSTANCE.workspaceFormToWorkspace(workspaceForm);
        workspace.setOwner(user);

        if (StringUtils.isNotBlank(workspaceForm.getGroup())) {
            UserGroup userGroup = userGroupRepository.findByExternalId(UUID.fromString(workspaceForm.getGroup())).orElseThrow();
            workspace.setUserGroup(userGroup);
        }

        if (workspaceRepository.findByNameAndUserGroupOrNameAndOwnerAndOwnerIsNotNull(workspaceForm.getName(),
                                                                     workspace.getUserGroup(),
                                                                     workspace.getName(),
                                                                     user).size() > 0) {
            throw new InvalidParameterException("name.already.exists", workspaceForm.getName());
        }

        workspace = workspaceRepository.save(workspace);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(workspace.getExternalId().toString())
            .objectType(Constants.WORKSPACE_OBJECT)
            .payload(WorkspaceMapper.INSTANCE.workspaceToWorkspaceResponse(workspace))
            .build());
    }

    @DeleteMapping(path = "/{workspaceId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String workspaceId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, true);

        // Delete all datasets / fields?
        // Cascade == ALL
        workspaceRepository.delete(workspace);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(workspace.getExternalId().toString())
            .objectType(Constants.WORKSPACE_OBJECT)
            .build());
    }

    @PutMapping(path = "/{workspaceId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String workspaceId, @Valid @RequestBody final WorkspaceForm workspaceForm) {

        Workspace workspaceFound = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspaceFound, true);

        User user = userRepository.findByUsername(workspaceForm.getOwner());
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        workspaceFound.setName(workspaceForm.getName());
        workspaceFound.setDescription(workspaceForm.getDescription());
        workspaceFound.setGroupPermissions(workspaceForm.getGroupPermissions());
        workspaceFound.setDateModified(new Date());
        workspaceFound.setOwner(user);
        if (StringUtils.isNotBlank(workspaceForm.getGroup())) {
            UserGroup userGroup = userGroupRepository.findByExternalId(UUID.fromString(workspaceForm.getGroup())).orElseThrow();
            workspaceFound.setUserGroup(userGroup);
        }
        Workspace workspaceResponse = workspaceRepository.save(workspaceFound);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceResponse.getExternalId().toString())
                                         .objectType(Constants.WORKSPACE_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{workspaceId}/owner/{ownerId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeOwner(@PathVariable @NotBlank String workspaceId, @PathVariable @NotBlank String ownerId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, true);

        User user = userRepository.findByExternalId(UUID.fromString(ownerId)).orElseThrow();

        workspace.setOwner(user);
        workspace.setDateModified(new Date());
        Workspace workspaceResponse = workspaceRepository.save(workspace);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceResponse.getExternalId().toString())
                                         .objectType(Constants.WORKSPACE_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{workspaceId}/group/{userGroupId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeUserGroup(@PathVariable @NotBlank String workspaceId, @PathVariable @NotBlank String userGroupId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, true);

        UserGroup group = userGroupRepository.findByExternalId(UUID.fromString(userGroupId)).orElseThrow();

        workspace.setUserGroup(group);
        workspace.setDateModified(new Date());
        Workspace workspaceResponse = workspaceRepository.save(workspace);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceResponse.getExternalId().toString())
                                         .objectType(Constants.WORKSPACE_OBJECT)
                                         .build());
    }

}
