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

import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.forms.FieldForm;
import ai.datamaker.service.HibernateSearchService;
import org.hibernate.search.exception.SearchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
class HibernateSearchServiceIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory factory;

    private HibernateSearchService searchService;

    @BeforeEach
    public void init() {
        if (searchService == null) {
            searchService = new HibernateSearchService(factory);
            searchService.initializeHibernateSearch();
        }
    }

    @Test
    void fuzzySearch() {
    }

    @Test
    void search() throws ExecutionException, InterruptedException {
        Future<List<StringField>> results = searchService.search("juni*", StringField.class, "name");

        assertThat(results.get()).hasSize(1).extracting("name").contains("junit");
    }

    @Test
    void search_notIndexedException() {
        Assertions.assertThrows(SearchException.class, () -> searchService.search("test", FieldForm.class, "name"), "Not indexed");
    }
}