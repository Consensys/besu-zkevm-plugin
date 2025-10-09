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
package net.consensys.stateless.context;

import net.consensys.stateless.trielog.StatelessTrieLogFactory;
import net.consensys.stateless.trielog.StatelessTrieLogService;

import com.google.common.annotations.VisibleForTesting;
import org.hyperledger.besu.plugin.services.TrieLogService;

/** Context class which allows for lazy instantiation of these services via plugin registry. */
public interface StatelessContext {

  TrieLogService getTrieLogService();

  StatelessContext setTrieLogService(StatelessTrieLogService service);

  StatelessTrieLogFactory getTrieLogFactory();

  StatelessContext setTrieLogFactory(StatelessTrieLogFactory factory);

  class StatelessContextImpl implements StatelessContext {
    private StatelessTrieLogFactory trieLogFactory = null;
    private StatelessTrieLogService trieLogService = null;

    @VisibleForTesting
    StatelessContextImpl() {}

    public static StatelessContext getOrCreate() {
      return Holder.INSTANCE;
    }

    private static class Holder {
      private static final StatelessContext INSTANCE = new StatelessContextImpl();
    }

    @Override
    public TrieLogService getTrieLogService() {
      return trieLogService;
    }

    @Override
    public StatelessContext setTrieLogFactory(StatelessTrieLogFactory factory) {
      this.trieLogFactory = factory;
      return this;
    }

    @Override
    public StatelessContext setTrieLogService(final StatelessTrieLogService trieLogService) {
      this.trieLogService = trieLogService;
      return this;
    }

    @Override
    public StatelessTrieLogFactory getTrieLogFactory() {
      return trieLogFactory;
    }
  }
}
