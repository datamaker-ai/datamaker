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
import ai.datamaker.model.FileType;
import ai.datamaker.model.ResourceFile;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileSystemController extends AbstractRestController {

    @Value("${application.config.path}")
    private String configPath;

    @PostConstruct
    public void init() throws IOException {
        if (Files.notExists(Paths.get(configPath))) {
            Files.createDirectories(Paths.get(configPath));
        }
        Arrays.stream(FileType.values())
            .filter(ft -> Files.notExists(Paths.get(configPath, ft.name().toLowerCase())))
            .forEach(ft -> {
                try {
                    Files.createDirectory(Paths.get(configPath, ft.name().toLowerCase()));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
    }

    @PostMapping("/generate-from-content")
    @ResponseBody
    public ResponseEntity<ApiResponse> createFromContent(@RequestParam("type") FileType type,
                                                         @RequestParam("content") String content,
                                                         @RequestParam("name") String name) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get(configPath, type.name().toLowerCase(), name).toFile());
        fileWriter.write(content);
        fileWriter.close();

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.FILE_OBJECT)
                                         .build());
    }

    @PostMapping("/generate-from-file")
    @ResponseBody
    public ResponseEntity<ApiResponse> createFromFile(@RequestParam("type") FileType type,
                                                      @RequestParam(name = "filename", required = false) String filename,
                                                      @RequestParam("file") MultipartFile file) throws IOException {
        Files.copy(file.getInputStream(), Paths.get(configPath,
                                                    type.name().toLowerCase(),
                                                    StringUtils.isNotBlank(filename) ? filename : file.getOriginalFilename()));

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.FILE_OBJECT)
                                         .build());
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@RequestParam("type") FileType type,
                                              @RequestParam("filename") String filename) throws IOException {

        Files.deleteIfExists(Path.of(configPath, type.name().toLowerCase(), filename));

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.FILE_OBJECT)
                                         .build());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list() throws IOException {
        // is admin
        List<ResourceFile> resourceFiles = Files
                .walk(Paths.get(configPath))
                .filter(p -> !Files.isDirectory(p))
                .map(
                    p -> ResourceFile
                    .builder()
                    .modified(getModificationDate(p))
                    .fileType(getFileType(p.getParent()))
                    .absolutePath(getRealPath(p))
                    .filename(p.getFileName().toString())
                    .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.FILE_OBJECT)
                                         .payload(resourceFiles)
                                         .build());
    }

    private String getRealPath(Path path) {
        try {
            return path.toRealPath().toString();
        } catch (IOException e) {
            return null;
        }
    }

    private Date getModificationDate(Path path) {
        try {
            return Date.from(Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return null;
        }
    }

    private FileType getFileType(Path parent) {
        switch (parent.getFileName().toString()) {
            case "jar":
                return FileType.JAR;
            case "jaas":
                return FileType.JAAS;
            case "keytab":
                return FileType.KEYTAB;
            case "jks":
                return FileType.JKS;
            case "resource":
                return FileType.RESOURCE;
        }
        return FileType.OTHER;
    }
}
