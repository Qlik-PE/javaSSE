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
import qlik.sse.generated.ServerSideExtension.DataType;
import qlik.sse.generated.ServerSideExtension.Parameter;
import qlik.sse.generated.ServerSideExtension.BundledRows;
import qlik.sse.generated.ServerSideExtension.Row;
import qlik.sse.generated.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function uses AES to encrypt string columns that are passed to it.
 */
public class AESEncryptData extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(AESEncryptData.class);

    private final AESEncryption encrypter;
    /**
     * Initializes the class.
     */
    public AESEncryptData() {
        super();

        encrypter = new AESEncryption();

        List<Parameter> parms = new ArrayList<>();
        String name = "AESEncryptData";
        int functionId = AESCapabilities.AES_ENCRYPT_DATA;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("value")
                .setDataType(DataType.STRING)
                .build());

        parms.add(Parameter.newBuilder()
                .setName("id")
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

        for (Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();

            List<Dual> dualList = row.getDualsList();
            Dual value = dualList.get(0);  // encrypt this value
            Dual id = dualList.get(1);     // do not encrypt the key
            rowBuilder.addDuals(Dual.newBuilder().setStrData(encrypter.encrypt(value.getStrData())));
            rowBuilder.addDuals(id);

            result.addRows(rowBuilder);
        }
        LOG.debug("Function AESEncryptData completed.");
        return result.build();
    }
}
