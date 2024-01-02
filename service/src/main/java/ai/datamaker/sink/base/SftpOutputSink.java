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
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Supports for: [SSH, SFTP SCP]
 */
@DataOutputSinkType(compressed = true, encrypted = true)
public class SftpOutputSink implements DataOutputSink {

    public static final PropertyConfig SFTP_HOSTNAME
            = new PropertyConfig("sftp.sink.hostname",
                                 "Hostname",
                                 PropertyConfig.ValueType.STRING,
                                 "remote",
                                 Collections.emptyList());

    public static final PropertyConfig SFTP_PORT
            = new PropertyConfig("sftp.sink.port",
                                 "Port",
                                 PropertyConfig.ValueType.NUMERIC,
                                 139,
                                 Collections.emptyList());
    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("ftp.sink.output.filename",
                                 "Output file path",
                                 PropertyConfig.ValueType.EXPRESSION,
                                 "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                 Collections.emptyList());

    public static final PropertyConfig SFTP_USERNAME =
            new PropertyConfig("sftp.sink.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig SFTP_PASSWORD =
            new PropertyConfig("sftp.sink.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig SFTP_USER_DIR_IS_ROOT =
            new PropertyConfig("sftp.sink.user.dir.is.root",
                               "Use User's home as path root",
                               PropertyConfig.ValueType.BOOLEAN,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig SFTP_PRIVATE_KNOWN_HOSTS_PATH =
            new PropertyConfig("sftp.sink.known.hosts.path",
                               "Known hosts path",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig SFTP_PRIVATE_KEY_PATH =
            new PropertyConfig("sftp.sink.private.key.path",
                               "Private key path",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig SFTP_PRIVATE_KEY_PASSPHRASE =
            new PropertyConfig("sftp.sink.private.key.passphrase",
                               "Passphrase",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

//    SFTP
//
//    Provides access to the files on an SFTP server (that is, an SSH or SCP server).
//
//    URI Format
//
//    sftp://[ username[: password]@] hostname[: port][ relative-path]
//
//    Examples
//
//    sftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz
//
//    By default, the path is relative to the user's home directory. This can be changed with:
//
//            FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(options, false);


    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder
                .append("sftp://");

        if (config.containsKey(SFTP_USERNAME.getKey())) {
            String username = (String) config.getConfigProperty(SFTP_USERNAME);
            pathBuilder.append(username);
        }
        if (config.containsKey(SFTP_PASSWORD.getKey())) {
            String password = (String) config.getConfigProperty(SFTP_PASSWORD);
            pathBuilder.append(":").append(password).append("@");
        } else {
            pathBuilder.append("@");
        }
        pathBuilder.append(config.getConfigProperty(SFTP_HOSTNAME));
        if (config.containsKey(SFTP_PORT.getKey())) {
            int port = (Integer) config.getConfigProperty(SFTP_PORT);
            pathBuilder.append(":").append(port);
        }
        pathBuilder.append(config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY));

        FileSystemManager manager = VFS.getManager();
        FileObject remote = manager.resolveFile(pathBuilder.toString(), createDefaultOptions(config));

        return remote.getContent().getOutputStream();
    }

    /**
     * Method to setup default SFTP config
     *
     * @return the FileSystemOptions object containing the specified
     *         configuration options
     * @throws FileSystemException
     */
    public static FileSystemOptions createDefaultOptions(JobConfig config) throws FileSystemException {
        // Create SFTP options
        FileSystemOptions opts = new FileSystemOptions();

        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        if (config.containsKey(SFTP_PRIVATE_KNOWN_HOSTS_PATH.getKey())) {
            String knownHostsPath = (String) config.getConfigProperty(SFTP_PRIVATE_KNOWN_HOSTS_PATH);
            SftpFileSystemConfigBuilder.getInstance().setKnownHosts(opts, new File(knownHostsPath));
        }

        if (config.containsKey(SFTP_PRIVATE_KEY_PATH.getKey())) {
            String keyPath = (String) config.getConfigProperty(SFTP_PRIVATE_KEY_PATH);
            String passphrase = (String) config.get(SFTP_PRIVATE_KEY_PASSPHRASE.getKey());
            byte[] passphraseBytes = passphrase != null ? passphrase.getBytes() : null;
            SftpFileSystemConfigBuilder.getInstance().setIdentityInfo(opts, new IdentityInfo(new File(keyPath), passphraseBytes));
        }

        /*
         * Using the following line will cause VFS to choose File System's Root as VFS's root. If I wanted to use User's home as VFS's root then set
         * 2nd method parameter to "true"
         */
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

        return opts;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
                SFTP_HOSTNAME,
                SFTP_PORT,
                FILE_OUTPUT_PATH_PROPERTY,
                SFTP_USERNAME,
                SFTP_PASSWORD,
                SFTP_USER_DIR_IS_ROOT,
                SFTP_PRIVATE_KEY_PATH,
                SFTP_PRIVATE_KEY_PASSPHRASE);
    }
}
