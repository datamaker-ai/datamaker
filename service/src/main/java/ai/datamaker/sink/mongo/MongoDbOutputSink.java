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

package ai.datamaker.sink.mongo;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.stream.SendDataOutputStream;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Deprecated
public class MongoDbOutputSink implements DataOutputSink {

  @Override
  public boolean accept(FormatType type) {
    return type == FormatType.JSON;
  }

  @Override
  public OutputStream getOutputStream() throws Exception {

//    final Document doc = new Document("myKey", "myValue");
//    final String jsonString = doc.toJson();
//    final Document doc = Document.parse(jsonString);

//    DBObject dbObj = ... ;
//    String json = JSON.serialize(dbObj );
//    DBObject bson = ( DBObject ) JSON.parse( json );

    return new SendDataOutputStream((bytes -> {

    }));
  }

  @Override
  public OutputStream getOutputStream(JobConfig config) throws Exception {
    return null;
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Collections.emptyList();
  }
}
