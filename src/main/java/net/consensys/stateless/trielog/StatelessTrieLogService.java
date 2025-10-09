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
package net.consensys.stateless.trielog;

import net.consensys.stateless.context.StatelessContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hyperledger.besu.plugin.services.TrieLogService;
import org.hyperledger.besu.plugin.services.trielogs.TrieLogEvent;
import org.hyperledger.besu.plugin.services.trielogs.TrieLogFactory;
import org.hyperledger.besu.plugin.services.trielogs.TrieLogProvider;

public class StatelessTrieLogService implements TrieLogService {
  private final StatelessContext ctx;

  public StatelessTrieLogService(StatelessContext ctx) {
    this.ctx = ctx;
  }

  private TrieLogProvider trieLogProvider = null;

  @Override
  public List<TrieLogEvent.TrieLogObserver> getObservers() {
    return Collections.emptyList();
  }

  @Override
  public Optional<TrieLogFactory> getTrieLogFactory() {
    return Optional.of(ctx.getTrieLogFactory());
  }

  @Override
  public void configureTrieLogProvider(final TrieLogProvider trieLogProvider) {
    this.trieLogProvider = trieLogProvider;
  }

  @Override
  public TrieLogProvider getTrieLogProvider() {
    return trieLogProvider;
  }
}
