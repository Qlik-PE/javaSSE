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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * A class that performs AES encryption / decryption on strings.
 */
public class AESEncryption {
    private static final Logger LOG = LoggerFactory.getLogger(qlik.sse.plugin.secsse.AESEncryption.class);
    private Cipher encryptCipher, decryptCipher;

    /**
     * The default constructor.
     */
    public AESEncryption() {
        // todo: get this info from the properties file.
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // a nonce. need external value.
        String secretKey = "boooooooooom!!!!";
        String salt = "ssshhhhhhhhhhh!!!!";

        IvParameterSpec ivspec = new IvParameterSpec(iv);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
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
            LOG.info(String.format("Before: %s  encrypted: %s", strToEncrypt, returnValue));
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
            LOG.info(String.format("Before: %s  encrypted: %s", strToDecrypt, returnValue));

        }
        catch (Exception e) {
            LOG.error("Error while decrypting: ", e);
            returnValue = null;
        }
        return returnValue;
    }

    /**
     * A quick test function. This needs to move to JUnit at some point.
     */
    public void test() {
        String plainText = "This is a plain text which need to be encrypted by Java AES 256 Algorithm in CBC Mode";

        LOG.info("Original Text  : "+ plainText);

        AESEncryption encrypter = new AESEncryption();
        String cipherText = encrypter.encrypt(plainText);
        LOG.info("Encrypted Text : " + cipherText);

        AESEncryption decrypter = new AESEncryption();
        String decryptedText = decrypter.decrypt(cipherText);
        LOG.info("DeCrypted Text : " + decryptedText);
    }
}
