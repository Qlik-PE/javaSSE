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

import com.idealista.fpe.FormatPreservingEncryption;
import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;
import com.idealista.fpe.component.functions.prf.DefaultPseudoRandomFunction;
import com.idealista.fpe.config.Domain;
import com.idealista.fpe.config.GenericDomain;
import com.idealista.fpe.config.GenericTransformations;
import com.idealista.fpe.config.LengthRange;
import com.idealista.fpe.config.basic.BasicAlphabet;
import com.idealista.fpe.transformer.IntToTextTransformer;
import com.idealista.fpe.transformer.TextToIntTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * A class that performs AES encryption / decryption on strings.
 */
public class FPEEncryption {
    private static final Logger LOG = LoggerFactory.getLogger(FPEEncryption.class);
    FormatPreservingEncryption fpe;
    byte[] aTweak = "MyTweakDta".getBytes();
    SecretKey secretKey;


    /**
     * The default constructor.
     */
    public FPEEncryption() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            secretKey = keyGen.generateKey();
            fpe = fpeCustom();
        } catch (Exception e) {
            LOG.error("Key generation failed", e);
        }
    }

    private FormatPreservingEncryption fpeDefaults() {
        //  taking all defaults for simplicity.
        return FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDefaultDomain()
                .withDefaultPseudoRandomFunction(secretKey.getEncoded())
                .withDefaultLengthRange()
                .build();
    }

    private FormatPreservingEncryption fpeCustom() {
        BasicAlphabet alphabet = new BasicAlphabet();
        TextToIntTransformer text_to_int_transformer = new GenericTransformations(alphabet.availableCharacters());
        IntToTextTransformer int_to_text_transformer = new GenericTransformations(alphabet.availableCharacters());
        Domain domain = new GenericDomain(alphabet, text_to_int_transformer, int_to_text_transformer);
        Integer max_length = Integer.MAX_VALUE;
        Integer min_length = 2;
        LengthRange LENGTH_RANGE = new LengthRange(min_length, max_length);

        return FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(domain)
                .withPseudoRandomFunction(new DefaultPseudoRandomFunction(secretKey.getEncoded()))
                .withLengthRange(new LengthRange(2, 30))
                .build();
    }

    /**
     * Encrypt a string using FPE encryption.
     * @param strToEncrypt the string to encrypt.
     * @return the encrypted string
     */
    public String encrypt(String strToEncrypt) {
        String returnValue = "string not encrypted";
        if (fpe ==null)
            LOG.error("fpe is null!!!");
        if (strToEncrypt == null)
            LOG.warn("input string is null");
        else {
            LOG.debug(String.format("input value:%s  length(%d) aTweak: %s", strToEncrypt, strToEncrypt.length(), Arrays.toString(aTweak)));
            returnValue = fpe.encrypt(strToEncrypt, aTweak);
        }
        LOG.debug("returning. Encrypted value: " + returnValue);
        return returnValue;
    }


    /**
     * Decrypt a string that was previously encoded using FPE
     * @param strToDecrypt a base64-encoded string to decrypt.
     * @return the decrypted string.
     */
    public String decrypt(String strToDecrypt) {
        return fpe.decrypt(strToDecrypt, aTweak);
    }

    /**
     * A quick test function. This needs to move to JUnit at some point.
     */
    public void test() {
        String plainText = "This is a plain text which need to be encrypted by Java AES 256 Algorithm in CBC Mode";

        LOG.info("Original Text  : "+ plainText);

        FPEEncryption encrypter = new FPEEncryption();
        String cipherText = encrypter.encrypt(plainText);
        LOG.info("Encrypted Text : " + cipherText);

        FPEEncryption decrypter = new FPEEncryption();
        String decryptedText = decrypter.decrypt(cipherText);
        LOG.info("DeCrypted Text : " + decryptedText);
    }
}
