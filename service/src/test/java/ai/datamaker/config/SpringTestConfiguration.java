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

package ai.datamaker.config;

import ai.datamaker.service.BeanService;
import ai.datamaker.service.ComponentConfigurationService;
import ai.datamaker.service.DashboardService;
import ai.datamaker.service.DatasetCreationService;
import ai.datamaker.service.DatasetService;
import ai.datamaker.service.DictionaryService;
import ai.datamaker.service.EncryptionService;
import ai.datamaker.service.FieldDetectorService;
import ai.datamaker.service.FieldService;
import ai.datamaker.service.GenerateDataService;
import ai.datamaker.service.JobSchedulerService;
import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Configuration
@AutoConfigureDataJpa
@TestConfiguration
@ActiveProfiles("integration")
//@EnableJpaRepositories("ca.breakpoints.datamaker.repository")
public class SpringTestConfiguration {

    @MockBean
    private GenerateDataService generateDataService;

    @MockBean
    private DatasetCreationService datasetCreationService;

    @MockBean
    private JobSchedulerService jobSchedulerService;

    @MockBean
    private ComponentConfigurationService componentConfigurationService;

    @MockBean
    private FieldDetectorService fieldDetectorService;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EncryptionService encryptionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    FieldService fieldService() throws Exception {

        return new FieldService();
    }

    @Bean
    DictionaryService dictionaryService() throws Exception {
        DictionaryService dictionaryService = new DictionaryService();
        dictionaryService.setSupportedLanguages(Sets.newHashSet("en"));
        dictionaryService.init();

        return dictionaryService;
    }

    @Bean
    DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }

    @Bean
    BeanService beanService() {
        BeanService beanService = new BeanService();
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        when(applicationContext.getBean(eq(EncryptionService.class))).thenReturn(encryptionService);
        beanService.setApplicationContext(applicationContext);

        return beanService;
    }

    @Bean
    public BuildProperties buildProperties() throws Exception {
        Properties properties = new Properties();
        properties.put("version", "1.0");
        return new BuildProperties(properties);
    }
}
