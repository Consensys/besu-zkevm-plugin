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
package net.consensys.stateless.rpc.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecutionWitnessJsonTest {

  @JsonProperty("state")
  private List<String> state;

  @JsonProperty("keys")
  private List<String> keys;

  @JsonProperty("codes")
  private List<String> codes;

  @JsonProperty("headers")
  private List<String> headers;

  public ExecutionWitnessJsonTest(
      final List<String> state,
      final List<String> keys,
      final List<String> codes,
      final List<String> headers) {
    this.state = state;
    this.keys = keys;
    this.codes = codes;
    this.headers = headers;
  }

  public List<String> getState() {
    return state;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public List<String> getKeys() {
    return keys;
  }

  public void setKeys(final List<String> keys) {
    this.keys = keys;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(final List<String> codes) {
    this.codes = codes;
  }

  public void setState(final List<String> state) {
    this.state = state;
  }

  public void setHeaders(final List<String> headers) {
    this.headers = headers;
  }
}
