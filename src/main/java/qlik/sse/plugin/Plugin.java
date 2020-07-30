package qlik.sse.plugin;

import io.grpc.Metadata;
import qlik.sse.ConnectorGrpc.ConnectorImplBase;

import java.util.Map;

/**
 * Abstract base class for creating SSE plugins.
 */
public abstract class Plugin extends ConnectorImplBase {

    /**
     * Default constructor.
     */
    public Plugin() {
        super();
    }

    /**
     * Set the meta data for this plugin.
     * @param metadata the meta data
     */
    public abstract void setMetadata(Metadata metadata);

    /**
     * Determine whether the results of this function execution should be cached.
     * @param id the function ID
     * @return true if caching should be enabled, false otherwise.
     */
    public abstract boolean getFunctionCaching(int id);
}
