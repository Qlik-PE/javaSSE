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
package qlik.sse.plugin.secsse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qlik.sse.ServerSideExtension.DataType;
import qlik.sse.ServerSideExtension.Parameter;
import qlik.sse.ServerSideExtension.BundledRows;
import qlik.sse.ServerSideExtension.Row;
import qlik.sse.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function uses AES to encrypt string columns that are passed to it.
 */
public class AESEncryptData extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(qlik.sse.plugin.secsse.AESEncryptData.class);

    private final AESEncryption encrypter;
    /**
     * Initializes the class.
     */
    public AESEncryptData() {
        super();

        encrypter = new AESEncryption();

        List<Parameter> parms = new ArrayList<>();
        String name = "AESEncryptData";
        int functionId = SecSSECapabilities.AES_ENCRYPT_DATA;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("str1")
                .setDataType(DataType.STRING)
                .build());
        parms.add(Parameter.newBuilder()
                .setName("str2")
                .setDataType(DataType.STRING)
                .build());

        init(name, functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function.
     * <p>
     * It uses AES to encrypt the input values before returning them.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows tensor(BundledRows rows) {
        LOG.debug("Function AESEncryptData called.");
        BundledRows.Builder result = BundledRows.newBuilder();
        Row.Builder rowBuilder;
        Dual.Builder dualBuilder;

        for (Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();

            for (Dual dual : row.getDualsList()) {
                dualBuilder = Dual.newBuilder();
                rowBuilder.addDuals(dualBuilder.setStrData(encrypter.encrypt(dual.getStrData())));
            }
            result.addRows(rowBuilder);
        }
        LOG.debug("Function AESEncryptData completed.");
        return result.build();
    }
}
