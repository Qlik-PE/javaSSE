# Java Server-Side Extension Sample Plugin

The functions implemented here were taken from the java-based "basic example"
[qlik-oss/servier-side-extension/examples/java/basic_example](https://github.com/qlik-oss/server-side-extension/tree/master/examples/java/basic_example) which is part of a larger collection of examples for
various languages [qlik-oss/server-side-extension](https://github.com/qlik-oss/server-side-extension).

Note that while this example is derived from the above mentioned "basic example", you will find 
very little similarity. The code has been extensively refactored to allow for a more object-oriented
approach to implementation of a plugin; allow for more reuse; and to isolate to the greatest 
degree possible the gRPC-based server logic from the implementation of SSE functions.

To experiment with this package, set the "capabilities" property to
**qlik.sse.plugin.secsse.SampleCapabilities**. This can be done via the command line by specifying
the **--capabilities** option, or in the properties file:

    qlik.sse.capabilities = qlik.sse.plugin.aesencryption.SampleCapabilities


