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

package ai.datamaker.integration.sink;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.base.JdbcOutputSink;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.sql.ResultSet;

//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@ActiveProfiles("azure")
class JdbcOutputSinkIntegrationTest {

//    @BeforeAll
//    public static void init() {
//        // start the TCP Server
//        Server server = Server.createTcpServer(args).start();
//        // stop the TCP Server
//        server.stop();
//
//    }

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {

        getDataSource().getConnection().createStatement().execute("CREATE TABLE TBL_EMPLOYEES (\n"
            + "   id INT AUTO_INCREMENT  PRIMARY KEY,\n"
            + "   first_name VARCHAR(250) NOT NULL,\n"
            + "   last_name VARCHAR(250) NOT NULL\n"
            + ");");

        JobConfig config = new JobConfig();
        config.put(JdbcOutputSink.JDBC_DRIVER, "org.h2.Driver");
        config.put(JdbcOutputSink.JDBC_URL, "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        config.put(JdbcOutputSink.JDBC_USERNAME, "sa");
        config.put(JdbcOutputSink.JDBC_PASSWORD, "sa");

        JdbcOutputSink outputSink = new JdbcOutputSink();
        // Test with h2
        OutputStream outputStream = outputSink.getOutputStream(config);

        outputStream.write("insert into TBL_EMPLOYEES(first_name,last_name) values('test', 'test abc 123');".getBytes());
        outputStream.flush();
        outputStream.close();

        ResultSet rs = getDataSource().getConnection().createStatement().executeQuery("select * from TBL_EMPLOYEES");
        while (rs.next()) {
            System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
        }
    }

    DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }
}