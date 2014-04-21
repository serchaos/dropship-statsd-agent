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

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Snitch runs multiple services which watch system resources
 * and report metrics to an optional statsd server.
 */
@Singleton
class Snitch {

  private final ServiceManager snitches;

  @Inject
  Snitch(List<SnitchService> snitches) {
    this.snitches = new ServiceManager(snitches);

    this.snitches.addListener(new ServiceManager.Listener() {
      @Override
      public void healthy() {
      }

      @Override
      public void stopped() {
      }

      @Override
      public void failure(Service service) {
        System.err.format("StatsdAgent service failed: %s, cause: %s", service, Throwables.getStackTraceAsString(service.failureCause()));
      }
    }, MoreExecutors.sameThreadExecutor());
  }

  /**
   * Starts all registered snitch services asynchronously.
   */
  void start() {
    snitches.startAsync();
  }

  void stop() {
    try {
      snitches.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // bury
    }
  }
}
