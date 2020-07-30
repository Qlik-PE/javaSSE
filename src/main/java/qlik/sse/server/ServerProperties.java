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
package qlik.sse.server;

import qlik.sse.plugin.Plugin;

import java.util.Properties;

/**
 * SSE-related properties;
 */
public class ServerProperties {
    /**
     * The port number that the server should listen on.
     */
    public static final String PORT = "qlik.sse.port";

    /**
     * The default port.
     */
    public static final String PORT_DEFAULT = "50050";

    /**
     * The directory where the PEM files used for secure connections can be found.
     */
    public static final String PEM_DIR = "qlik.sse.pemdir";

    /**
     * The default value for the PEM directory, the empty string, which implies "none".
     */
    public static final String PEM_DIR_DEFAULT = "";

    /**
     * The class of the plugin we want to load.
     */
    public static final String PLUGIN = "qlik.sse.plugin";
    /**
     * The default plugin to load
     */
    public static final String PLUGIN_CLASS_DEFAULT = "qlik.sse.plugin.JavaPlugin";

    /**
     * The class of the plugin capabilities we want to load.
     */
    public static final String CAPABILITIES = "qlik.sse.capabilities";
    /**
     * Capabilities are required. Return bogus class by default.
     */
    public static final String CAPABILITIES_CLASS_DEFAULT = "capabilities.not.set.FunctionList";

    /**
     * The location of an external properties file.
     */
    public static final String PROPERTIES_FILE = "qlik.sse.plugin.propertiesfile";

    /**
     * A nonce used for AES encryption. The nature of this use case requires the nonce
     * to be constant. The nonce should be an ASCII string 16-bytes in length. If you
     * specify a shorter string it will be padded; a longer string will be truncated.
     */
    public static final String AES_NONCE = "qlik.sse.plugin.aes.nonce";
    /**
     * The default value for the nonce.
     */
    public static final String AES_NONCE_DEFAULT = "OGFhZDNkNDdhNzlm";

    /**
     * The key to be used in AES Encryption.
     */
    public static final String AES_KEY = "qlik.sse.plugin.aes.key";
    /**
     * The default AES key.
     */
    public static final String AES_KEY_DEFAULT = "MjE0YmJkZTRhNDdjMmQzNDFk";

    /**
     * The salt used to initialize the AES encryption engine.
     */
    public static final String AES_SALT = "qlik.sse.plugin.aes.salt";
    /**
     * The default AES salt.
     */
    public static final String AES_SALT_DEFAULT = "MWYwNzdmNjUy";


    /**
     * Get the default properties.
     */
    public static Properties getDefaultProperties() {
        Properties props = new Properties();

        props.setProperty(PORT, PORT_DEFAULT);
        props.setProperty(PEM_DIR, PEM_DIR_DEFAULT);
        props.setProperty(PLUGIN, PLUGIN_CLASS_DEFAULT);
        props.setProperty(CAPABILITIES, CAPABILITIES_CLASS_DEFAULT);
        props.setProperty(AES_NONCE, AES_NONCE_DEFAULT);
        props.setProperty(AES_KEY, AES_KEY_DEFAULT);
        props.setProperty(AES_SALT, AES_SALT_DEFAULT);

        return props;
    }

    /**
     * private to prevent explicit object creation
     */
    private ServerProperties() { super(); }
}
