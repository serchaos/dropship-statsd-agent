/*
 * Copyright (C) 2014 zulily, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dropship.agent.statsd;

import dagger.ObjectGraph;
import dropship.agent.BaseAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * The StatsdAgent runs multiple services which watch system resources
 * and report metrics to an optional statsd server.
 */
public class StatsdAgent extends BaseAgent {

  /**
   * Invoked by the Java VM when this .jar file is given to the {@code -javaagent} argument.
   *
   * This method sets up an instance of {@code StatsdAgent} and uses
   * {@link BaseAgent#premain(String, java.lang.instrument.Instrumentation, dropship.agent.BaseAgent)} to
   * wire it up to Dropship.
   */
  public static void premain(String agentArgument, Instrumentation instrumentation) {
    BaseAgent.premain(agentArgument, instrumentation, new StatsdAgent());
  }

  private Snitch snitch = null;

  private StatsdAgent() {}

  @Override
  public void onStart(Properties properties, String gav, Class<?> mainClass, Method mainMethod, Object[] mainArgs) {
    this.snitch = ObjectGraph.create(new SnitchModule(properties, gav, mainClass)).get(Snitch.class);
    this.snitch.start();
  }

  @Override
  public void onError(Throwable throwable) {}

  @Override
  public void onExit() {
    this.snitch.stop();
  }
}
