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
package net.consensys.stateless.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.datatypes.AccountValue;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.proof.WorldStateProofProvider;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.worldview.BonsaiWorldState;
import org.hyperledger.besu.ethereum.worldstate.WorldStateStorageCoordinator;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.services.BlockchainService;
import org.hyperledger.besu.plugin.services.rlp.RlpConverterService;
import org.hyperledger.besu.plugin.services.trielogs.TrieLog;

public class ExecutionWitnessService {

  private final BlockchainService blockchainService;
  private final RlpConverterService rlpConverterService;

  public ExecutionWitnessService(
      final BlockchainService blockchainService, final RlpConverterService rlpConverterService) {
    this.blockchainService = blockchainService;
    this.rlpConverterService = rlpConverterService;
  }

  public List<String> buildTrieNodes(final TrieLog trieLog, final BonsaiWorldState worldView) {
    var proofProvider =
        new WorldStateProofProvider(
            new WorldStateStorageCoordinator(worldView.getWorldStateStorage()));

    var resultSet = ConcurrentHashMap.<String>newKeySet();
    trieLog.getAccountChanges().entrySet().parallelStream()
        .forEach(
            entry -> {
              Address address = entry.getKey();
              var prior = entry.getValue().getPrior();
              if (prior == null) {
                return;
              }
              var storageSlots = collectStorageSlots(trieLog, address);
              proofProvider
                  .getAccountProof(worldView.getWorldStateRootHash(), address, storageSlots)
                  .ifPresent(
                      proof -> {
                        proof
                            .getAccountProof()
                            .forEach(bytes -> resultSet.add(bytes.toHexString()));
                        proof
                            .getStorageKeys()
                            .forEach(
                                key ->
                                    proof
                                        .getStorageProof(key)
                                        .forEach(bytes -> resultSet.add(bytes.toHexString())));
                      });
            });

    return List.copyOf(resultSet);
  }

  public List<String> buildKeys(final TrieLog trieLog) {
    return trieLog.getAccountChanges().keySet().stream()
        .flatMap(
            address ->
                Stream.concat(
                    Stream.of(address.toHexString()),
                    collectStorageSlots(trieLog, address).stream().map(Bytes::toHexString)))
        .distinct()
        .toList();
  }

  public List<String> buildCode(final TrieLog trieLog, final BonsaiWorldState worldView) {
    var resultSet = java.util.concurrent.ConcurrentHashMap.<String>newKeySet();
    trieLog.getAccountChanges().entrySet().parallelStream()
        .map(entry -> new AccountPrior(entry.getKey(), entry.getValue().getPrior()))
        .filter(ap -> ap.prior() != null && !ap.prior().getCodeHash().equals(Hash.EMPTY))
        .forEach(
            ap ->
                worldView
                    .getCode(ap.address(), ap.prior.getCodeHash())
                    .ifPresent(bytes -> resultSet.add(bytes.toHexString())));
    return List.copyOf(resultSet);
  }

  private record AccountPrior(Address address, AccountValue prior) {}

  public List<String> buildHeaders(final BlockHeader startHeader) {
    return Stream.iterate(
            Optional.of(startHeader),
            Optional::isPresent,
            opt -> opt.flatMap(h -> blockchainService.getBlockHeaderByHash(h.getParentHash())))
        .flatMap(Optional::stream)
        .limit(2)
        .map(h -> rlpConverterService.buildRlpFromHeader(h).toHexString())
        .toList();
  }

  private List<UInt256> collectStorageSlots(final TrieLog trieLog, final Address address) {
    return trieLog.getStorageChanges(address).keySet().stream()
        .map(slotKey -> slotKey.getSlotKey().orElseThrow())
        .toList();
  }
}
