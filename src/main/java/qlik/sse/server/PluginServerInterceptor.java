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

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.ServerSideExtension.FunctionRequestHeader;
import qlik.sse.plugin.Plugin;

import java.util.Arrays;
import java.util.Set;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER; // simple metadata marshaller that encodes strings as is.
import static io.grpc.Metadata.BINARY_BYTE_MARSHALLER;  // simple metadata marshaller that encodes bytes as is.

/**
 * This class implements the interface for io.grpc.ServerInterceptor. It provides a
 * mechanism for intercepting incoming calls before they are dispatched by the
 * gRPC ServerCallHandler. It is used to add cross-cutting behavior to server-side calls.
 * Common examples of such behavior include:
 *
 * <ul>
 *     <li>Enforcing valid authentication credentials</li>
 *     <li>Logging and monitoring call behavior
 *     Delegating calls to other servers</li>
 * </ul>
 */
public class PluginServerInterceptor implements ServerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PluginServerInterceptor.class);

    private final Plugin plugin;

    /**
     * Creates and instance of this interceptor and ties it to the SSE plugin
     * that was specified in the application properties file.
     *
     * @param plugin the plugin that was specified in the application properties file
     */
    public PluginServerInterceptor(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Intercept a ServerCall dispatched by the ServerCallHandler. General semantics of
     * ServerCallHandler.startCall() apply and the returned ServerCall.Listener must
     * not be null. THis method should NOT throw exceptions.
     *
     * @param call receives response messages.
     * @param metadata contains external call metadata header info from ClientCall.start (i.e. authentication credentials)
     * @param next the next processor in the interceptor chain
     * @return a listener for processing incoming messages for 'call'. Should never be null.
     */
    @Override
    public <RequestT,ResponseT>ServerCall.Listener<RequestT> interceptCall(
            ServerCall<RequestT,ResponseT> call, final Metadata metadata,
            ServerCallHandler<RequestT,ResponseT> next) {
        LOG.debug("Intercepting call to get metadata.");
        logHeader(metadata, "listener metadata");

        // pass the metadata info to the plugin.
        plugin.setMetadata(metadata);

        return next.startCall(new SimpleForwardingServerCall<RequestT,ResponseT>(call){
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                LOG.debug("in sendHeaders");
                try {
                    if (metadata == null)
                        LOG.warn("metadata is NULL!!!");
                    /*
                     * Get the qlik SSE function request header from the metadata.
                     */
                    byte[] qlikHeader = metadata.get(Metadata.Key.of("qlik-functionrequestheader-bin", BINARY_BYTE_MARSHALLER));
                    if (qlikHeader != null) {
                        FunctionRequestHeader requestHeader = FunctionRequestHeader
                                .parseFrom(qlikHeader);

                        logHeader(responseHeaders, "response header before");

                        if (!plugin.getFunctionCaching(requestHeader.getFunctionId())) {
                            // qlikCache is false, so set the response header to disable caching.
                            LOG.debug("cache OFF: Setting qlik-cache to no-store in the response header");
                            responseHeaders.put(Metadata.Key.of("qlik-cache", ASCII_STRING_MARSHALLER), "no-store");
                        } else {
                            // qlikCache is true, so remove qlik-cache from the response header.
                            LOG.debug("cache ON: removing qlik-cache from response header");
                            responseHeaders.remove(Metadata.Key.of("qlik-cache", ASCII_STRING_MARSHALLER), "no-store");
                        }
                        logHeader(responseHeaders, "response header after");
                    } else {
                        LOG.info("Qlik Response Header was Null.");
                    }
                } catch(Exception e) {
                    LOG.warn("exception thrown in sendHeaders. Continuing.", e);
                }
                super.sendHeaders(responseHeaders);
            }
        }, metadata);
    }

    /**
     * Log information about the Metadata info passed to the interceptor.
     * Do nothing if we are not logging debug-level messages.
     *
     * @param header the meta data info passed to the interceptor.
     * @param msg a message to include when logging.
     */
    private void logHeader(Metadata header, String msg) {
        // do nothing if we are not logging DEBUG messages.
        if (LOG.isDebugEnabled()) {
            Set<String> keys = header.keys();
            LOG.debug(msg + ": Is header empty? " + keys.isEmpty());
            for (String key : keys) {
                if (key.toLowerCase().contains("-bin")) {
                    LOG.debug(msg + ": Key: " + key + " Value: " +
                            Arrays.toString(header.get(Metadata.Key.of(key, BINARY_BYTE_MARSHALLER))));
                } else {
                    LOG.debug(msg + ": Key: " + key + " Value: " + header.get(Metadata.Key.of(key, ASCII_STRING_MARSHALLER)));
                }

            }
        }
    }
}
