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
package net.consensys.stateless.rpc.methods;

import net.consensys.stateless.context.StatelessContext;
import net.consensys.stateless.plugin.PluginServiceProvider;
import net.consensys.stateless.rpc.response.ExecutionWitnessJsonTest;
import net.consensys.stateless.service.ExecutionWitnessService;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.BlockParameterOrBlockHash;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.worldview.BonsaiWorldState;
import org.hyperledger.besu.plugin.data.BlockContext;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.services.BlockchainService;
import org.hyperledger.besu.plugin.services.WorldStateService;
import org.hyperledger.besu.plugin.services.rlp.RlpConverterService;
import org.hyperledger.besu.plugin.services.rpc.PluginRpcRequest;
import org.hyperledger.besu.plugin.services.trielogs.TrieLog;

public class DebugExecutionWitnessServer implements PluginRpcMethod {
  private final StatelessContext ctx;
  private final PluginServiceProvider pluginServiceProvider;

  public DebugExecutionWitnessServer(
      final StatelessContext ctx, final PluginServiceProvider pluginServiceProvider) {
    this.ctx = ctx;
    this.pluginServiceProvider = pluginServiceProvider;
  }

  @Override
  public String getNamespace() {
    return "debug";
  }

  @Override
  public String getName() {
    return "executionWitness";
  }

  @Override
  public Object execute(final PluginRpcRequest rpcRequest) {
    if (!areServicesReady()) {
      return null;
    }

    final var blockchainService = pluginServiceProvider.getService(BlockchainService.class);
    final var rlpConverterService = pluginServiceProvider.getService(RlpConverterService.class);
    final var worldStateService = pluginServiceProvider.getService(WorldStateService.class);

    final Hash blockHash;
    try {
      blockHash = parseBlockHash(rpcRequest, blockchainService);
    } catch (InvalidParameterException e) {
      return null;
    }

    final Optional<TrieLog> trieLogLayer = getTrieLogLayer(blockHash);
    if (trieLogLayer.isEmpty()) {
      return null;
    }

    final Optional<BlockHeader> maybeBlockHeader =
        blockchainService.getBlockHeaderByHash(blockHash);
    final Optional<BonsaiWorldState> maybeWorldView =
        worldStateService.getWorldView(blockHash).map(BonsaiWorldState.class::cast);

    if (maybeBlockHeader.isEmpty() || maybeWorldView.isEmpty()) {
      return null;
    }

    final ExecutionWitnessService service =
        new ExecutionWitnessService(blockchainService, rlpConverterService);

    List<String> trieNodes = service.buildTrieNodes(trieLogLayer.get(), maybeWorldView.get());
    List<String> keys = service.buildKeys(trieLogLayer.get());
    List<String> codes = service.buildCode(trieLogLayer.get(), maybeWorldView.get());
    List<String> headers = service.buildHeaders(maybeBlockHeader.get());

    return new ExecutionWitnessJsonTest(trieNodes, keys, codes, headers);
  }

  private boolean areServicesReady() {
    return pluginServiceProvider.isServiceAvailable(BlockchainService.class)
        && pluginServiceProvider.isServiceAvailable(RlpConverterService.class)
        && pluginServiceProvider.isServiceAvailable(WorldStateService.class);
  }

  private Hash parseBlockHash(
      final PluginRpcRequest rpcRequest, final BlockchainService blockchainService)
      throws InvalidParameterException {
    BlockParameterOrBlockHash blockParam;
    try {
      blockParam = new BlockParameterOrBlockHash(rpcRequest.getParams()[0].toString());
    } catch (JsonProcessingException e) {
      throw new InvalidParameterException();
    }

    if (blockParam.isLatest()) {
      return blockchainService.getChainHeadHash();
    }

    if (blockParam.isNumeric()) {
      long blockNumber = blockParam.getNumber().orElseThrow(InvalidParameterException::new);
      return blockchainService
          .getBlockByNumber(blockNumber)
          .map(BlockContext::getBlockHeader)
          .map(BlockHeader::getBlockHash)
          .orElseThrow(InvalidParameterException::new);
    }

    return blockParam.getHash().orElseThrow(InvalidParameterException::new);
  }

  private Optional<TrieLog> getTrieLogLayer(final Hash blockHash) {
    return ctx.getTrieLogService().getTrieLogProvider().getTrieLogLayer(blockHash);
  }
}
