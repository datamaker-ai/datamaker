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

package ai.datamaker.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.type.EmptyField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class JsonProcessorTest extends AbstractDatasetProcessorTest {

    String JSON_PAYLOAD = "{\"web-app\": {\n"
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
        + "        \"betaServer\": true}}],\n"
        + "  \"servlet-mapping\": {\n"
        + "    \"cofaxCDS\": \"/\",\n"
        + "    \"cofaxEmail\": \"/cofaxutil/aemail/*\",\n"
        + "    \"cofaxAdmin\": \"/admin/*\",\n"
        + "    \"fileServlet\": \"/static/*\",\n"
        + "    \"cofaxTools\": \"/tools/*\"},\n"
        + " \n"
        + "  \"taglib\": {\n"
        + "    \"taglib-uri\": \"cofax.tld\",\n"
        + "    \"taglib-location\": \"/WEB-INF/tlds/cofax.tld\"}}}\n";


    private static String TEST_ARRAY = "[\"element1\",\"element2\",\"test\"]";

    private static String TEST_PRIMITIVE = "\"this is a string\"";

    private static String TEST_ARRAY_NESTED = "[   \n"
        + "    {\n"
        + "      \"servlet-name\": \"cofaxCDS\",\n"
        + "      \"servlet-class\": \"org.cofax.cds.CDSServlet\",\n"
        + "      \"init-param\": {\n"
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
        + "        \"lookInContext\": 1,\n"
        + "        \"adminGroupID\": 4,\n"
        + "        \"betaServer\": true}}]";

    protected JsonProcessorTest() {
        super(new JsonProcessor());
    }

    @Test
    void process() throws IOException {
        when(fieldDetectorService.detectTypeOnValue(anyString(), any(), anyCollection())).thenReturn(Optional.of(new EmptyField()));
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        ByteArrayInputStream bais = new ByteArrayInputStream(JSON_PAYLOAD.getBytes());

        Optional<Dataset> dataset = datasetProcessor.process(bais);

        assertTrue(dataset.isPresent());
        assertEquals(1, dataset.get().getFields().size());
    }

    @Test
    void process_primitives() {
        when(fieldDetectorService.detectTypeOnValue(anyString(), any(), anyCollection())).thenReturn(Optional.of(new EmptyField()));

        ByteArrayInputStream bais = new ByteArrayInputStream(TEST_PRIMITIVE.getBytes());

        Optional<Dataset> dataset = datasetProcessor.process(bais);

        assertTrue(dataset.isPresent());
        assertEquals(1, dataset.get().getFields().size());
    }

    @Test
    void process_list_of_nested() {
        when(fieldDetectorService.detectTypeOnValue(anyString(), any(), anyCollection())).thenReturn(Optional.of(new EmptyField()));
        ByteArrayInputStream bais = new ByteArrayInputStream(TEST_ARRAY_NESTED.getBytes());

        Optional<Dataset> dataset = datasetProcessor.process(bais);

        assertTrue(dataset.isPresent());
        assertEquals(1, dataset.get().getFields().size());
    }

    @Test
    void process_list_of_primitives() {
        when(fieldDetectorService.detectTypeOnValue(anyString(), any(), anyCollection())).thenReturn(Optional.of(new EmptyField()));
        ByteArrayInputStream bais = new ByteArrayInputStream(TEST_ARRAY.getBytes());

        Optional<Dataset> dataset = datasetProcessor.process(bais);

        assertTrue(dataset.isPresent());
        assertEquals(1, dataset.get().getFields().size());
    }

    @Test
    void supportedTypes() {
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.JSON));
    }
}