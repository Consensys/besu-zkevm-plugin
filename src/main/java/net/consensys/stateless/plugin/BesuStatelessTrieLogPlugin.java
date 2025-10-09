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
import net.consensys.stateless.trielog.StatelessTrieLogFactory;
import net.consensys.stateless.trielog.StatelessTrieLogService;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.TrieLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(BesuPlugin.class)
public class BesuStatelessTrieLogPlugin implements BesuPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(BesuStatelessTrieLogPlugin.class);

  @Override
  public void register(final ServiceManager serviceManager) {
    LOG.info("Registering Stateless TrieLog plugin");

    StatelessContext ctx = StatelessContextImpl.getOrCreate();
    StatelessTrieLogFactory trieLogFactory = new StatelessTrieLogFactory();
    StatelessTrieLogService trieLogService = new StatelessTrieLogService(ctx);
    ctx.setTrieLogService(trieLogService);
    ctx.setTrieLogFactory(trieLogFactory);

    serviceManager.addService(TrieLogService.class, trieLogService);
  }

  @Override
  public void start() {
    // no-op
  }

  @Override
  public void stop() {
    // no-op
  }
}
