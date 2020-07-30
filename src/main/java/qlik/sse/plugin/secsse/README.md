# SecSSE Server-Side Extension

This is a java-based reimagining of the node.js-based 
[Qlik Sense Analytic Extension for Security](https://github.com/flautrup/SecSSE).

All of the SSE functions have been implemented here, although at present the
FPE (Format-Preserving Encryption) functions do not work. There seems to be a bug in 
the package we utilized for implementation 
([idealista/format-preserving-encryption-java](https://github.com/idealista/format-preserving-encryption-java)). We will go back and revisit this when time allows.

To experiment with this package, set the "capabilities" property to
**qlik.sse.plugin.secsse.SecSSECapabilities**. This can be done via the command line by specifying
the **--capabilities** option, or in the properties file:

    qlik.sse.capabilities = qlik.sse.plugin.aesencryption.SecSSECapabilities


