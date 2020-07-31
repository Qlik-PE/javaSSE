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
import qlik.sse.generated.ServerSideExtension.DataType;
import qlik.sse.generated.ServerSideExtension.Parameter;
import qlik.sse.generated.ServerSideExtension.BundledRows;
import qlik.sse.generated.ServerSideExtension.Row;
import qlik.sse.generated.ServerSideExtension.Dual;

import qlik.sse.plugin.PluginFunction;
import qlik.sse.plugin.TensorFunction;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * This function creates a SHA-256 hash on string columns that are passed to it.
 */
public class SHA256HashData extends TensorFunction {
    private static final Logger LOG = LoggerFactory.getLogger(SHA256HashData.class);
    MessageDigest  md;

    /**
     * Initializes the class.
     */
    public SHA256HashData() {
        super();

        try {
            // Static getInstance method is called with hashing SHA
            md = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e) {
            LOG.error("Failed to create message digest", e);
        }

        List<Parameter> parms = new ArrayList<>();
        String name = "SHA256HashData";
        int functionId = SecSSECapabilities.SHA256_HASH_DATA;
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
     * It returns a SHA-256 hash of the input values before returning them.
     *
     * @param rows the rows we are to operate on
     * @return the result set
     */
    @Override
    public BundledRows tensor(BundledRows rows) {
        LOG.debug("Function SHA256HashData called.");
        BundledRows.Builder result = BundledRows.newBuilder();
        Row.Builder rowBuilder;
        Dual.Builder dualBuilder;

        for (Row row : rows.getRowsList()) {
            rowBuilder = Row.newBuilder();

            for (Dual dual : row.getDualsList()) {
                dualBuilder = Dual.newBuilder();
                rowBuilder.addDuals(dualBuilder.setStrData(getSHA(dual.getStrData())));
            }
            result.addRows(rowBuilder);
        }
        LOG.debug("Function SHA256HashData completed.");
        return result.build();
    }

    /**
     * Calculate the SHA-25 message digest of the input string.
     * @param input the input string
     * @return the SHA-256 hash value
     */
    public String getSHA(String input) {
        byte[] messageDigest = md.digest(input.getBytes());
        String padding = "00000000000000000000000000000000";
        String returnValue;

        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashtext = no.toString(16);

        // pad with zeros of not long enough
        if (hashtext.length() < 32) {
            returnValue = padding.substring(0, 32-hashtext.length()) + hashtext;
        } else {
            returnValue = hashtext;
        }

        return returnValue;
    }
}
