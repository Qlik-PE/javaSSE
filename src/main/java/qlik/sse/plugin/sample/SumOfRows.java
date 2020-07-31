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
import qlik.sse.generated.ServerSideExtension.Row;
import qlik.sse.generated.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function adds the values of the two columns that are passed
 * in as parameters for each row.
 */
public class SumOfRows extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(SumOfRows.class);

    /**
     * Initializes the class.
     */
    public SumOfRows() {
        super();

        List<Parameter> parms = new ArrayList<>();
        String name = "SumOfRows";
        int functionId = SampleCapabilities.SUM_OF_ROWS;
        DataType returnType = DataType.NUMERIC;

        parms.add(Parameter.newBuilder()
                .setName("col1")
                .setDataType(DataType.NUMERIC)
                .build());
        parms.add(Parameter.newBuilder()
                .setName("col2")
                .setDataType(DataType.NUMERIC)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function. It adds the values of
     * the parameters (col1 and col2) that we receive as arguments for
     * each row.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows tensor (BundledRows rows) {
        LOG.debug("Function SumOfRows called.");
        BundledRows.Builder result = BundledRows.newBuilder();
        Row.Builder rowBuilder;
        Dual.Builder dualBuilder;
        double rowSum;
        for(Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();
            dualBuilder = Dual.newBuilder();
            rowSum = 0;
            for(Dual dual : row.getDualsList()) {
                rowSum += dual.getNumData();
            }
            result.addRows(rowBuilder.addDuals(dualBuilder.setNumData(rowSum)));
        }
        LOG.debug("Function SumOfRows completed.");
        return result.build();
    }

}
