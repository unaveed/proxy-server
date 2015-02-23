# proxy-server
A proxy server with caching and multi-client support.

Usage:
java Server [port number]

[port number] is optional, without providing a port number for the server to run on, it will default to 2112

To connection with telnet:
telnet localhost [port number]

[port number] of the server

Accepts GET command for HTTP/1.0 requests. Works with Firefox if Firefox is configured to use a proxy server.

