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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.ServerSideExtension.FunctionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The function IDs for the functions associated with this
 * server-side extension.
 */
public class PluginCapabilities {
    private static final Logger LOG = LoggerFactory.getLogger(PluginCapabilities.class);

    private final Map<Integer, PluginFunction> functionMap;
    private String pluginIdentifier;
    private String pluginVersion;
    private boolean allowScripts;


    /**
     * Constructor. Builds a Map of functions.
     */
    public PluginCapabilities() {
        super();
        functionMap = new HashMap<>();
    }

    /**
     * Set information about this plugin.
     * @param pluginIdentifier a short string identifying this plugin.
     * @param pluginVersion a String representing the version number
     * @param allowScripts true = enable scripting, false = disable scripting.
     */
    public void setPluginInfo(String pluginIdentifier, String pluginVersion, boolean allowScripts) {
        this.pluginIdentifier = pluginIdentifier;
        this.pluginVersion = pluginVersion;
        this.allowScripts = allowScripts;
    }

    /**
     * Add a function to the function map.
     *
     * @param id the function id value
     * @param function an instance of PluginFunction
     */
    public void putFunction(int id, PluginFunction function) {
        functionMap.put(id, function);
    }

    /**
     * Boolean indicating whether scripts should be enabled.
     * @return true to allow, false otherwise.
     */
    public boolean getAllowScripts() {
        return allowScripts;
    }

    /**
     * Return the integer representing the plugin function.
     * @return the plugin identifier.
     */
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    /**
     * return the version of this plugin configuration.
     * @return the version information.
     */
    public String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * Gets the information about the functions in this plugin.
     *
     * @return a Map mapping function id to the plugin function.
     */
    public Map<Integer, PluginFunction> getFunctionMap() {
        return functionMap;
    }

    /**
     * Get the SSE function definitions for the plugin's functions.
     * @return a List of function definitions.
     */
    public List<FunctionDefinition> getFunctionDefinitionList() {
        List<FunctionDefinition> list = new ArrayList<>();

        for (PluginFunction function : functionMap.values())
            list.add(function.getFunctionDefinition());

        return list;
    }

    /**
     * Gets the plugin function for this function id.
     * @param functionId the id of this function.
     * @return the PluginFunction, or null if the functionId is not valid.
     */
    public PluginFunction getPluginFunction(int functionId) {
        PluginFunction function;
        function = functionMap.getOrDefault(functionId, null);

        if (function == null) {
            LOG.error(String.format("Invalid function ID received: %d", functionId));
        }

        return function;
    }
}
