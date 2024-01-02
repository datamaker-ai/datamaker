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

package ai.datamaker.integration;

import ai.datamaker.model.Authority;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.User;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.UserRepository;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
public class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private GenerateDataJobRepository generateDataJobRepository;

    @Test
    public void get() {
        User user = new User();
        user.setFirstName("first");
        user.setLastName("last");
        user.setPassword("Qwerty1234!");
        user.setUsername("user");
        user.setAuthority(Authority.ROLE_USER);
        userRepository.save(user);

        Iterable<User> iterable = userRepository.findAll();

        iterable.forEach(System.out::println);
    }

    @Test
    public void list() {
        final Query query = entityManager.createQuery("SELECT u FROM User as u WHERE u.firstName = :name");
        query.setParameter("name", "Deja");
        List users = query.getResultList();

        users.forEach(System.out::println);
    }

    @Test
    @Transactional
    public void persist_generateJob() {
        GenerateDataJob generateDataJob = new GenerateDataJob();
        JobConfig config = new JobConfig();
        config.put("test", 4);
        generateDataJob.getConfig().put("test", config);

        generateDataJobRepository.save(generateDataJob);

        Iterable<GenerateDataJob> generateDataJobs = generateDataJobRepository.findAll();

        generateDataJobs.forEach(j -> {
            List<Dataset> d = j.getDataset();
            d.forEach(System.out::println);
            System.out.println(j);
        });
    }

    @Test
    @Transactional
    public void persist_complexField() {
        ComplexField field = new ComplexField("complex-level1", Locale.ENGLISH);
        FieldConfig stringConfig = new FieldConfig();

        StringField stringField = new StringField("age", Locale.ENGLISH);
        stringConfig.put(StringField.LENGTH_PROPERTY, 1024);

        stringField.setConfig(stringConfig);

        ComplexField field2 = new ComplexField("complex-level2", Locale.ENGLISH);
        field2.setReferences(Lists.newArrayList(new AgeField("age", Locale.ITALIAN)));

        field.setReferences(Lists.newArrayList(field2, stringField));

        fieldRepository.save(field);

        Iterable<Field> iterable = fieldRepository.findAll();

        iterable.forEach(System.out::println);
    }

}
