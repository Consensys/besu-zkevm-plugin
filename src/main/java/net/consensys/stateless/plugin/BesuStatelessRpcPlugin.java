/*
 * Copyright ConsenSys 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.consensys.stateless.plugin;

import net.consensys.stateless.context.StatelessContext;
import net.consensys.stateless.context.StatelessContext.StatelessContextImpl;
import net.consensys.stateless.rpc.methods.DebugExecutionWitnessServer;

import java.util.List;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.BlockchainService;
import org.hyperledger.besu.plugin.services.RpcEndpointService;
import org.hyperledger.besu.plugin.services.WorldStateService;
import org.hyperledger.besu.plugin.services.rlp.RlpConverterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(BesuPlugin.class)
public class BesuStatelessRpcPlugin implements BesuPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(BesuStatelessRpcPlugin.class);
  private final StatelessContext ctx = StatelessContextImpl.getOrCreate();
  private ServiceManager serviceManager;
  private PluginServiceProvider pluginServiceProvider;

  @Override
  public void register(final ServiceManager serviceManager) {
    this.serviceManager = serviceManager;

    this.pluginServiceProvider = new PluginServiceProvider();
    var methods = List.of(new DebugExecutionWitnessServer(ctx, pluginServiceProvider));
    serviceManager
        .getService(RpcEndpointService.class)
        .ifPresent(
            rpcEndpointService ->
                methods.forEach(
                    method -> {
                      LOG.info(
                          "Registering RPC plugin endpoint {}_{}",
                          method.getNamespace(),
                          method.getName());

                      rpcEndpointService.registerRPCEndpoint(
                          method.getNamespace(), method.getName(), method::execute);
                    }));
  }

  @Override
  public void start() {
    LOG.info("Loading RLP converter service");
    final RlpConverterService rlpConverterService =
        serviceManager
            .getService(RlpConverterService.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Expecting a RLP converter service, but none found."));
    pluginServiceProvider.provideService(RlpConverterService.class, () -> rlpConverterService);
    LOG.info("Loading blockchain service");
    final BlockchainService blockchainService =
        serviceManager
            .getService(BlockchainService.class)
            .orElseThrow(
                () -> new IllegalStateException("Expecting a blockchain service, but none found."));
    pluginServiceProvider.provideService(BlockchainService.class, () -> blockchainService);
    LOG.info("Loading worldstate service");
    final WorldStateService worldStateService =
        serviceManager
            .getService(WorldStateService.class)
            .orElseThrow(
                () -> new IllegalStateException("Expecting a worlstate service, but none found."));
    pluginServiceProvider.provideService(WorldStateService.class, () -> worldStateService);
  }

  @Override
  public void stop() {
    // no-op
  }
}
