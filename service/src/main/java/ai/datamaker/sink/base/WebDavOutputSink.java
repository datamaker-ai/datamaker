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

package ai.datamaker.sink.base;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@DataOutputSinkType(compressed = true, encrypted = true)
public class WebDavOutputSink implements DataOutputSink {

    public static final PropertyConfig WEBDAV_HOSTNAME
            = new PropertyConfig("webdav.sink.hostname",
                                 "Hostname",
                                 PropertyConfig.ValueType.STRING,
                                 "remote",
                                 Collections.emptyList());

    public static final PropertyConfig WEBDAV_PORT
            = new PropertyConfig("webdav.sink.port",
                                 "Port",
                                 PropertyConfig.ValueType.NUMERIC,
                                 80,
                                 Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("webdav.sink.output.filename",
                                 "Output file path",
                                 PropertyConfig.ValueType.EXPRESSION,
                                 "\"/\" + #dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                 Collections.emptyList());

    public static final PropertyConfig WEBDAV_USERNAME =
            new PropertyConfig("webdav.sink.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig WEBDAV_PASSWORD =
            new PropertyConfig("webdav.sink.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }
    //    Provides access to files on a WebDAV server through the modules commons-vfs2-jackrabbit1 and commons-vfs2-jackrabbit2.
//
//            URI Format
//
//    webdav://[ username[: password]@] hostname[: port][ absolute-path]
//
//    File System Options
//
//    versioning true if versioning should be enabled
//    creatorName the user name to be identified with changes to a file. If not set the user name used to authenticate will be used.
//
//    Examples
//
//    webdav://somehost:8080/dist
    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder
                .append("webdav://");

        if (config.containsKey(WEBDAV_USERNAME.getKey())) {
            String username = (String) config.getConfigProperty(WEBDAV_USERNAME);
            pathBuilder.append(username);
        }
        if (config.containsKey(WEBDAV_PASSWORD.getKey())) {
            String password = (String) config.getConfigProperty(WEBDAV_PASSWORD);
            pathBuilder.append(":").append(password).append("@");
        }
        pathBuilder.append(config.getConfigProperty(WEBDAV_HOSTNAME));
        if (config.containsKey(WEBDAV_PORT.getKey())) {
            int port = (Integer) config.getConfigProperty(WEBDAV_PORT);
            pathBuilder.append(":").append(port);
        }
        pathBuilder.append(config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY));

        FileSystemManager manager = VFS.getManager();
        FileObject remote = manager.resolveFile(pathBuilder.toString());

        return remote.getContent().getOutputStream();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(WEBDAV_HOSTNAME,
                                  WEBDAV_PORT,
                                  FILE_OUTPUT_PATH_PROPERTY,
                                  WEBDAV_USERNAME,
                                  WEBDAV_PASSWORD);
    }

}
