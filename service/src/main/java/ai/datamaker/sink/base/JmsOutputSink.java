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
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.SslCommon;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generic JMS sender sink.
 */
@Slf4j
public class JmsOutputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig JMS_CONNECTION_FACTORY_CLASSNAME = new PropertyConfig("jms.sink.connection.factory",
                                                                                             "A fully qualified name of the JMS ConnectionFactory implementation class (i.e., org.apache.activemq.ActiveMQConnectionFactory)",
                                                                                             PropertyConfig.ValueType.STRING,
                                                                                             "",
                                                                                             Collections.emptyList());

    public static final PropertyConfig JMS_DESTINATION_NAME = new PropertyConfig("jms.sink.topic.name",
                                                                                 "Destination name (ex: topicName, queueName)",
                                                                                 PropertyConfig.ValueType.STRING,
                                                                                 "",
                                                                                 Collections.emptyList());

    public static final PropertyConfig JMS_BROKER_URI = new PropertyConfig("jms.sink.broker.uri",
                                                                           "Topic name",
                                                                           PropertyConfig.ValueType.STRING,
                                                                           "",
                                                                           Collections.emptyList());

    public static final PropertyConfig JMS_MESSAGE_ID = new PropertyConfig("jms.sink.message.id",
                                                                           "Message ID",
                                                                           PropertyConfig.ValueType.EXPRESSION,
                                                                           "",
                                                                           Collections.emptyList());


    public static final PropertyConfig JMS_DESTINATION_TYPE = new PropertyConfig("jms.sink.destination.type",
                                                                                 "Destination type",
                                                                                 PropertyConfig.ValueType.STRING,
                                                                                 "TOPIC",
                                                                                 Lists.newArrayList("QUEUE", "TOPIC"));

    public static final PropertyConfig USERNAME = new PropertyConfig("jms.sink.username",
                                                                     "Username",
                                                                     PropertyConfig.ValueType.STRING,
                                                                     "",
                                                                     Collections.emptyList());

    public static final PropertyConfig PASSWORD = new PropertyConfig("jms.sink.password",
                                                                     "Password",
                                                                     PropertyConfig.ValueType.PASSWORD,
                                                                     "",
                                                                     Collections.emptyList());

    // Broker URI

//    Destination Name			The name of the JMS Destination. Usually provided by the administrator (e.g., 'topic://myTopic' or 'myTopic').
//    Supports Expression Language: true
//    Destination Type	QUEUE
//
//            QUEUE
//    TOPIC
//
//    The type of the JMS Destination. Could be one of 'QUEUE' or 'TOPIC'. Usually provided by the administrator. Defaults to 'TOPIC
//    User Name			User Name used for authentication and authorization.
//    Password			Password used for authentication and authorization.
//    Sensitive Property: true
//    Connection Client ID			The client id to be set on the connection, if set. For durable non shared consumer this is mandatory, for all others it is optional, typically with shared consumers it is undesirable to be set. Please see JMS spec for further details
//    Supports Expression Language: true

//    static final PropertyDescriptor MESSAGE_BODY = new PropertyDescriptor.Builder()
//            .name("message-body-type")
//            .displayName("Message Body Type")
//            .description("The type of JMS message body to construct.")
//            .required(true)
//            .defaultValue(BYTES_MESSAGE)
//            .allowableValues(BYTES_MESSAGE, TEXT_MESSAGE)
//            .build();

//    @ReadsAttribute(attribute = JmsHeaders.DELIVERY_MODE, description = "This attribute becomes the JMSDeliveryMode message header. Must be an integer."),
//    @ReadsAttribute(attribute = JmsHeaders.EXPIRATION, description = "This attribute becomes the JMSExpiration message header. Must be an integer."),
//    @ReadsAttribute(attribute = JmsHeaders.PRIORITY, description = "This attribute becomes the JMSPriority message header. Must be an integer."),
//    @ReadsAttribute(attribute = JmsHeaders.REDELIVERED, description = "This attribute becomes the JMSRedelivered message header."),
//    @ReadsAttribute(attribute = JmsHeaders.TIMESTAMP, description = "This attribute becomes the JMSTimestamp message header. Must be a long."),
//    @ReadsAttribute(attribute = JmsHeaders.CORRELATION_ID, description = "This attribute becomes the JMSCorrelationID message header."),
//    @ReadsAttribute(attribute = JmsHeaders.TYPE, description = "This attribute becomes the JMSType message header. Must be an integer."),
//    @ReadsAttribute(attribute = JmsHeaders.REPLY_TO, description = "This attribute becomes the JMSReplyTo message header. Must be an integer."),
//    @ReadsAttribute(attribute = JmsHeaders.DESTINATION, description = "This attribute becomes the JMSDestination message header. Must be an integer."),

    @Override
    public boolean accept(FormatType type) {
        return type == FormatType.TEXT || type == FormatType.JSON || type == FormatType.XML || type == FormatType.CUSTOM_TEMPLATE;
    }

    private Destination createDestination(Session session, JobConfig config) throws JMSException {
        String destinationName = (String) config.getConfigProperty(JMS_DESTINATION_NAME);
        String destinationType = (String) config.getConfigProperty(JMS_DESTINATION_TYPE);
        if ("TOPIC".equals(destinationType)) {
            return session.createTopic(destinationName);
        }
        return session.createQueue(destinationName);
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        String connectionClassName = (String) config.getConfigProperty(JMS_CONNECTION_FACTORY_CLASSNAME);
        ConnectionFactory connectionFactory = (ConnectionFactory) Class.forName(connectionClassName).getDeclaredConstructor().newInstance();
        setConnectionFactoryProperties((String) config.getConfigProperty(JMS_BROKER_URI),
                                       connectionClassName,
                                       connectionFactory);
        String username = (String) config.getConfigProperty(USERNAME);
        String password = (String) config.getConfigProperty(PASSWORD);

        final Connection connection = StringUtils.isNotBlank(username) ?
                connectionFactory.createConnection(username, password) :
                connectionFactory.createConnection();

        connection.start();
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final MessageProducer producer = session.createProducer(createDestination(session, config));

        // Create broker based on config
        return new OutputStream() {
            private ByteArrayOutputStream baos = new ByteArrayOutputStream();
            private boolean closed = false;

            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                baos.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                baos.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                if (!closed) {
                    sendData();
                }
            }

            @Override
            public void close() throws IOException {
                if (!closed && baos.size() > 0) {
                    sendData();
                }
                try {
                    session.close();
                    producer.close();
                    connection.close();
                } catch (JMSException e) {
                    log.error("Error while sending message", e);
                }
                closed = true;
            }

            private void sendData() throws IOException {
//              Text message : Represented by javax.jms.TextMessage. It is used to represent a block of text.
//              Object message : Represented by javax.jms.ObjectMessage. It is used to represent a java object.
//              Bytes message : Represented by javax.jms.BytesMessage. It is used to represent the binary data.
//              Stream message : Represented by javax.jms.StreamMessage. It is used to represent a list of java primitive values.
//              Map message : Represented by javax.jms.MapMessage. It is used to represent a set of keyword or value pairs.
                // TODO support other modes
                try {
                    BytesMessage bytesMessage = session.createBytesMessage();
                    bytesMessage.setJMSMessageID((String) config.getConfigProperty(JMS_MESSAGE_ID));
                    bytesMessage.writeBytes(baos.toByteArray());
                    producer.send(bytesMessage);
                } catch (JMSException e) {
                    throw new IOException(e);
                }
                baos.reset();
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(Lists.newArrayList(JMS_CONNECTION_FACTORY_CLASSNAME,
                                                       JMS_BROKER_URI,
                                                       JMS_DESTINATION_NAME,
                                                       JMS_DESTINATION_TYPE,
                                                       JMS_MESSAGE_ID,
                                                       USERNAME,
                                                       PASSWORD));
    }

    /**
     * This operation follows standard bean convention by matching property name
     * to its corresponding 'setter' method. Once the method was located it is
     * invoked to set the corresponding property to a value provided by during
     * service configuration. For example, 'channel' property will correspond to
     * 'setChannel(..) method and 'queueManager' property will correspond to
     * setQueueManager(..) method with a single argument. The bean convention is also
     * explained in user manual for this component with links pointing to
     * documentation of various ConnectionFactories.
     * <p>
     * There are also few adjustments to accommodate well known brokers. For
     * example ActiveMQ ConnectionFactory accepts address of the Message Broker
     * in a form of URL while IBMs in the form of host/port pair(s).
     * <p>
     * This method will use the value retrieved from the 'BROKER_URI' static
     * property as is. An exception to this if ConnectionFactory implementation
     * is coming from IBM MQ and connecting to a stand-alone queue manager. In
     * this case the Broker URI is expected to be entered as a colon separated
     * host/port pair, which then is split on ':' and the resulting pair will be
     * used to execute setHostName(..) and setPort(..) methods on the provided
     * ConnectionFactory.
     * <p>
     * This method may need to be maintained and adjusted to accommodate other
     * implementation of ConnectionFactory, but only for URL/Host/Port issue.
     * All other properties are set as dynamic properties where user essentially
     * provides both property name and value.
     *
     * @see <a href="http://activemq.apache.org/maven/apidocs/org/apache/activemq/ActiveMQConnectionFactory.html#setBrokerURL-java.lang.String-">setBrokerURL(String brokerURL)</a>
     * @see <a href="https://docs.tibco.com/pub/enterprise_message_service/8.1.0/doc/html/tib_ems_api_reference/api/javadoc/com/tibco/tibjms/TibjmsConnectionFactory.html#setServerUrl(java.lang.String)">setServerUrl(String serverUrl)</a>
     * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.1.0/com.ibm.mq.javadoc.doc/WMQJMSClasses/com/ibm/mq/jms/MQConnectionFactory.html#setHostName_java.lang.String_">setHostName(String hostname)</a>
     * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.1.0/com.ibm.mq.javadoc.doc/WMQJMSClasses/com/ibm/mq/jms/MQConnectionFactory.html#setPort_int_">setPort(int port)</a>
     * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_7.1.0/com.ibm.mq.javadoc.doc/WMQJMSClasses/com/ibm/mq/jms/MQConnectionFactory.html#setConnectionNameList_java.lang.String_">setConnectionNameList(String hosts)</a>
     */
    void setConnectionFactoryProperties(String brokerValue, String connectionFactoryValue, ConnectionFactory connectionFactory) {
        if (connectionFactoryValue.startsWith("com.rabbitmq.jms.admin")) {
            //String[] hostPort = brokerValue.split(":");
            setProperty("uri", brokerValue, connectionFactory);
            //factory.setUri("amqp://userName:password@hostName:portNumber/virtualHost");
//            if (hostPort.length == 2) {
//                // If broker URI indeed was colon separated host/port pair
//                setProperty("host", hostPort[0], connectionFactory);
//                setProperty("port", hostPort[1], connectionFactory);
//            }
        } else if (connectionFactoryValue.startsWith("org.apache.activemq")) {
            setProperty("brokerURL", brokerValue, connectionFactory);
        } else if (connectionFactoryValue.startsWith("com.tibco.tibjms")) {
            setProperty("serverUrl", brokerValue, connectionFactory);
        } else {
            String[] brokerList = brokerValue.split(",");
            if (connectionFactoryValue.startsWith("com.ibm.mq.jms")) {
                List<String> ibmConList = new ArrayList<String>();
                for (String broker : brokerList) {
                    String[] hostPort = broker.split(":");
                    if (hostPort.length == 2) {
                        ibmConList.add(hostPort[0] + "(" + hostPort[1] + ")");
                    } else {
                        ibmConList.add(broker);
                    }
                }
                setProperty("connectionNameList", String.join(",", ibmConList), connectionFactory);
            } else {
                // Try to parse broker URI as colon separated host/port pair. Use first pair if multiple given.
                String[] hostPort = brokerList[0].split(":");
                if (hostPort.length == 2) {
                    // If broker URI indeed was colon separated host/port pair
                    setProperty("hostName", hostPort[0], connectionFactory);
                    setProperty("port", hostPort[1], connectionFactory);
                }
            }
        }
    }

    /**
     * Sets corresponding {@link ConnectionFactory}'s property to a
     * 'propertyValue' by invoking a 'setter' method that corresponds to
     * 'propertyName'. For example, 'channel' property will correspond to
     * 'setChannel(..) method and 'queueManager' property will correspond to
     * setQueueManager(..) method with a single argument.
     * <p>
     * NOTE: There is a limited type conversion to accommodate property value
     * types since all NiFi configuration properties comes as String. It is
     * accomplished by checking the argument type of the method and executing
     * its corresponding conversion to target primitive (e.g., value 'true' will
     * go thru Boolean.parseBoolean(propertyValue) if method argument is of type
     * boolean). None-primitive values are not supported at the moment and will
     * result in {@link IllegalArgumentException}. It is OK though since based
     * on analysis of several ConnectionFactory implementation the all seem to
     * follow bean convention and all their properties using Java primitives as
     * arguments.
     */
    void setProperty(String propertyName, Object propertyValue, ConnectionFactory connectionFactory) {
        String methodName = toMethodName(propertyName);
        Method[] methods =  findMethods(methodName, connectionFactory.getClass());
        if (methods != null && methods.length > 0) {
            try {
                for (Method method : methods) {
                    Class<?> returnType = method.getParameterTypes()[0];
                    if (String.class.isAssignableFrom(returnType)) {
                        method.invoke(connectionFactory, propertyValue);
                        return;
                    } else if (int.class.isAssignableFrom(returnType)) {
                        method.invoke(connectionFactory, Integer.parseInt((String) propertyValue));
                        return;
                    } else if (long.class.isAssignableFrom(returnType)) {
                        method.invoke(connectionFactory, Long.parseLong((String) propertyValue));
                        return;
                    } else if (boolean.class.isAssignableFrom(returnType)) {
                        method.invoke(connectionFactory, Boolean.parseBoolean((String) propertyValue));
                        return;
                    }
                }
                methods[0].invoke(connectionFactory, propertyValue);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to set property " + propertyName, e);
            }
        } else if (propertyName.equals("hostName")) {
            setProperty("host", propertyValue, connectionFactory); // try 'host' as another common convention.
        }
    }

    /**
     * Will convert propertyName to a method name following bean convention. For
     * example, 'channel' property will correspond to 'setChannel method and
     * 'queueManager' property will correspond to setQueueManager method name
     */
    private String toMethodName(String propertyName) {
        char[] c = propertyName.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return "set" + new String(c);
    }

    /**
     * Finds a method by name on the target class. If more then one method
     * present it will return the first one encountered.
     *
     * @param name        method name
     * @param targetClass instance of target class
     * @return Array of {@link Method}
     */
    public static Method[] findMethods(String name, Class<?> targetClass) {
        Class<?> searchType = targetClass;
        ArrayList<Method> fittingMethods = new ArrayList<>();
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
            for (Method method : methods) {
                if (name.equals(method.getName())) {
                    fittingMethods.add(method);
                }
            }
            searchType = searchType.getSuperclass();
        }
        if (fittingMethods.isEmpty()) {
            return null;
        } else {
            //Sort so that in case there are two methods that accept the parameter type
            //as first param use the one which accepts fewer parameters in total
            Collections.sort(fittingMethods, Comparator.comparing(Method::getParameterCount));
            return fittingMethods.toArray(new Method[fittingMethods.size()]);
        }
    }
}
