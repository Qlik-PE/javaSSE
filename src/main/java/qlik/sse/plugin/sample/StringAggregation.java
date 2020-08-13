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
import qlik.sse.ServerSideExtension.DataType;
import qlik.sse.ServerSideExtension.Parameter;
import qlik.sse.ServerSideExtension.BundledRows;
import qlik.sse.ServerSideExtension.Row;
import qlik.sse.ServerSideExtension.Dual;

import qlik.sse.plugin.AggregationFunction;
import qlik.sse.plugin.PluginFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function aggregates the values of the given column as a concatenated string.
 */
public class StringAggregation extends AggregationFunction {
    private static final Logger LOG = LoggerFactory.getLogger(StringAggregation.class);

    private final StringBuilder result = new StringBuilder();

    /**
     * Initializes the class.
     */
    public StringAggregation() {
        List<Parameter> parms = new ArrayList<>();
        String name = "StringAggregation";
        int functionId = SampleCapabilities.STRING_AGGREGATION;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("columnOfStrings")
                .setDataType(DataType.STRING)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function.
     *
     * In this case, it concatenates the values of the column.
     *
     * It is called from onNext() in the plugin.
     *
     * @param rows the rows we are to operate on
     */
    @Override
    public void aggregation (BundledRows rows) {
        LOG.debug("Function StringAggregation called.");
        StringBuilder strBuilder = new StringBuilder();
        for(Row row : rows.getRowsList()) {
            strBuilder.append(row.getDuals(0).getStrData());
        }
        result.append(strBuilder.toString());
        LOG.debug("Function StringAggregation completed.");
    }

    /**
     * Aggregation is complete. Combine intermediate results.
     *
     * It is called from onCompleted() in the plugin.
     *
     * @return the accumulated total as an instance of BundledRows.
     */
    public BundledRows reduce() {
        String output = result.toString();
        result.setLength(0);  // reset the buffer for next time.

        BundledRows.Builder bundledRowsBuilder = BundledRows.newBuilder();
        return bundledRowsBuilder.addRows(Row.newBuilder()
                .addDuals(Dual.newBuilder().setStrData(output))).build();
    }
}
