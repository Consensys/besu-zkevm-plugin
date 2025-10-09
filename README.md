# Besu-ZkEVM Plugin

The **Besu-ZkEVM Plugin** enhances Besu by adding the `debug_executionWitness` RPC endpoint, allowing clients to retrieve execution witnesses for blocks.

## Quick Start

Build the plugin using the Gradle wrapper:
```bash
./gradlew build
```
Then, run the Besu Docker container with the plugin mounted as a volume.

## Download

The latest plugin release is available on the releases page:
https://github.com/Consensys/besu-zkevm-plugin/releases

To use a release version:

1. Download the plugin JAR.
2. Create a `plugins/` folder in your Besu installation directory.
3. Copy the plugin JAR into the `plugins/` directory.
4. Start Besu with the configurations listed below.

## Features

The plugin enhances Besu by:

* Adding the `debug_executionWitness` RPC endpoint to retrieve execution witnesses for blocks.

## Configuration

Besu will autodetect the plugin and load it at startup. Some additional configuration may be needed to expose the RPC endpoint.

### Enable RPC-HTTP-API Endpoint

The plugin registers a `debug` namespace for the `executionWitness` RPC. Enable the namespace in Besu's configuration using:
```
--rpc-http-api=DEBUG
```
### Docker Example

You can run Besu with the plugin using Docker:
```bash
docker run -it -v <FOLDER_CONTAINING_PLUGIN_JAR>:/opt/besu/plugins \
hyperledger/besu:<version> \
--rpc-http-enabled=true \
--rpc-http-host="0.0.0.0" \
--rpc-http-api=DEBUG \
--rpc-http-cors-origins="*"
```
After startup, you can query the execution witness RPC:
```json
{
"jsonrpc": "2.0",
"method": "debug_executionWitness",
"params": ["0x1"],
"id": 1
}
```
## Development

### Building from Source
```bash
./gradlew build
```
The resulting JAR file will be in build/libs.

### Installation from Source

1. Create a `plugins` directory in your Besu installation directory:
```bash
mkdir -p /opt/besu/plugins
```

2. Copy the built JAR into the `plugins` directory:
```bash
cp build/libs/besu-zkevm-plugin-<version>.jar /opt/besu/plugins
```
