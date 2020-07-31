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
import qlik.sse.plugin.ScalarFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This function encrypts a scalar value using AES.
 *
 * input: one or more rows containing a single column to encrypt.
 * output: the resulting row(s) with the column encrypted.
 */
public class AESEncryptScalar extends ScalarFunction {
    private static final Logger LOG = LoggerFactory.getLogger(AESEncryptScalar.class);

    private final AESEncryption encrypter;

    /**
     * Initializes the class.
     */
    public AESEncryptScalar() {
        super();
        encrypter = new AESEncryption();

        List<Parameter> parms = new ArrayList<>();
        String name = "AESEncryptScalar";
        int functionId = AESCapabilities.AES_ENCRYPT_SCALAR;
        DataType returnType = DataType.STRING;

        parms.add(Parameter.newBuilder()
                .setName("input")
                .setDataType(DataType.STRING)
                .build());

        init(name,functionId, returnType, parms, PluginFunction.CACHE);
    }

    /**
     * The execution logic for this SSE function.
     *
     * It encrypts the value(s) it receives.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows scalar(BundledRows rows) {
        LOG.debug("Function AESEncryptScalar called.");
        BundledRows.Builder result = BundledRows.newBuilder();
        Row.Builder rowBuilder;

        /*
         * this is a scalar function. There should only be one row and
         * one column to work with.
         */
        if (rows.getRowsCount() > 1)
            LOG.warn("row count > 1: " + rows.getRowsCount());


        for (Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();
            if (row.getDualsCount() > 1)
                LOG.warn("duals count > 1: " + row.getDualsCount());
            String input = row.getDuals(0).getStrData();
            String output;

            if (input != null) {
                output = encrypter.encrypt(input);
                if (output == null) {
                    LOG.warn("encrypter returned null");
                    output = "encrypterNull";
                }
            } else output = " input value was null";

            rowBuilder.addDuals(Dual.newBuilder()
                    .setStrData(output));
            result.addRows(rowBuilder);
        }

        LOG.debug("Function AESEncryptScalar completed.");
        return result.build();
    }

}
