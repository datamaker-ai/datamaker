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
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Support TLS/SSL
 */
@DataOutputSinkType(compressed = true, encrypted = true)
public class FtpOutputSink implements DataOutputSink {

    public static final PropertyConfig FTP_HOSTNAME
            = new PropertyConfig("ftp.sink.hostname",
                                 "Hostname",
                                 PropertyConfig.ValueType.STRING,
                                 "remote",
                                 Collections.emptyList());

    public static final PropertyConfig FTP_PORT
            = new PropertyConfig("ftp.sink.port",
                                 "Port",
                                 PropertyConfig.ValueType.NUMERIC,
                                 22,
                                 Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("ftp.sink.output.filename",
                                 "Output file path",
                                 PropertyConfig.ValueType.EXPRESSION,
                                 "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                 Collections.emptyList());

    // Choosing FTP with TLS/SSL enables the Enable Encryption checkbox. If this box is checked, all of your communication will be protected; otherwise only your password will be protected, but performance may be better. Some servers do not support encrypting data when using FTP with TLS/SSL.
    public static final PropertyConfig FTP_SECURE
            = new PropertyConfig("ftp.sink.secure",
                                "Secure (ftp+ssl)",
                                ValueType.BOOLEAN,
                                false,
                                Collections.emptyList());

    public static final PropertyConfig FTP_USERNAME =
            new PropertyConfig("ftp.sink.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig FTP_PASSWORD =
            new PropertyConfig("ftp.sink.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

//    org.apache.commons.net.ftp.FTP
//
//    Provides access to the files on an FTP server.
//
//            URI Format
//
//    ftp://[ username[: password]@] hostname[: port][ relative-path]
//
//    Examples
//
//    ftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz
//
//    By default, the path is relative to the user's home directory. This can be changed with:
//
//            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(options, false);
//    FTPS
//
//    Provides access to the files on an FTP server over SSL.
//
//            URI Format
//
//    ftps://[ username[: password]@] hostname[: port][ absolute-path]
//
//    Examples
//
//    ftps://myusername:mypassword@somehost/pub/downloads/somefile.tgz

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        StringBuilder pathBuilder = new StringBuilder();

        if (config.containsKey(FTP_SECURE.getKey())) {
            pathBuilder.append("ftps://");
        } else {
            pathBuilder.append("ftp://");
        }

        if (config.containsKey(FTP_USERNAME.getKey())) {
            String username = (String) config.getConfigProperty(FTP_USERNAME);
            pathBuilder.append(username);
        }
        if (config.containsKey(FTP_PASSWORD.getKey())) {
            String password = (String) config.getConfigProperty(FTP_PASSWORD);
            pathBuilder.append(":").append(password).append("@");
        }
        pathBuilder.append(config.getConfigProperty(FTP_HOSTNAME));
        if (config.containsKey(FTP_PORT.getKey())) {
            int port = (Integer) config.getConfigProperty(FTP_PORT);
            pathBuilder.append(":").append(port);
        }
        pathBuilder.append(config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY));

        FileSystemManager manager = VFS.getManager();
        FileObject remote = manager.resolveFile(pathBuilder.toString());

        return remote.getContent().getOutputStream();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            FTP_HOSTNAME,
            FTP_PORT,
            FILE_OUTPUT_PATH_PROPERTY,
            FTP_USERNAME,
            FTP_PASSWORD
        );
    }
}
