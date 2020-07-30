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
import qlik.sse.plugin.PluginCapabilities;


/**
 * The function IDs for the functions associated with this
 * server-side extension.
 */
public class AESCapabilities extends PluginCapabilities {
    private static final Logger LOG = LoggerFactory.getLogger(AESCapabilities.class);

    public static final int HELLO_WORLD = 0;
    public static final int AES_ENCRYPT_DATA = 1;
    public static final int AES_DECRYPT_DATA = 2;
    public static final int AES_ENCRYPT_SCALAR = 3;
    public static final int AES_DECRYPT_SCALAR = 4;

    private static final boolean allowScripts = false;
    private static final String pluginIdentifier = "AES Encryption SSE"; // a short descriptive identifier.
    private static final String pluginVersion = "v1.0.0";

    /**
     * Constructor. Builds a Map of functions.
     */
    public AESCapabilities() {
        super();
        LOG.debug("initializing plugin capabilities");
        setPluginInfo(pluginIdentifier, pluginVersion, allowScripts);
        putFunction(HELLO_WORLD, new HelloWorld());
        putFunction(AES_ENCRYPT_DATA, new AESEncryptData());
        putFunction(AES_DECRYPT_DATA, new AESDecryptData());
        putFunction(AES_ENCRYPT_SCALAR, new AESEncryptScalar());
        putFunction(AES_DECRYPT_SCALAR, new AESDecryptScalar());
    }

}
