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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.storage.BonsaiWorldStateKeyValueStorage;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.storage.BonsaiWorldStateLayerStorage;

public class BonsaiWorldStateWitnessStorage extends BonsaiWorldStateLayerStorage {

  private final Set<Bytes> trieNodes;

  public BonsaiWorldStateWitnessStorage(final BonsaiWorldStateKeyValueStorage parent) {
    super(parent);
    this.trieNodes = ConcurrentHashMap.newKeySet();
  }

  @Override
  public Optional<Bytes> getAccountStateTrieNode(final Bytes location, final Bytes32 nodeHash) {
    final Optional<Bytes> accountStateTrieNode = super.getAccountStateTrieNode(location, nodeHash);
    accountStateTrieNode.ifPresent(trieNodes::add);
    return accountStateTrieNode;
  }

  @Override
  public Optional<Bytes> getAccountStorageTrieNode(
      Hash accountHash, Bytes location, Bytes32 nodeHash) {
    final Optional<Bytes> accountStorageTrieNode =
        super.getAccountStorageTrieNode(accountHash, location, nodeHash);
    accountStorageTrieNode.ifPresent(trieNodes::add);
    return accountStorageTrieNode;
  }

  public Set<Bytes> getTrieNodes() {
    return trieNodes;
  }
}
