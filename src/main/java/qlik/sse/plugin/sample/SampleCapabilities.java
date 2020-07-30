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
package qlik.sse.plugin.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.plugin.PluginCapabilities;

/**
 * The function IDs for the functions associated with this
 * server-side extension.
 */
public class SampleCapabilities extends PluginCapabilities {
    private static final Logger LOG = LoggerFactory.getLogger(SampleCapabilities.class);

    public static final int HELLO_WORLD = 0;
    public static final int SUM_OF_ROWS = 1;
    public static final int SUM_OF_COLUMN = 2;
    public static final int STRING_AGGREGATION = 3;
    public static final int CACHE = 4;
    public static final int NO_CACHE = 5;

    private static final boolean allowScripts = true;
    private static final String pluginIdentifier = "Qlik java plugin basic example"; // a short descriptive identifier.
    private static final String pluginVersion = "v1.0.0";

    /**
     * Constructor. Builds a Map of functions.
     */
    public SampleCapabilities() {
        super();
        LOG.debug("initializing plugin capabilities");
        setPluginInfo(pluginIdentifier, pluginVersion, allowScripts);
        putFunction(HELLO_WORLD, new HelloWorld());
        putFunction(SUM_OF_ROWS, new SumOfRows());
        putFunction(SUM_OF_COLUMN, new SumOfColumn());
        putFunction(STRING_AGGREGATION, new StringAggregation());
        putFunction(CACHE, new Cache());
        putFunction(NO_CACHE, new NoCache());
    }

}
