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
import ai.datamaker.model.Dataset;
import ai.datamaker.model.Searchable;
import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldMapping;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.model.response.SearchResultResponse;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.service.HibernateSearchService;
import ai.datamaker.sink.SinkConfiguration;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";

    @Autowired
    private HibernateSearchService searchService;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Value("${search.service.thread.pool.size}")
    private Integer threadPoolSize = 10;

    private final ExecutorService workerThreadPool = Executors.newFixedThreadPool(10);

    @GetMapping("/uuid")
    public ResponseEntity<ApiResponse> uuid(@RequestParam("query") String uuid) throws Exception {
        Object searchQuery = uuid;
        if (uuid.matches(UUID_REGEX)) {
            searchQuery = UUID.fromString(uuid);
        }

        Future<List<Field>> fields = searchService.search(searchQuery, Field.class, "externalId");
        List<SearchResultResponse> searchResultResponses = Lists.newArrayList();

        createSearchResultResponses(searchResultResponses, fields, "name", "sinkClassName", "externalId");

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.SEARCH_OBJECT)
                                         .payload(searchResultResponses)
                                         .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> search(@RequestParam("query") String searchTerm) throws Exception {

        Object searchQuery = searchTerm;
//        if (searchTerm.matches(UUID_REGEX)) {
//            searchQuery = UUID.fromString(searchTerm);
//        }

        // In parallel then join results
        List<SearchResultResponse> searchResultResponses = Lists.newArrayList();

        Future<List<GenerateDataJob>> generateDataJobs = searchService.search(searchQuery, GenerateDataJob.class, "name", "description", "externalId");
        Future<List<JobExecution>> jobExecutions = searchService.search(searchQuery, JobExecution.class, "results", "errors", "externalId");
        Future<List<Field>> fields = searchService.search(searchQuery, Field.class, "name", "description", "externalId", "className");
        Future<List<FieldMapping>> fieldMappings = searchService.search(searchQuery, FieldMapping.class, "mappingKey", "fieldJson", "externalId");
        Future<List<Dataset>> datasets = searchService.search(searchQuery, Dataset.class, "name", "description", "externalId", "tags");
        Future<List<Workspace>> workspaces = searchService.search(searchQuery, Workspace.class, "name", "description", "externalId");
        Future<List<UserGroup>> groups = searchService.search(searchQuery, UserGroup.class, "name", "description", "externalId");
        Future<List<User>> users = searchService.search(searchQuery, User.class, "username", "firstName", "lastName", "externalId");
        Future<List<SinkConfiguration>> sinks = searchService.search(searchQuery, SinkConfiguration.class, "name", "sinkClassName", "externalId");

        createSearchResultResponses(searchResultResponses, generateDataJobs, "name", "description", "externalId");
        createSearchResultResponses(searchResultResponses, jobExecutions, "results", "errors", "externalId");
        createSearchResultResponses(searchResultResponses, fields, "name", "description", "externalId", "className");
        createSearchResultResponses(searchResultResponses, fieldMappings, "mappingKey", "fieldJson", "externalId");
        createSearchResultResponses(searchResultResponses, datasets, "name", "description", "externalId", "tags");
        createSearchResultResponses(searchResultResponses, workspaces, "name", "description", "externalId");
        createSearchResultResponses(searchResultResponses, groups, "name", "description", "externalId");
        createSearchResultResponses(searchResultResponses, users, "username", "firstName", "lastName", "externalId");
        createSearchResultResponses(searchResultResponses, sinks, "name", "sinkClassName", "externalId");

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.SEARCH_OBJECT)
            .payload(searchResultResponses)
            .build());
    }

    private <S extends Searchable> void createSearchResultResponses(List<SearchResultResponse> searchResultResponses,
                                             Future<List<S>> searchables,
                                             String... keys) throws ExecutionException, InterruptedException {
        searchables.get().forEach(s -> searchResultResponses.add(
                SearchResultResponse
                        .builder()
                        .name(s.getName())
                        .url(buildUrl(s))
                        .fields(setFields(s, keys))
                        .externalId(s.getExternalId().toString())
                        .type(s.getClass().getSimpleName())
                        .build()));
    }

    private Map<String, Object> setFields(Searchable searchable, String... keys) {
        Map<String, Object> fields = new TreeMap<>();
        Arrays.stream(keys).forEach(k -> {
            try {
                java.lang.reflect.Method method = ReflectionUtils.findMethod(searchable.getClass(), "get" + StringUtils.capitalize(k));
                if (method != null) {
                    fields.put(k, method.invoke(searchable));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return fields;
    }

    private String buildUrl(Searchable s) {
        String type = s.getClass().getSimpleName();
        if (s instanceof Field) {
            Field field = (Field)s;
            if (field.getIsNested()) {
                // Handle nested logic
                return "/datasets/" + field.getDataset().getExternalId().toString() + "/fields/" + s.getExternalId() + "?name=" + field.getDataset().getName();
            }
            return "/datasets/fields/" + field.getDataset().getExternalId().toString() + "?name=" + field.getDataset().getName();
        }
        switch (type) {
            case "User":
                return "/users/edit/" + s.getExternalId();
            case "UserGroup":
                return "/users/groups/edit/" + s.getExternalId();
            case "GenerateDataJob":
                return "/jobs/edit/" + s.getExternalId();
            case "JobExecution":
                JobExecution jobExecution = (JobExecution)s;
                return "/jobs/" + jobExecution.getDataJob().getExternalId().toString() + "/logs";
            case "Dataset":
                return "/datasets/edit/" + s.getExternalId();
            case "Workspace":
                return "/workspaces/edit/" + s.getExternalId() + "?name=" + s.getName();
            case "SinkConfiguration":
                return "/sinks/edit/" + s.getExternalId();
            case "Field":
                return "/fields";
            case "FieldMapping":
                return "/mappings/edit/" + s.getExternalId();
        }
        return "/";
    }
}
