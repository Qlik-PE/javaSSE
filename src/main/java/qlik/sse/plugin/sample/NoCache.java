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
import qlik.sse.generated.ServerSideExtension.DataType;
import qlik.sse.generated.ServerSideExtension.Parameter;
import qlik.sse.generated.ServerSideExtension.BundledRows;
import qlik.sse.generated.ServerSideExtension.Row;
import qlik.sse.generated.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This function returns the column data with the date appended to
 * the column value. The function has engine caching enabled (the default),
 * so subsequent calls will execute and cause another date to be appended
 * to the column.
 */
public class NoCache extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(NoCache.class);

    /**
     * Initializes the class.
     */
    public NoCache() {
        super();

        List<Parameter> parms = new ArrayList<>();
        String name = "NoCache";
        int functionId = SampleCapabilities.NO_CACHE;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("columnOfStrings")
                .setDataType(DataType.STRING)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.NO_CACHE);
    }

    /**
     * The execution logic for this SSE function. In this case, it
     * modifies the contents of the column by appending a date.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows tensor(BundledRows rows) {
        LOG.debug("Function NoCache called.");

        BundledRows.Builder bundledRowsBuilder = BundledRows.newBuilder();
        Row.Builder rowBuilder;
        Dual.Builder dualBuilder;

        for(Row row : rows.getRowsList()) {
            String str = String.format("%s+++%s", row.getDuals(0).getStrData(), new Date().toString());
            rowBuilder = Row.newBuilder();
            dualBuilder = Dual.newBuilder();
            bundledRowsBuilder.addRows(rowBuilder.addDuals(dualBuilder.setStrData(str)));
        }
        LOG.debug("Function NoCache completed.");
        return bundledRowsBuilder.build();
    }

}
