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
import qlik.sse.generated.ServerSideExtension;
import qlik.sse.generated.ServerSideExtension.BundledRows;
import qlik.sse.generated.ServerSideExtension.DataType;
import qlik.sse.generated.ServerSideExtension.Parameter;
import qlik.sse.generated.ServerSideExtension.Row;
import qlik.sse.generated.ServerSideExtension.Dual;

import qlik.sse.plugin.AggregationFunction;
import qlik.sse.plugin.PluginFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function aggregates the values of the given column.
 */
public class SumOfColumn extends AggregationFunction {
    private static final Logger LOG = LoggerFactory.getLogger(SumOfColumn.class);

    private final List<Double> results = new ArrayList<>();

    /**
     * Initializes the class.
     */
    public SumOfColumn() {
        List<Parameter> parms = new ArrayList<>();
        String name = "SumOfColumn";
        int functionId = SampleCapabilities.SUM_OF_COLUMN;
        DataType returnType = DataType.NUMERIC;

        parms.add(Parameter.newBuilder()
                .setName("column")
                .setDataType(DataType.NUMERIC)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function. In this case, it
     * totals the values of the column. It is called from onNext()
     * in the plugin.
     *
     * @param rows the rows we are to operate on
     */
    @Override
    public void aggregation (BundledRows rows) {
        LOG.debug("Function SumOfColumn called.");
        double columnSum = 0;
        for(ServerSideExtension.Row row : rows.getRowsList()) {
            columnSum += row.getDuals(0).getNumData();
        }

        LOG.debug("Function SumOfColumn completed.");
        results.add(columnSum);
    }

    /**
     * This function aggregates the total values that we have
     * accumulated from multiple calls to aggregation(). It is called
     * from onCompleted() in the plugin.
     *
     * @return the accumulated total as an instance of BundledRows.
     */
    public BundledRows reduce() {
        double sum = 0;
        for(double d : results) {
            sum += d;
        }
        LOG.debug("sum(List<Double>) completed with sum: " + sum + ".");
        results.clear();  // reset for next time
        BundledRows.Builder bundledRowsBuilder = BundledRows.newBuilder();
        return bundledRowsBuilder.addRows(Row.newBuilder()
                        .addDuals(Dual.newBuilder().setNumData(sum))).build();
    }
}
