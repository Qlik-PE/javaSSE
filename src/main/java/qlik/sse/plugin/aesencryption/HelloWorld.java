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
package qlik.sse.plugin.aesencryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.ServerSideExtension.DataType;
import qlik.sse.ServerSideExtension.Parameter;
import qlik.sse.ServerSideExtension.BundledRows;
import qlik.sse.ServerSideExtension.Row;
import qlik.sse.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.ScalarFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple function that sets the value of any column it receives
 * to "Hello World" and returns the result.
 */
public class HelloWorld extends ScalarFunction {
    private static final Logger LOG = LoggerFactory.getLogger(HelloWorld.class);

    /**
     * Initializes the class.
     */
    public HelloWorld() {
        super();

        List<Parameter> parms = new ArrayList<>();
        String name = "HelloWorld";
        int functionId = AESCapabilities.HELLO_WORLD;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("str1")
                .setDataType(DataType.STRING)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function.
     *
     * It sets the value of the column data it receives
     * to "Hello World".
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows scalar(BundledRows rows) {
        LOG.debug("Function secsse.HelloWorld called.");
        BundledRows.Builder result = BundledRows.newBuilder();
        Row.Builder rowBuilder;
        Dual.Builder dualBuilder;

        for (Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();
            dualBuilder = Dual.newBuilder();
            result.addRows(rowBuilder.addDuals(dualBuilder.setNumData(0).setStrData("Hello World")));
        }
        LOG.debug("Function secsse.HelloWorld completed.");
        return result.build();
    }

}
