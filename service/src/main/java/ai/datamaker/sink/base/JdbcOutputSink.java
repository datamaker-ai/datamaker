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
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JdbcOutputSink implements DataOutputSink {

    public static final PropertyConfig JDBC_DRIVER = new PropertyConfig(
        "jdbc.sink.driver",
        "JDBC driver class name",
        ValueType.STRING,
        "",
        Collections.emptyList());

    public static final PropertyConfig JDBC_URL = new PropertyConfig(
        "jdbc.sink.url",
        "Connection URL",
        ValueType.STRING,
        "",
        Collections.emptyList());

    public static final PropertyConfig JDBC_USERNAME = new PropertyConfig(
        "jdbc.sink.username",
        "Username",
        ValueType.STRING,
        "",
        Collections.emptyList());

    public static final PropertyConfig JDBC_PASSWORD = new PropertyConfig(
        "jdbc.sink.password",
        "Password",
        ValueType.PASSWORD,
        "",
        Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return type == FormatType.SQL;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        Class.forName((String) config.getConfigProperty(JDBC_DRIVER));
        final Connection connection = DriverManager.getConnection((String) config.getConfigProperty(JDBC_URL),
                                                                  (String)  config.getConfigProperty(JDBC_USERNAME),
                                                                  (String) config.getConfigProperty(JDBC_PASSWORD));

        return new OutputStream() {

            private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void flush() throws IOException {
                super.flush();
                try (Statement stmt = connection.createStatement()) {
                    // connection.createBlob()
                    // connection.createStruct()
                    // stmt.executeBatch()
                    // connection.createArrayOf()
                    //PreparedStatement preparedStatement = connection.prepareStatement("");
                    stmt.execute(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
                } catch (SQLException e) {
                    throw new IOException(e);
                }
                byteArrayOutputStream.reset();
            }

            @Override
            public void close() throws IOException {
                super.close();
                if (byteArrayOutputStream.size() > 0) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
                        connection.close();
                    } catch (SQLException e) {
                        throw new IOException(e);
                    }
                    byteArrayOutputStream.reset();
                }
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            JDBC_DRIVER,
            JDBC_URL,
            JDBC_USERNAME,
            JDBC_PASSWORD
        );
    }
}
