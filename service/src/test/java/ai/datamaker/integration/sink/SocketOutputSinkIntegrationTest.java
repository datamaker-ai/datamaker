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

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.base.SocketOutputSink;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class SocketOutputSinkIntegrationTest {

    private static Thread t;

    private static volatile boolean isSuccess = false;

    @BeforeAll
   //@Timeout(5)
    public static void init() throws Exception {
        System.out.println("Init..");

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isSuccess) {
                    try (
                            ServerSocket serverSocket = new ServerSocket(9090);
                            Socket clientSocket = serverSocket.accept();
                            PrintWriter out =
                                    new PrintWriter(clientSocket.getOutputStream(),
                                                    true);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(clientSocket.getInputStream()));
                    ){
                        String inputLine, outputLine;

                        while ((inputLine = in.readLine()) != null) {
                            if ("hello world".equals(inputLine)) {
                                isSuccess = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.setDaemon(true);
        t.start();
        //t.join();

    }

//    @AfterEach
//    void destroy() throws InterruptedException {
//        t.interrupt();
//        t.join();
//    }

    @Test
    @Timeout(5)
    void accept() {
        SocketOutputSink sink = new SocketOutputSink();
        Assertions.assertTrue(sink.accept(FormatType.ORC));
        Assertions.assertTrue(sink.accept(FormatType.CSV));
    }

    @Test
    @Timeout(5)
    void getOutputStream() throws Exception {
        SocketOutputSink sink = new SocketOutputSink();
        JobConfig config = new JobConfig();
        config.put(SocketOutputSink.HOSTNAME,
                  "localhost");
        config.put(SocketOutputSink.PORT_NUMBER,
                  9090);
        config.put(SocketOutputSink.SOCKET_FOOTER_MESSAGE,
                  "\n=======\n");

        try (OutputStream outputStream = sink.getOutputStream(config);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream,
                                                                StandardCharsets.UTF_8)) {
            writer.write("hello world");
        }

        Thread.sleep(2000);
        Assertions.assertTrue(isSuccess);
    }
}