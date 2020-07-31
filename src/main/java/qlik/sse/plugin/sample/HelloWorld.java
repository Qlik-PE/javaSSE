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
import qlik.sse.generated.ServerSideExtension.BundledRows;
import qlik.sse.generated.ServerSideExtension.DataType;
import qlik.sse.generated.ServerSideExtension.Parameter;
import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple function that returns the data it is passed without
 * any alteration. It is a smoke test to verify that the Qlik Sense
 * engine can can communicate with this SSE plugin. A message
 * gets logged by the logger on this side of things.
 */
public class HelloWorld extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorld.class);

    /**
     * Initializes the class.
     */
    public HelloWorld() {
        super();

        List<Parameter> parms = new ArrayList<>();
        String name = "HelloWorld";
        int functionId = SampleCapabilities.HELLO_WORLD;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("str1")
                .setDataType(DataType.STRING)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function. In this case, it
     * simply logs a message and returns the rows that we received
     * as is.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows tensor(BundledRows rows) {
        LOG.info("helloWorld called (and completed).");
        return rows;
    }

}
