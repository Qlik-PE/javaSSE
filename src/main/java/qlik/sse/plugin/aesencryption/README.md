# AES Encryption/Decryption  Server-Side Extension

This is a java-based Server Side Extension that allows you to encrypt fields as they are loaded
from an external source, and then decrypt them as they are used in visualizations.

To experiment with this package, set the "capabilities" property to
**qlik.sse.plugin.secsse.AESCapabilities**. This can be done via the command line by specifying
the **--capabilities** option, or in the properties file:

    qlik.sse.capabilities = qlik.sse.plugin.aesencryption.AESCapabilities

Additionally, you can set nonce, secret key, and salt values in the properties file if 
you choose not to take the default values:

    # The nonce should be an ASCII string 16-bytes in length. If you
    # specify a shorter string it will be padded; a longer string will be truncated.
    qlik.sse.plugin.aes.nonce = "QasFQWZSMDLOIG78"
    qlik.sse.plugin.aes.key = "your-secret-key"
    qlik.sse.plugin.aes.salt = "your-salt-string"

