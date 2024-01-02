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
import ai.datamaker.utils.stream.UDPOutputStream;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import org.springframework.util.Assert;

public class SocketOutputSink implements DataOutputSink {

    public static final PropertyConfig PORT_NUMBER
        = new PropertyConfig(
        "socket.sink.port.number",
        "Port number",
        ValueType.NUMERIC,
        0,
        Lists.newArrayList(0, 65535));

    public static final PropertyConfig HOSTNAME
        = new PropertyConfig(
        "socket.sink.hostname",
        "Hostname",
        ValueType.STRING,
        "localhost",
        Collections.emptyList());

    public static final PropertyConfig SOCKET_PROTOCOL
        = new PropertyConfig(
        "socket.sink.socket.protocol",
        "Socket protocol",
        ValueType.STRING,
        "TCP",
        Lists.newArrayList("TCP", "UDP"));

    public static final PropertyConfig SOCKET_HEADER_MESSAGE
        = new PropertyConfig(
        "socket.sink.header.message",
        "Header message",
        ValueType.STRING,
        "",
        Collections.emptyList());

    public static final PropertyConfig SOCKET_FOOTER_MESSAGE
        = new PropertyConfig(
        "socket.sink.footer.message",
        "Footer message",
        ValueType.STRING,
        "",
        Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            PORT_NUMBER,
            HOSTNAME,
            SOCKET_PROTOCOL,
            SOCKET_HEADER_MESSAGE,
            SOCKET_FOOTER_MESSAGE
        );
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        int portNumber = (int) config.getConfigProperty(PORT_NUMBER);
        Assert.isTrue(portNumber >= 0 && portNumber <= 0xFFFF , "Invalid port number");
        String hostName = (String) config.getConfigProperty(HOSTNAME);
        Assert.isTrue(StringUtils.isNotBlank(hostName), "Hostname cannot be blank");
        String protocol = (String) config.getConfigProperty(SOCKET_PROTOCOL);
        Assert.isTrue(StringUtils.isNotBlank(protocol), "Protocol cannot be blank");

        String header = (String) config.getConfigProperty(SOCKET_HEADER_MESSAGE);
        String footer = (String) config.getConfigProperty(SOCKET_FOOTER_MESSAGE);

        if (StringUtils.isNotEmpty(header) || StringUtils.isNotEmpty(footer)) {
            if ("UDP".equals(protocol)) {
                return new UDPOutputStream(hostName, portNumber);
            }

            Socket echoSocket = new Socket(hostName, portNumber);
            return new SocketStreamWrapper(echoSocket.getOutputStream(), header, footer);
        }

        if ("UDP".equals(protocol)) {
            return new UDPOutputStream(hostName, portNumber);
        }

        Socket echoSocket = new Socket(hostName, portNumber);
        return echoSocket.getOutputStream();
    }

    static class SocketStreamWrapper extends OutputStream {

        private final OutputStream socketOutputStream;
        private final String footerMessage;
        private boolean closed = false;

        SocketStreamWrapper(OutputStream socketOutputStream, String headerMessage, String footerMessage) throws IOException {
            this.socketOutputStream = socketOutputStream;
            if (StringUtils.isNotEmpty(headerMessage)) {
                socketOutputStream.write(headerMessage.getBytes());
            }
            this.footerMessage = footerMessage;
        }

        @Override
        public void write(int b) throws IOException {
            socketOutputStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            socketOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;

                if (StringUtils.isNotEmpty(footerMessage)) {
                    socketOutputStream.write(footerMessage.getBytes());
                    socketOutputStream.flush();
                }
                socketOutputStream.close();
            }
        }
    }

}
