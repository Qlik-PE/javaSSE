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
package qlik.sse.plugin;

import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qlik.sse.ServerSideExtension.Capabilities;
import qlik.sse.ServerSideExtension.FunctionType;
import qlik.sse.ServerSideExtension.DataType;
import qlik.sse.ServerSideExtension.BundledRows;
import qlik.sse.ServerSideExtension.FunctionRequestHeader;
import qlik.sse.ServerSideExtension.ScriptRequestHeader;
import qlik.sse.ServerSideExtension.Row;
import qlik.sse.ServerSideExtension.Dual;
import qlik.sse.ServerSideExtension.Empty;

import qlik.sse.server.PluginServer;
import qlik.sse.server.ServerProperties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


import java.util.Properties;

import static io.grpc.Metadata.BINARY_BYTE_MARSHALLER;


public class JavaPlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(JavaPlugin.class);
    private final ThreadLocal<Metadata> metadata = new ThreadLocal<>();

    private final PluginCapabilities pluginCapabilities;


    /**
     * Default constructor.
     */
    public JavaPlugin() {
        String className;
        Class<PluginCapabilities> clazz;
        Properties props = PluginServer.getProperties();
        // instantiate an instance of the plugin class specified in the properties file.
        className = props.getProperty(ServerProperties.CAPABILITIES);
        try {
            //noinspection unchecked
            clazz = (Class<PluginCapabilities>) Class.forName(className);
            pluginCapabilities = clazz.newInstance();
        } catch(Exception e) {
            LOG.error("Could not instantiate plugin.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the metadata information.
     * @param metadata the meta data
     */
    @Override
    public void setMetadata(Metadata metadata) {
        this.metadata.set(metadata);
    }

    /**
     * Determine whether function execution results should be cached.
     * @param id the function ID
     * @return true if the results should be cached, false otherwise.
     */
    @Override
    public boolean getFunctionCaching(int id) {
        return pluginCapabilities.getPluginFunction(id).getQlikCache();
    }

    /**
     * Creates and returns the Capabilities for this plugin.
     * @param request and Empty request.
     * @param responseObserver where we return the gRPC response.
     */
     @Override
     public void getCapabilities(Empty request, StreamObserver<Capabilities> responseObserver) {
         
         LOG.info("getCapabilities called.");
         
         Capabilities capabilities = Capabilities.newBuilder()
             .setAllowScript(pluginCapabilities.getAllowScripts())
             .setPluginIdentifier(pluginCapabilities.getPluginIdentifier())
             .setPluginVersion(pluginCapabilities.getPluginVersion())
             .addAllFunctions(pluginCapabilities.getFunctionDefinitionList())
             .build();

         responseObserver.onNext(capabilities);
         responseObserver.onCompleted();
         LOG.debug("getCapabilities completed.");
     }

    /**
     * The gRPC entry point into the plugin when a function is executed.
     * @param responseObserver a StreamObserver that receives notifications from the gRPC message stream.
     * @return a StreamObserver
     */
     @Override
     public StreamObserver<BundledRows> executeFunction(final StreamObserver<BundledRows> responseObserver) {
         LOG.debug("executeFunction called.");
         final int functionId;

         /*
          * Gets the function ID from the FunctionRequestHeader. Returns immediately
          * if there is a problem.
          */
         try {
             functionId = FunctionRequestHeader
             .parseFrom(metadata.get().get(Metadata.Key.of("qlik-functionrequestheader-bin", BINARY_BYTE_MARSHALLER))).getFunctionId();
             LOG.debug("Function nbr " + functionId + " was called.");
         } catch (Exception e) {
             LOG.warn("Exception when trying to get the function request header.", e);
             responseObserver.onError(new Throwable("Exception when trying to get the function request header in executeFunction."));
             responseObserver.onCompleted();
             return responseObserver;
         }
         LOG.debug("executeFunction called. Function Id: " + functionId + ".");
         final FunctionType functionType;
         final PluginFunction function = pluginCapabilities.getPluginFunction(functionId);
         if (function == null) {
             String msg = String.format("Incorrect function id %d received in executeFunction.",
                     functionId);
             LOG.warn(msg);
             responseObserver.onError(new Throwable(msg));
             responseObserver.onCompleted();
             return responseObserver;
         } else {
             functionType = function.getFunctionType();
         }

         /*
          * Builds a StreamObserver as an anonymous class to return
          * from this function. It implements the functions required
          * by the StreamObserver interface.
          */
         return new StreamObserver<BundledRows>() {

             /**
              * Receives a value from the stream. It can be called many times (via callback)
              * but is never called after onError() or onCompleted().
              * @param bundledRows the rows that we need to process.
              */
             @Override
             public void onNext(BundledRows bundledRows) {
                 LOG.debug("onNext in executeFunction called.");

                 switch(functionType) {
                     case SCALAR:
                         /*
                          * return this batch of results.
                          */
                         responseObserver.onNext(((ScalarFunction)function).scalar(bundledRows));
                         break;
                     case TENSOR:
                         /*
                          * return this batch of results.
                          */
                         responseObserver.onNext(((TensorFunction)function).tensor(bundledRows));
                         break;
                     case AGGREGATION:
                         /*
                          * Aggregation functions do not return values here. A single value
                          * is returned once all rows have been processed.
                          */
                         ((AggregationFunction)function).aggregation(bundledRows);
                         break;
                     case UNRECOGNIZED:
                     default:
                         String msg = "Incorrect function type in onNext in executeFunction: " + functionType;
                         LOG.error(msg);
                         responseObserver.onError(new Throwable("Incorrect function id in onNext in executeFunction."));
                         responseObserver.onCompleted();
                         break;

                 }
                 LOG.debug("onNext in executeFunction completed.");
             }

             /**
              * Receives a terminating error from the stream.
              * May only be called once and if called it must be the last method called.
              * In particular if an exception is thrown no further calls to any method
              * may be made.
              * @param t the error that occurred on the stream.
              */
             @Override
             public void onError(Throwable t) {
                 LOG.warn("Encountered error in executeFunction.", t);
                 responseObserver.onCompleted();
             }

             /**
              * Receives a notification of successful stream completion.
              * May only be called once and if called it must be the last method called.
              */
             @Override
             public void onCompleted() {
                 LOG.debug("onCompleted in executeFunction called.");
                 if (function.isAggregation()) {
                     /*
                      * return final result from aggregation processing.
                      */
                     responseObserver.onNext(((AggregationFunction)function).reduce());
                 }
                 responseObserver.onCompleted();
                 LOG.debug("onCompleted in executeFunction completed.");
             }
         };
     }
    /**
     * The gRPC entry point into the plugin when a script is to be executed.
     * @param responseObserver a StreamObserver that receives notifications from the gRPC message stream.
     * @return a StreamObserver
     */
     @Override
     public StreamObserver<BundledRows> evaluateScript(final StreamObserver<BundledRows> responseObserver) {
         
         LOG.debug("evaluateScript called");
         final ScriptRequestHeader header;
         
         try {
             header = ScriptRequestHeader
             .parseFrom(metadata.get().get(Metadata.Key.of("qlik-scriptrequestheader-bin", BINARY_BYTE_MARSHALLER)));
             LOG.debug("Got the script request header.");
         } catch (Exception e) {
             LOG.error("Exception when trying to get the script request header.", e);
             responseObserver.onError(new Throwable("Exception when trying to get the script request header in evaluateScript."));
             responseObserver.onCompleted();
             return responseObserver;
         }

         if(header != null ) {
             if(header.getParamsCount() == 0) {
                 BundledRows result = prepareScript(header, null);
                 if(result.getRowsCount() > 0) {
                     responseObserver.onNext(result);
                 } else {
                     responseObserver.onError(new Throwable("An error occurred in prepareScript in evaluateScript."));
                 }
                 responseObserver.onCompleted();
                 LOG.debug("evaluateScript completed");
             }
         } else {
             LOG.warn("The script request header is null.");
             responseObserver.onError(new Throwable("The script request header is null in evaluateScript."));
             responseObserver.onCompleted();
         }
         
         return new StreamObserver<BundledRows>() {

             /**
              * Receives a value from the stream. It can be called many times (via callback)
              * but is never called after onError() or onCompleted().
              * @param bundledRows the rows that we need to process.
              */
             @Override
             public void onNext(BundledRows bundledRows) {
                 LOG.debug("onNext in evaluateScript called");
                 if(header != null) {
                     if(header.getFunctionType() == FunctionType.AGGREGATION) {
                         LOG.warn("Aggregation is not implemented in evaluate script.");
                         responseObserver.onCompleted();
                     }
                     BundledRows result = prepareScript(header, bundledRows);
                     if(result.getRowsCount() > 0) {
                         responseObserver.onNext(result);
                         LOG.debug("onNext in evaluateScript completed");
                     } else {
                         responseObserver.onError(new Throwable("An error occured in prepareScript in evaluateScript."));
                     }
                 } else {
                     LOG.warn("The script request header is null.");
                     responseObserver.onError(new Throwable("The script request header is null in onNext in evaluateScript."));
                     responseObserver.onCompleted();
                 }
             }

             /**
              * Receives a terminating error from the stream.
              * May only be called once and if called it must be the last method called.
              * In particular if an exception is thrown no further calls to any method
              * may be made.
              * @param t the error that occurred on the stream.
              */
             @Override
             public void onError(Throwable t) {
                LOG.warn("Encountered error in evaluateScript", t);
                 responseObserver.onCompleted();
             }

             /**
              * Receives a notification of successful stream completion.
              * May only be called once and if called it must be the last method called.
              */
             @Override
             public void onCompleted() {
                 LOG.debug("onCompleted in evaluateScript called");
                 responseObserver.onCompleted();
                 LOG.debug("onCompleted in evaluateScript completed");
             }
         };
     }

    /**
     * Extracts the script information from the header and prepares for execution.
     * Called from evaluateScript().
     *
     * @param header the ScriptRequestHeader
     * @param bundledRows the rows to be processed. May be null.
     * @return the resulting rows
     */
     private BundledRows prepareScript(ScriptRequestHeader header, BundledRows bundledRows) {
         
         LOG.debug("prepareScript called");
         ScriptEngineManager manager = new ScriptEngineManager();
         ScriptEngine engine = manager.getEngineByName("JavaScript");
         BundledRows.Builder outputRowsBuilder = BundledRows.newBuilder(); // the output from script execution
         
         String script = header.getScript();
         DataType returnType = header.getReturnType();
         int nbrOfParams = header.getParamsCount();


         if(nbrOfParams == 0) {
             /*
              * Script requires no arguments, so we only need to call it once.
              */
             executeScript(script, outputRowsBuilder, engine, returnType);
             LOG.debug("single execution of executeScript completed");
         } else {
             /*
              * Script requires an argument, so call it once per row and accumulate the results.
              */
             Object[] args;
             int cnt =0;
             for (Row row : bundledRows.getRowsList()) {
                 args = row.getDualsList().toArray();
                 engine.put("args", args);
                 LOG.trace(String.format("calling executeScript: %d", cnt++));
                 if (!executeScript(script, outputRowsBuilder, engine, returnType)) {
                     // got a bad return from the call, so bail out.
                     LOG.error("bad return from executeScript. Aborting.");
                     break;
                 }
             }
         }
         LOG.debug("prepareScript completed");
         return outputRowsBuilder.build();
     }

    /**
     * Executes the requested javascript code.
     * Called once per row. Args to the script are passed in externally via engine.put().
     *
     * @param script the script to be executed
     * @param outputRowsBuilder will contain the result set
     * @param engine the script engined (i.e. javascript)
     * @param returnType the type of data that will be returned via gRPC to the caller.
     * @return true if execution was successful, false otherwise.
     */
     private boolean executeScript(String script, BundledRows.Builder outputRowsBuilder,
                                   ScriptEngine engine, DataType returnType) {
         
         LOG.debug("executeScript called from eval script");
         String result;
         try {
             Object res = engine.eval(script);
             result = res.toString();
             LOG.debug("The string representation of the result: " + result);
         } catch (Exception e) {
             LOG.error("The script failed to execute", e);
             return false;
         }
         
         Row.Builder rowBuilder = Row.newBuilder();
         Dual.Builder dualBuilder = Dual.newBuilder();
         boolean rval = true;
         switch (returnType) {
             case STRING : 
                 outputRowsBuilder.addRows(rowBuilder.addDuals(dualBuilder.setStrData(result)));
                 break;
             case NUMERIC : 
                 outputRowsBuilder.addRows(rowBuilder.addDuals(dualBuilder.setNumData(Double.parseDouble(result))));
                 break;
             case DUAL : 
                 outputRowsBuilder.addRows(rowBuilder.addDuals(dualBuilder.setStrData(result).setNumData(Double.parseDouble(result))));
                 break;
             case UNRECOGNIZED:
             default :
                 LOG.warn("Incorrect return type.");
                 rval = false;
         }
         return rval;
     }
     
 }
