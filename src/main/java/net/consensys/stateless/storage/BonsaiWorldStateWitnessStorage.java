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
package net.consensys.stateless.storage;

import static org.apache.tuweni.rlp.RLP.decodeValue;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.StorageSlotKey;
import org.hyperledger.besu.ethereum.trie.NodeLoader;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.storage.BonsaiWorldStateKeyValueStorage;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.storage.BonsaiWorldStateLayerStorage;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.storage.flat.BonsaiFlatDbStrategy;
import org.hyperledger.besu.ethereum.trie.pathbased.common.storage.flat.AccountHashCodeStorageStrategy;
import org.hyperledger.besu.ethereum.trie.pathbased.common.storage.flat.CodeHashCodeStorageStrategy;
import org.hyperledger.besu.ethereum.trie.patricia.StoredMerklePatriciaTrie;
import org.hyperledger.besu.ethereum.trie.patricia.StoredNodeFactory;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.storage.SegmentedKeyValueStorage;

public class BonsaiWorldStateWitnessStorage extends BonsaiWorldStateLayerStorage {

  private final Set<Bytes> trieNodes;
  private final BonsaiFlatDbStrategy witnessFlatDbStrategy;

  public BonsaiWorldStateWitnessStorage(
      final MetricsSystem metricsSystem, final BonsaiWorldStateKeyValueStorage parent) {
    super(parent);
    this.witnessFlatDbStrategy = buildWitnessFlatDbStrategy(metricsSystem, parent);
    this.trieNodes = ConcurrentHashMap.newKeySet();
  }

  private BonsaiFlatDbStrategy buildWitnessFlatDbStrategy(
      final MetricsSystem metricsSystem, final BonsaiWorldStateKeyValueStorage parent) {

    final boolean isCodeByCodeHash = parent.getFlatDbStrategy().isCodeByCodeHash();

    return new BonsaiFlatDbStrategy(
        metricsSystem,
        isCodeByCodeHash
            ? new CodeHashCodeStorageStrategy()
            : new AccountHashCodeStorageStrategy()) {

      @Override
      public Optional<Bytes> getFlatAccount(
          final Supplier<Optional<Bytes>> worldStateRootHashSupplier,
          final NodeLoader nodeLoader,
          final Hash accountHash,
          final SegmentedKeyValueStorage storage) {

        return worldStateRootHashSupplier
            .get()
            .flatMap(
                rootHash ->
                    new StoredMerklePatriciaTrie<>(
                            new StoredNodeFactory<>(
                                nodeLoader, Function.identity(), Function.identity()),
                            Bytes32.wrap(rootHash))
                        .get(accountHash.getBytes()));
      }

      @Override
      public Optional<Bytes> getFlatStorageValueByStorageSlotKey(
          final Supplier<Optional<Bytes>> worldStateRootHashSupplier,
          final Supplier<Optional<Hash>> storageRootSupplier,
          final NodeLoader nodeLoader,
          final Hash accountHash,
          final StorageSlotKey storageSlotKey,
          final SegmentedKeyValueStorage storage) {

        final Optional<Hash> storageRoot = storageRootSupplier.get();
        final Optional<Bytes> worldStateRootHash = worldStateRootHashSupplier.get();

        if (storageRoot.isEmpty() || worldStateRootHash.isEmpty()) {
          return Optional.empty();
        }

        return new StoredMerklePatriciaTrie<>(
                new StoredNodeFactory<>(nodeLoader, Function.identity(), Function.identity()),
                Bytes32.wrap(storageRoot.get().getBytes()))
            .get(storageSlotKey.getSlotHash().getBytes())
            .map(bytes -> Bytes32.leftPad(decodeValue(bytes)));
      }
    };
  }

  @Override
  public Optional<Bytes> getAccountStateTrieNode(final Bytes location, final Bytes32 nodeHash) {
    final Optional<Bytes> accountStateTrieNode = super.getAccountStateTrieNode(location, nodeHash);
    accountStateTrieNode.ifPresent(trieNodes::add);
    return accountStateTrieNode;
  }

  @Override
  public Optional<Bytes> getAccountStorageTrieNode(
      final Hash accountHash, final Bytes location, final Bytes32 nodeHash) {
    final Optional<Bytes> accountStorageTrieNode =
        super.getAccountStorageTrieNode(accountHash, location, nodeHash);
    accountStorageTrieNode.ifPresent(trieNodes::add);
    return accountStorageTrieNode;
  }

  @Override
  public BonsaiFlatDbStrategy getFlatDbStrategy() {
    return witnessFlatDbStrategy;
  }

  public Set<Bytes> getTrieNodes() {
    return trieNodes;
  }
}
