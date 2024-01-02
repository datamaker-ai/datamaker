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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldMapping;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.NameField;
import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.service.FieldDetectorService;
import ai.datamaker.repository.FieldMappingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class FieldDetectorServiceTest {

    FieldDetectorService service;

    FieldMappingRepository repository;

    String JSON_PAYLOAD = "{"
        + "  \"servlet\": [   \n"
        + "    {\n"
        + "      \"servlet-name\": \"cofaxCDS\",\n"
        + "      \"servlet-class\": \"org.cofax.cds.CDSServlet\",\n"
        + "      \"init-param\": {\n"
        + "        \"configGlossary:installationAt\": \"Philadelphia, PA\",\n"
        + "        \"configGlossary:adminEmail\": \"ksm@pobox.com\",\n"
        + "        \"configGlossary:poweredBy\": \"Cofax\",\n"
        + "        \"configGlossary:staticPath\": \"/content/static\",\n"
        + "        \"templateOverridePath\": \"\",\n"
        + "        \"defaultListTemplate\": \"listTemplate.htm\",\n"
        + "        \"defaultFileTemplate\": \"articleTemplate.htm\",\n"
        + "        \"useJSP\": false,\n"
        + "        \"jspListTemplate\": \"listTemplate.jsp\",\n"
        + "        \"jspFileTemplate\": \"articleTemplate.jsp\",\n"
        + "        \"cachePackageTagsTrack\": 200,\n"
        + "        \"cachePackageTagsStore\": 200,\n"
        + "        \"cachePackageTagsRefresh\": 60,\n"
        + "        \"cacheTemplatesTrack\": 100,\n"
        + "        \"cacheTemplatesStore\": 50,\n"
        + "        \"cacheTemplatesRefresh\": 15,\n"
        + "        \"cachePagesTrack\": 200,\n"
        + "        \"cachePagesStore\": 100,\n"
        + "        \"cachePagesRefresh\": 10,\n"
        + "        \"cachePagesDirtyRead\": 10,\n"
        + "        \"searchEngineListTemplate\": \"forSearchEnginesList.htm\",\n"
        + "        \"searchEngineFileTemplate\": \"forSearchEngines.htm\",\n"
        + "        \"searchEngineRobotsDb\": \"WEB-INF/robots.db\",\n"
        + "        \"useDataStore\": true,\n"
        + "        \"dataStoreClass\": \"org.cofax.SqlDataStore\",\n"
        + "        \"dataStoreMaxConns\": 100,\n"
        + "        \"dataStoreConnUsageLimit\": 100,\n"
        + "        \"dataStoreLogLevel\": \"debug\",\n"
        + "        \"maxUrlLength\": 500}},\n"
        + "    {\n"
        + "      \"servlet-name\": \"cofaxEmail\",\n"
        + "      \"servlet-class\": \"org.cofax.cds.EmailServlet\",\n"
        + "      \"init-param\": {\n"
        + "      \"mailHost\": \"mail1\",\n"
        + "      \"mailHostOverride\": \"mail2\"}},\n"
        + "    {\n"
        + "      \"servlet-name\": \"cofaxAdmin\",\n"
        + "      \"servlet-class\": \"org.cofax.cds.AdminServlet\"},\n"
        + " \n"
        + "    {\n"
        + "      \"servlet-name\": \"fileServlet\",\n"
        + "      \"servlet-class\": \"org.cofax.cds.FileServlet\"},\n"
        + "    {\n"
        + "      \"servlet-name\": \"cofaxTools\",\n"
        + "      \"servlet-class\": \"org.cofax.cms.CofaxToolsServlet\",\n"
        + "      \"init-param\": {\n"
        + "        \"templatePath\": \"toolstemplates/\",\n"
        + "        \"log\": 1,\n"
        + "        \"logLocation\": \"/usr/local/tomcat/logs/CofaxTools.log\",\n"
        + "        \"logMaxSize\": \"\",\n"
        + "        \"dataLog\": 1,\n"
        + "        \"dataLogLocation\": \"/usr/local/tomcat/logs/dataLog.log\",\n"
        + "        \"dataLogMaxSize\": \"\",\n"
        + "        \"removePageCache\": \"/content/admin/remove?cache=pages&id=\",\n"
        + "        \"removeTemplateCache\": \"/content/admin/remove?cache=templates&id=\",\n"
        + "        \"fileTransferFolder\": \"/usr/local/tomcat/webapps/content/fileTransferFolder\",\n"
        + "        \"lookInContext\": 1,\n"
        + "        \"adminGroupID\": 4,\n"
        + "        \"betaServer\": true}}] }";

    @BeforeEach
    void setUp() throws Exception {
        service = new FieldDetectorService();
        repository = Mockito.mock(FieldMappingRepository.class);
        when(repository.count()).thenReturn(0L);
        ReflectionTestUtils.setField(service, "fieldMappingRepository", repository);
        List<FieldMapping> fieldMappings = new ArrayList<>();
        fieldMappings.add(new FieldMapping(
            "firstname-en",
            "{\"name\": \"First name\", \"languageTag\": \"en\", \"className\": \"ai.datamaker.model.field.type.NameField\"}"));
        fieldMappings.add(new FieldMapping(
            "firstname-en-CA",
            "{\"name\": \"First name\", \"languageTag\": \"en-CA\", \"className\": \"ai.datamaker.model.field.type.NameField\"}"));
        fieldMappings.add(new FieldMapping(
            "prenom-fr-FR",
            "{\"name\": \"Prenom\", \"languageTag\": \"fr-FR\", \"className\": \"ai.datamaker.model.field.type.NameField\"}"));

        when(repository.findAll()).thenReturn(fieldMappings);

        service.init();
    }

    @Test
    void test_initFromRepository() throws Exception {
        when(repository.count()).thenReturn(1L);
        List<FieldMapping> fieldMappings = new ArrayList<>();
        fieldMappings.add(new FieldMapping(
            "primavera-it",
            "{\"name\": \"primavera\", \"languageTag\": \"it\", \"className\": \"ai.datamaker.model.field.type.StringField\"}"));

        when(repository.findAll()).thenReturn(fieldMappings);
        service.init();

        assertTrue(service.detectTypeOnName("prima vera", Locale.ITALIAN).get() instanceof StringField, "prima vera");
    }

    @Test
    void detectTypeOnName_perLanguage() {
        Optional<Field> nameFieldOptional = service.detectTypeOnName("First name", Locale.ENGLISH);

        assertTrue(nameFieldOptional.isPresent());
        assertTrue(nameFieldOptional.get() instanceof NameField);
        assertEquals("First name", nameFieldOptional.get().getName());
        assertEquals(Locale.ENGLISH, nameFieldOptional.get().getLocale());

        Optional<Field> nameFieldFrOptional = service.detectTypeOnName("prénom", Locale.FRANCE);

        assertTrue(nameFieldFrOptional.isPresent());
        assertTrue(nameFieldFrOptional.get() instanceof NameField);
        assertEquals("prénom", nameFieldFrOptional.get().getName());
        assertEquals(Locale.FRANCE, nameFieldFrOptional.get().getLocale());
    }

    @Test
    void detectTypeOnName_perLocale() {
        Optional<Field> nameFieldOptional = service.detectTypeOnName("First name", Locale.forLanguageTag("en-CA"));

        assertTrue(nameFieldOptional.isPresent());
        assertTrue(nameFieldOptional.get() instanceof NameField);
        assertEquals("First name", nameFieldOptional.get().getName());
        assertEquals(Locale.forLanguageTag("en-CA"), nameFieldOptional.get().getLocale());
    }

    @Test
    void detectTypeOnName_notFoundForName() {
        assertTrue(service.detectTypeOnName("fewafwae", Locale.forLanguageTag("en-CA")).isEmpty());
    }

    @Test
    void detectTypeOnName_notFoundForLocale() {
        assertTrue(service.detectTypeOnName("Míngzì", Locale.SIMPLIFIED_CHINESE).isEmpty());
    }

    @Test
    void detectTypeOnName_cleanInvalidCharacters() {
        assertTrue(service.detectTypeOnName("   (First)_$Name$", Locale.ENGLISH).get() instanceof NameField);
    }

    @Test
    void detectTypeOnValue() throws IOException {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
        Map<String, Object> mapObjects = new ObjectMapper().readValue(JSON_PAYLOAD, typeRef);

        Optional<Field> field = service.detectTypeOnValue(
            "servlet",
            Locale.getDefault(),
            Lists.newArrayList((Iterable) mapObjects.get("servlet")));

        assertTrue(field.isPresent());
    }

    @Test
    void detectType_NoPossibleMatch() {

    }

    @Test
    void detectType_FieldNameDiffersFromValue() {

    }

    @Test
    void isDate() {
        assertTrue(service.getDate("2018-08-08 08:08:08").isPresent(), "2018-08-08 08:08:08=false");
    }

    @Test
    void isDate_withTimeZone() {
        assertTrue(((ZonedDateTime)service.getDate("2018-08-08 08:08:08").get()).getZone() != null, "2018-08-08 08:08:08=false");
    }

    @Test
    void isDate_withTimeZone2() {
        assertTrue(service.getDate("2/6/1968").isPresent(), "2/6/1968=false");
    }

    @Test
    void isBigInteger() {

    }

    @Test
    void isNumeric() {
        assertTrue(service.getNumber("888.00a", Locale.ENGLISH).isPresent(), "888.00a=true");
        assertTrue(service.getNumber("100", Locale.ENGLISH).isPresent(), "100=true");
        assertTrue(service.getNumber("12,342", Locale.ENGLISH).isPresent(), "12,342=true");
        assertTrue(service.getNumber("-1.2", Locale.ENGLISH).isPresent(), "-1.2=true");
        assertTrue(service.getNumber("AAA", Locale.ENGLISH).isEmpty(), "AAA=false");
        assertTrue(service.getNumber("12342,89989", Locale.FRANCE).isPresent(), "12342,89989=true");

        assertTrue(service.getNumber("varchar(50)", Locale.ENGLISH).isEmpty());
    }

    @Test
    void testParse() throws ParseException {
        NumberFormat formatter1 = NumberFormat.getCurrencyInstance(Locale.CANADA_FRENCH);
        // User input
        String value = "432 551,89 $";
        Number valueParsed = formatter1.parse(value);
        System.out.println(valueParsed);
        System.out.println(formatter1.format(432551.89d));
    }

    @Test
    void test_StringMaxLength() {
        TextField field = new TextField();
        List<Object> strings = Lists.newArrayList("feaw", "8098423", "fa", "809jifpaji;ji32qfq-j-feaw", "feaw");
        service.applyLength(field, strings);

        assertEquals(40, field.getLength());
    }

    @Test
    void test_constraints() {
        List<Object> numbers = Arrays.asList(100, 5, 1000443, 6, 4, 4329);
        IntegerField field = new IntegerField();

        service.applyRangeConstraint(field, numbers);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MIN_VALUE_PROPERTY), 4);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MAX_VALUE_PROPERTY), 1000443);
    }

    @Test
    void test_constraints_nullValues() {
        List<Object> numbers = Arrays.asList(null, null, null);
        IntegerField field = new IntegerField();

        service.applyRangeConstraint(field, numbers);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MIN_VALUE_PROPERTY), 0);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MAX_VALUE_PROPERTY), Integer.MAX_VALUE);
    }

    @Test
    void test_constraints_sameValues() {
        List<Object> numbers = Arrays.asList(4329, 4329, 4329);
        IntegerField field = new IntegerField();

        service.applyRangeConstraint(field, numbers);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MIN_VALUE_PROPERTY), 0);
        assertEquals(field.getConfig().getConfigProperty(IntegerField.MAX_VALUE_PROPERTY), 4329);
    }

    @Test
    void test_detectOnTypeOnly() {
        assertTrue(service.detectType("varchar(50)", "string", Locale.ENGLISH).isPresent());
    }
}