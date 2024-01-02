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

package ai.datamaker.sink.hadoop;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.Helper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class HdfsOutputSink implements DataOutputSink {

    public static final int BUFFER_SIZE_DEFAULT = 4096;
    public static final long BLOCK_SIZE_DEFAULT = 33554432L;

    public static final PropertyConfig HADOOP_CONFIGURATION_RESOURCES
            = new PropertyConfig("hdfs.sink.configuration.resources",
                                 "A file or comma separated list of files which contains the Hadoop file system configuration. " +
                                         "Without this, Hadoop will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration.",
                                 ValueType.STRING,
                                 "",
                                 Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("hdfs.sink.output.path",
            "Output file path",
            ValueType.EXPRESSION,
            "'/tmp'",
            Collections.emptyList());

    public static final PropertyConfig FILENAME_PROPERTY
            = new PropertyConfig("hdfs.sink.filename",
                                "Output file path",
                                ValueType.EXPRESSION,
                    "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                Collections.emptyList());

    public static final PropertyConfig SECURED_CLUSTER =
        new PropertyConfig("hdfs.sink.use.kerberos",
                            "Kerberized cluster",
                            ValueType.BOOLEAN,
                            false,
                            Collections.emptyList());

    public static final PropertyConfig HADOOP_HDFS_PRINCIPAL =
            new PropertyConfig("hdfs.sink.hdfs.principal",
                               "Kerberos principal",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig HADOOP_HDFS_KEYTAB =
            new PropertyConfig("hdfs.sink.hdfs.keytab",
                               "Kerberos keytab",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig FILE_WRITE_MODE =
            new PropertyConfig("hdfs.sink.file.write.mode",
                               "Name node",
                               ValueType.STRING,
                               "OVERWRITE",
                               Lists.newArrayList("APPEND", "FAILED", "OVERWRITE"));

    public static final PropertyConfig HDFS_USER_NAME =
            new PropertyConfig("hdfs.sink.hdfs.username",
                               "HDFS user name",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig HDFS_BLOCK_SIZE =
            new PropertyConfig("hdfs.sink.hdfs.block.size",
                               "Size of each block as written to HDFS",
                               ValueType.NUMERIC,
                               BLOCK_SIZE_DEFAULT,
                               Collections.emptyList());

    public static final PropertyConfig HDFS_BUFFER_SIZE =
            new PropertyConfig("hdfs.sink.hdfs.block.size",
                               "Amount of memory to use to buffer file contents during IO",
                               ValueType.NUMERIC,
                               BUFFER_SIZE_DEFAULT,
                               Collections.emptyList());

    public static final PropertyConfig HDFS_REPLICATION_FACTOR =
            new PropertyConfig("hdfs.sink.hdfs.replication.factor",
                               "Number of times that HDFS will replicate each file",
                               ValueType.NUMERIC,
                               1,
                               Collections.emptyList());

    public static final PropertyConfig HDFS_UMASK =
            new PropertyConfig("hdfs.sink.hdfs.umask",
                               "A umask represented as an octal number which determines the permissions of files written to HDFS",
                               ValueType.NUMERIC,
                               022,
                               Collections.emptyList());

    public static final PropertyConfig HDFS_REMOTE_OWNER =
            new PropertyConfig("hdfs.sink.hdfs.remote.owner",
                               "Changes the owner of the HDFS file to this value after it is written",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig HDFS_REMOTE_GROUP =
            new PropertyConfig("hdfs.sink.hdfs.remote.group",
                               "Changes the group of the HDFS file to this value after it is written",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    private static final Set<FormatType> ACCEPTED_TYPES = Sets.newHashSet(FormatType.JSON,
                                                                          FormatType.AVRO,
                                                                          FormatType.CSV,
                                                                          FormatType.ORC,
                                                                          FormatType.PARQUET);

    @Override
    public boolean accept(FormatType type) {
        return ACCEPTED_TYPES.contains(type);
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        boolean secured = (boolean) config.getConfigProperty(SECURED_CLUSTER);
        String additionalConfig = (String) config.getConfigProperty(HADOOP_CONFIGURATION_RESOURCES);
        Configuration conf = new Configuration();
        if (StringUtils.isNotBlank(additionalConfig)) {
            getConfigurationFromResources(conf, additionalConfig);
        }

        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation ugi = secured ? getUgiFromKeytab(config) : getSimpleUgi();

        return ugi.doAs((PrivilegedExceptionAction<FSDataOutputStream>) () -> {
            FileSystem fs = FileSystem.newInstance(conf);

            Path newFolderPath = new Path((String) config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY));
            if(!fs.exists(newFolderPath)) {
                fs.mkdirs(newFolderPath);
                log.debug("Path {} created", newFolderPath.toString());
            }

            Path hdfsWritePath = new Path(newFolderPath + "/" + config.getConfigProperty(FILENAME_PROPERTY));
            fs.setOwner(hdfsWritePath, "admin", "group");
            fs.setPermission(hdfsWritePath, FsPermission.createImmutable((short)777));

            // append / failed
            String writeMode = (String) config.getConfigProperty(FILE_WRITE_MODE);
            if ("APPEND".equals(writeMode)) {
                return fs.append(hdfsWritePath);
            }
            boolean overwrite = "OVERWRITE".equals(writeMode);

            return fs.create(hdfsWritePath,
                             overwrite,
                    conf.getInt("io.file.buffer.size", BUFFER_SIZE_DEFAULT),
                    (short) 1,
                    conf.getLong("fs.local.block.size", BLOCK_SIZE_DEFAULT));
        });
    }

    private UserGroupInformation getSimpleUgi() {
        return UserGroupInformation.createRemoteUser("hdfs");
    }

    private UserGroupInformation getUgiFromUserPassword(JobConfig config) throws Exception {
        System.setProperty("java.security.krb5.conf", "/tmp/home/krb5.conf");

        java.nio.file.Path jaasFile = Files.createTempFile("secure-", ".jaas");

        // create JAAS config
        Files.write(jaasFile, Arrays.asList(
                "Client {",
                "    com.sun.security.auth.module.Krb5LoginModule required;",
                "};"
        ));
        System.setProperty("java.security.auth.login.config", jaasFile.toString());
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        LoginContext lc = new LoginContext("Client", new Helper.UserPasswordHandler("admin/admin", "admin"));
        lc.login();

        Subject subject = lc.getSubject();

        UserGroupInformation.loginUserFromSubject(subject);
        return UserGroupInformation.getLoginUser();
    }

    private UserGroupInformation getUgiFromKeytab(JobConfig config) throws IOException {
        String principal = (String) config.getConfigProperty(HADOOP_HDFS_PRINCIPAL);
        String keytab = (String) config.getConfigProperty(HADOOP_HDFS_KEYTAB);

        return UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytab);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            HADOOP_CONFIGURATION_RESOURCES,
            FILE_OUTPUT_PATH_PROPERTY,
            FILENAME_PROPERTY,
            HADOOP_HDFS_PRINCIPAL,
            HADOOP_HDFS_KEYTAB,
            SECURED_CLUSTER,
            FILE_WRITE_MODE,
            HDFS_BLOCK_SIZE,
            HDFS_BUFFER_SIZE,
            HDFS_REPLICATION_FACTOR,
            HDFS_UMASK,
            HDFS_REMOTE_OWNER,
            HDFS_REMOTE_GROUP
        );
    }

    private static Configuration getConfigurationFromResources(final Configuration config, String configResources) throws IOException {
        boolean foundResources = false;
        if (null != configResources) {
            String[] resources = configResources.split(",");
            for (String resource : resources) {
                config.addResource(new Path(resource.trim()));
                foundResources = true;
            }
        }

        if (!foundResources) {
            // check that at least 1 non-default resource is available on the classpath
            String configStr = config.toString();
            for (String resource : configStr.substring(configStr.indexOf(":") + 1).split(",")) {
                if (!resource.contains("default") && config.getResource(resource.trim()) != null) {
                    foundResources = true;
                    break;
                }
            }
        }

        if (!foundResources) {
            throw new IOException("Could not find any of the " + configResources + " on the classpath");
        }
        return config;
    }
}
