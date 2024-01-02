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

/**
 *     CIFS
 *     The CIFS (sandbox) filesystem provides access to a CIFS server, such as a Samba server, or a Windows share.
 *     URI Format
 *     smb://[ username[: password]@] hostname[: port][ absolute-path]
 *     Examples
 *     smb://somehost/home
 */
@DataOutputSinkType(compressed = true, encrypted = true)
public class CifsOutputSink implements DataOutputSink {
    public static final PropertyConfig CIFS_HOSTNAME
            = new PropertyConfig("cifs.sink.hostname",
                                 "Hostname",
                                 PropertyConfig.ValueType.STRING,
                                 "remote",
                                 Collections.emptyList());

    public static final PropertyConfig CIFS_PORT
            = new PropertyConfig("cifs.sink.port",
                                 "Port",
                                 PropertyConfig.ValueType.NUMERIC,
                                 139,
                                 Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("cifs.sink.output.filename",
                                "Output file path",
                                PropertyConfig.ValueType.EXPRESSION,
                                "\"/\" + #dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                Collections.emptyList());

    public static final PropertyConfig CIFS_USERNAME =
            new PropertyConfig("cifs.sink.username",
                                "Username",
                                PropertyConfig.ValueType.STRING,
                                "",
                                Collections.emptyList());

    public static final PropertyConfig CIFS_PASSWORD =
            new PropertyConfig("cifs.sink.password",
                                "Password",
                                PropertyConfig.ValueType.PASSWORD,
                                "",
                                Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder
                .append("smb://");

        if (config.containsKey(CIFS_USERNAME.getKey())) {
            String username = (String) config.getConfigProperty(CIFS_USERNAME);
            pathBuilder.append(username);
        }
        if (config.containsKey(CIFS_PASSWORD.getKey())) {
            String password = (String) config.getConfigProperty(CIFS_PASSWORD);
            pathBuilder.append(":").append(password).append("@");
        }
        pathBuilder.append(config.getConfigProperty(CIFS_HOSTNAME));
        if (config.containsKey(CIFS_PORT.getKey())) {
            int port = (Integer) config.getConfigProperty(CIFS_PORT);
            pathBuilder.append(":").append(port);
        }
        pathBuilder.append(config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY));

        FileSystemManager manager = VFS.getManager();
        FileObject remote = manager.resolveFile(pathBuilder.toString());

        return remote.getContent().getOutputStream();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(CIFS_HOSTNAME,
                                  CIFS_PORT,
                                  FILE_OUTPUT_PATH_PROPERTY,
                                  CIFS_USERNAME,
                                  CIFS_PASSWORD);
    }

}
