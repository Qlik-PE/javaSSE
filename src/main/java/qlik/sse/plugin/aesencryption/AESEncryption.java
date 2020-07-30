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
import qlik.sse.server.PluginServer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import static qlik.sse.server.ServerProperties.*;

/**
 * A class that performs AES encryption / decryption on strings.
 */
public class AESEncryption {
    private static final Logger LOG = LoggerFactory.getLogger(AESEncryption.class);
    private Cipher encryptCipher, decryptCipher;

    /**
     * The default constructor.
     */
    public AESEncryption() {
        super();
        /*
         * get security parameters from the application properties file.
         */
        // pad nonce to ensure at least 16 bytes long, then truncate to 16 bytes.
        String nonceString = PluginServer.getProperties().getProperty(AES_NONCE) + "0000000000000000";
        byte[] nonce = nonceString.substring(0,16).getBytes();
        char[] secretKey = PluginServer.getProperties().getProperty(AES_KEY).toCharArray();
        byte[] salt = PluginServer.getProperties().getProperty(AES_SALT).getBytes();

        IvParameterSpec ivspec = new IvParameterSpec(nonce);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey, salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

            encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

        } catch(Exception e) {
            LOG.error("Error while initializing cipher: ", e);
        }
    }

    /**
     * Encrypt a string using AES encryption.
     * @param strToEncrypt the string to encrypt.
     * @return the encrypted string in base64 encoded form
     */
    public String encrypt(String strToEncrypt) {
        String returnValue;
        try {
            returnValue = Base64.getEncoder().encodeToString(encryptCipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
            LOG.info(String.format("Encrypt: Before: %s  encrypted: %s", strToEncrypt, returnValue));
        }
        catch (Exception e) {
            LOG.error("Error while encrypting: ", e);
            returnValue = null;
        }
        return returnValue;
    }


    /**
     * Decrypt a string that was previously encoded using AES
     * @param strToDecrypt a base64-encoded string to decrypt.
     * @return the decrypted string.
     */
    public String decrypt(String strToDecrypt) {
        String returnValue;
        try {
            returnValue = new String(decryptCipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            LOG.info(String.format("Decrypt: Before: %s  decrypted: %s", strToDecrypt, returnValue));

        }
        catch (Exception e) {
            LOG.error("Error while decrypting: ", e);
            returnValue = null;
        }
        return returnValue;
    }

    /**
     * A quick test function. Called as a runtime smoke test.
     *
     * @return true if encyption/decryption was a success, false otherwise.
     */
    public boolean smokeTest() {
        String plainText = "This is a plain text which need to be encrypted by Java AES 256 Algorithm in CBC Mode";
        boolean returnValue;

        LOG.info("Original Text  : " + plainText);

        AESEncryption encrypter = new AESEncryption();
        String cipherText = encrypter.encrypt(plainText);
        LOG.info("Encrypted Text : " + cipherText);

        AESEncryption decrypter = new AESEncryption();
        String decryptedText = decrypter.decrypt(cipherText);
        LOG.info("DeCrypted Text : " + decryptedText);

        if (decryptedText.compareTo(plainText) == 0) {
            LOG.info("SUCCESS: decrypted string equals the original string");
            returnValue = true;
        } else {
            LOG.error("ERROR: decrypted string does not equal the original string");
            returnValue = false;
        }
        return returnValue;
    }
}
