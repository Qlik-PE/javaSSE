/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qlik.sse.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.internal.UnrecognizedArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import static net.sourceforge.argparse4j.impl.Arguments.store;

/**
 * This class contains the main() entry point for creating a gRPC server
 * supporting Qlik Server-Side Extension (SSE) functionality.
 *
 * Server-side extensions (SSE) are based on gRPC and allow you to extend the Qlik
 * built-in expression library with functionality from external calculation engines.
 * You can use external calculation engines in both load scripts and charts.
 */
public class PluginServer {

    private static final Logger LOG = LoggerFactory.getLogger(PluginServer.class);
    private static Properties properties;
    private final Server server;
    private final int port;

    /**
     * Generates an instance of PluginServer.
     *
     * @param props an instance of java.util.properties containing properties for this application.
     */
    public PluginServer(Properties props) {
        String pemDir;
        Plugin plugin;
        String className;
        Class<Plugin> clazz;

        // retrieve configuration parameters from the application properties.
        port = Integer.parseInt(props.getProperty(ServerProperties.PORT));
        pemDir = props.getProperty(ServerProperties.PEM_DIR);

        // instantiate an instance of the plugin class specified in the properties file.
        className = props.getProperty(ServerProperties.PLUGIN);
        try {
          //noinspection unchecked
          clazz = (Class<Plugin>) Class.forName(className);
          plugin = clazz.newInstance();
        } catch(Exception e) {
          LOG.error("Could not instantiate plugin.", e);
          throw new RuntimeException(e);
        }

        if(!pemDir.isEmpty()) {
            /*
             * PEM directory has been specified, so instantiate the server using the PEM files
             * to establish a secure connection.
             */
            try {
                server = ServerBuilder.forPort(port)
                        .useTransportSecurity(new File(pemDir, "sse_server_cert.pem"), new File(pemDir, "sse_server_key.pk8"))
                        .addService(plugin)
                        .intercept(new PluginServerInterceptor(plugin))
                        .build();
            } catch (Exception e) {
                LOG.error("Could not create a secure connection.", e);
                throw new RuntimeException(e);
            }
            
        } else {
            /*
             * No PEM directory was specified, so instantiate the server without
             * configuring transport security.
             */
            server = ServerBuilder.forPort(port)
                    .addService(plugin)
                    .intercept(new PluginServerInterceptor(plugin))
                    .build();
        }
    }

    /**
     * Bind and start the server. After this call returns, clients may begin connecting to the listening sockets.
     *
     * @throws IOException if unable to bind
     */
    public void start() throws IOException {

        server.start();
        LOG.info("Server started, listening on " + port + ".");
        Runtime.getRuntime().addShutdownHook(new Thread(PluginServer.this::stop));
    }

    /**
     * Initiates an orderly shutdown in which preexisting calls continue but new calls are
     * rejected. After this call returns, this server has released the listening socket(s)
     * and may be reused by another server. Note that this method will not wait for
     * prexisting calls to finish before returning.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Waits for the server to become terminated. Returns immediately if the server has not already been
     * created.
     *
     * @throws InterruptedException if the process is interrupted.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


   /**
    * Generates an instance of ArgumentParser for parsing the
    * command line for PluginServer.
    * @return an instance of ArgumentParser.
    */
   public static ArgumentParser argParser() {
      ArgumentParser parser = ArgumentParsers
                .newFor("SSEPlugin")
                .build()
                .defaultHelp(true)
                .description("A java-based server for Qlik Sense Server Side Extensions.");

      parser.addArgument("--port")
                .action(store())
                .required(false)
                .type(String.class)
                .metavar("PORT")
                .dest(ServerProperties.PORT)
                .help("The port that the server will listen on");

      parser.addArgument("--pemdir")
                .action(store())
                .required(false)
                .type(String.class)
                .metavar("PEM_DIR")
                .dest(ServerProperties.PEM_DIR)
                .help("The directory where pem-related files are stored. Required to establish secure connections.");

       parser.addArgument("--capabilities")
               .action(store())
               .required(false)
               .type(String.class)
               .metavar("FUNCTION_LIST")
               .dest(ServerProperties.CAPABILITIES)
               .help("A class that lists/defines the capabilities (functions) of this SSE server");

       parser.addArgument("--plugin")
               .action(store())
               .required(false)
               .type(String.class)
               .metavar("PLUGIN")
               .dest(ServerProperties.PLUGIN)
               .help("The main plugin class to load");

       parser.addArgument("--properties-file")
                .action(store())
                .required(false)
                .type(String.class)
                .metavar("PROPERTIES_FILE")
                .dest(ServerProperties.PROPERTIES_FILE)
                .help("The path to a properties file where these options may also be set");

      return parser;
   }

   /**
    * Assembles the application properties from various sources:
    * <ul>
    *     <li>default properties</li>
    *     <li>external properties file, which take precedence over the defaults</li>
    *     <li>command line arguments, which take precedence over the properties file</li>
    * </ul>
    * @param parser the parser for the command line arguments.
    * @param args the command line arguments
    * @return an instance of java.util.Properties
    * @throws ArgumentParserException for invalid command line arguments.
    */
   public static Properties loadProperties(ArgumentParser parser, String[] args) throws ArgumentParserException {
       Namespace res = null;
       try {
          res = parser.parseArgs(args);
      } catch (HelpScreenException e) {
          // we displayed the help screen, so exit.
          System.exit(0);
      } catch (UnrecognizedArgumentException e) {
          System.out.printf("unrecognized argument: %s%n", e.getMessage());
          parser.printHelp();
          System.exit(0);
      }
      System.out.println(res.toString());
      Map<String, Object> commandLineProps = res.getAttrs();

      String propertiesFile = res.getString(ServerProperties.PROPERTIES_FILE);

      // initialize with the default properties
      Properties serverProps = new Properties(ServerProperties.getDefaultProperties());

      // now override the defaults with anything specified in an external properties file
      if (propertiesFile != null) {
         try {
            FileInputStream fis =new FileInputStream(propertiesFile);
            Properties props = new Properties();
            props.load(fis);
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
               if (entry.getValue() != null) {
                  System.out.println("Key:" + entry.getKey());
                  System.out.println("Value:" + entry.getValue());
                  serverProps.setProperty((String)entry.getKey(), (String)entry.getValue());
               }
            }
            PluginServer.printProperties("properties file", props);
         } catch (IOException e) {
                throw new ArgumentParserException(e.getMessage(), parser);
         }

      }
      // finally, override properties with anything specified on the command line
      for (Map.Entry<String, Object> entry : commandLineProps.entrySet()) {
        if (entry.getValue() != null) {
           serverProps.setProperty(entry.getKey(), (String)entry.getValue());
        }
      }

      return serverProps;
   }

   /**
    * Print the properties in this instance of java.util.properties.
    * @param msg a message to surround this dump of the properties.
    * @param props the instance of java.util.properties to dump.
    */
   public static void printProperties(String msg, Properties props) {
      System.out.println("Dumping properties: " + msg);
      PrintWriter writer = new PrintWriter(System.out);
      props.list(writer);
      writer.flush();
      System.out.println("End of dump for: " + msg);
   }

    /**
     * Return the properties that have been set for this application. This allows the
     * contents of the properties file to be used elsewhere in the application.
     *
     * @return an instance of java.util.Properties.
     */
   public static Properties getProperties() { return properties; }


    public static void main(String[] args) throws Exception {
        ArgumentParser parser = argParser();
        properties = loadProperties(parser, args);

        PluginServer pluginServer = new PluginServer(getProperties());
        pluginServer.start();
        pluginServer.blockUntilShutdown();

    }
}

