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

package ai.datamaker.service;

import ai.datamaker.model.field.Field;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The indexes are updated automatically when {@link javax.persistence.Entity} are updated.
 */
@Service
@Slf4j
public class HibernateSearchService {

    private final EntityManager entityManager;

    @Value("${search.service.threshold}")
    private Float threshold = 0.5f;

    @Value("${search.service.distance}")
    private Integer editDistanceUpTo = 2;

    @Value("${search.service.prefixLength}")
    private Integer prefixLength = 0;

    public HibernateSearchService(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public HibernateSearchService(final EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    public void initializeHibernateSearch() {

        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            //e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    //@Scheduled(cron = "0 15 10 15 * ?")
    public void forceRefresh() {
        log.info("Rebuilding search index");
        initializeHibernateSearch();
    }

    @Transactional
    public List<Field> fuzzySearch(String searchTerm) {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Field.class).get();
        Query luceneQuery = qb
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(editDistanceUpTo)
                .withPrefixLength(prefixLength)
                .onFields("name")
                .matching(searchTerm)
                .createQuery();

        FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Field.class);
        //jpaQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.THIS);
        // execute search
        try {
            return jpaQuery.getResultList();
        } catch (Exception e) {
            log.warn("Error while searching", e);
        }

        return Collections.emptyList();
    }

    @Async
    @Transactional
    public <V> Future<List<V>> search(Object searchTerm, Class<V> entityClass, String... fields) {

        // TODO enabled
//        if (entityClass.getAnnotation(Indexed.class) == null) {
//            throw new IllegalArgumentException("Entity " + entityClass.getName() + " is not indexed");
//        }

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();
        Query luceneQuery = qb
                .simpleQueryString()
                .boostedTo(2.0f)
                .withConstantScore()
                .onFields(fields[0], Arrays.copyOfRange(fields, 1, fields.length))
                .matching(searchTerm.toString())
                .createQuery();

//        luceneQuery = qb
//                .keyword()
//                .wildcard()
//                .onFields(fields)
//                .matching(searchTerm.toString())
//                .createQuery();

        // TODO filter on entity or not
        javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, entityClass);

        // execute search
        try {
            return new AsyncResult(jpaQuery.getResultList());
        } catch (Exception e) {
            log.warn("Error while searching", e);
        }

        return new AsyncResult(Collections.emptyList());
    }
}
