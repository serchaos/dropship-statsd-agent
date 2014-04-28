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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dagger module that provides a list of snitch services to run.
 */
@Module(injects = Snitch.class)
class SnitchModule {

  private final String groupArtifactId;
  private final Properties properties;
  private final Class<?> mainClass;

  SnitchModule(Properties properties, String groupArtifactId, Class mainClass) {
    this.properties = checkNotNull(properties, "properties");
    this.groupArtifactId = checkNotNull(groupArtifactId, "group artifact id");
    this.mainClass = checkNotNull(mainClass, "main class");
  }

  @Provides Properties provideProperties() {
    return this.properties;
  }

  @Provides @Named("gav") String provideGav() {
    return this.groupArtifactId;
  }

  @Provides @Named("main") Class provideMainClass() {
    return this.mainClass;
  }

  @Provides StatsdStatsLogger provideStatsLogger() {
    Optional<String> host = statsdHost();
    if (host.isPresent()) {
      return new StatsdStatsLogger.StatsdStatsLoggerImpl(properties, host.get(), statsdPort());
    } else {
      return new StatsdStatsLogger.NoOpLogger(properties);
    }
  }

  @Provides
  List<SnitchService> provideSnitchServices(SnitchService.GarbageCollectionSnitch gc,
                                            SnitchService.ClassLoadingSnitch cl,
                                            SnitchService.DiskSpaceSnitch disk,
                                            SnitchService.MemorySnitch mem,
                                            SnitchService.ThreadSnitch thread,
                                            SnitchService.UptimeSnitch uptime,
                                            SnitchService.NoOp noop) {

    if (!statsdHost().isPresent()) {
      return ImmutableList.<SnitchService>of(noop);
    }

    return ImmutableList.of(gc, cl, disk, mem, thread, uptime);
  }

  private Optional<String> statsdHost() {
    return Optional.fromNullable(properties.getProperty("statsd.host"));
  }

  private int statsdPort() {
    return Integer.parseInt(Optional.fromNullable(properties.getProperty("statsd.port")).or("8125"));
  }
}
