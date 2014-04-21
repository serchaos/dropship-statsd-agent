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

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Properties;
import java.util.Random;

/**
 * Base class for logging stats to statsd.
 */
abstract class StatsdStatsLogger {

  static final class NoOpLogger extends StatsdStatsLogger {

    NoOpLogger(Properties settings) {
      super(settings);
    }

    @Override
    protected void doSend(String stat) {
      // no-op!
    }
  }

  static final class StatsdStatsLoggerImpl extends StatsdStatsLogger {

    private final InetSocketAddress address;
    private final DatagramChannel channel;

    StatsdStatsLoggerImpl(Properties settings,
                          String host,
                          int port) {
      super(settings);

      try {
        this.address = new InetSocketAddress(host, port);
        this.channel = DatagramChannel.open();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    StatsdStatsLoggerImpl(Properties settings, String host) {
      this(settings, host, 8125);
    }

    @Override
    protected void doSend(String stat) {
      try {
        byte[] rawBytesToSend = stat.getBytes(Charsets.UTF_8.name());
        ByteBuffer bytesToSend = ByteBuffer.wrap(rawBytesToSend);
        channel.send(bytesToSend, address);
      } catch (IOException e) {
        // bury
      }
    }
  }

  private static final CharMatcher DISALLOWED_CHARS = CharMatcher.is('@');
  private final Random rng;
  private final double defaultSampleRate;

  private StatsdStatsLogger(Properties properties) {
    this.rng = new Random();
    this.defaultSampleRate = Double.parseDouble(Optional.fromNullable(properties.getProperty("statsd.sample-rate")).or("1"));
  }

  /**
   * Sends a timing (in milliseconds) to statsd using the default
   * (configurable) sample rate.
   *
   * @param metric name of metric
   * @param valueInMs timing value, in milliseconds
   */
  public final void timing(String metric, long valueInMs) {
    timing(metric, valueInMs, defaultSampleRate);
  }

  /**
   * Sends a timing (in milliseconds) to statsd using the specified
   * sample rate.
   *
   * @param metric name of metric
   * @param value timing value, in milliseconds
   * @param sampleRate sample rate between 0.0 and 1.0 inclusive
   */
  public final void timing(String metric, long value, double sampleRate) {
    send(metric + ':' + value + "|ms", sampleRate);
  }

  /**
   * Increments a counter in statsd using the default (configurable)
   * sample rate.
   *
   * @param metric name of metric
   * @param amount amount of increment
   */
  public final void increment(String metric, long amount) {
    increment(metric, amount, defaultSampleRate);
  }

  /**
   * Increments a counter in statsd using the specified sample rate.
   *
   * @param metric name of metric
   * @param amount amount of increment
   * @param sampleRate sample rate between 0.0 and 1.0 inclusive
   */
  public final void increment(String metric, long amount, double sampleRate) {
    String stat = metric + ':' + amount + "|c";
    send(stat, sampleRate);
  }

  /**
   * Sets a gauge in statsd using the default (configurable) sample rate.
   *
   * @param metric name of metric
   * @param value value of gauge
   */
  public final void gauge(String metric, long value) {
    gauge(metric, value, defaultSampleRate);
  }

  /**
   * Sets a gauge in statsd using the specified sample rate.
   *
   * @param metric name of metric
   * @param value value of gauge
   * @param sampleRate sample rate between 0.0 and 1.0 inclusive
   */
  public final void gauge(String metric, long value, double sampleRate) {
    String stat = metric + ':' + value + "|g";
    send(stat, sampleRate);
  }

  private void send(String stat, double sampleRate) {
    if (sampleRate <= 0.0) {
      return;
    }

    if (sampleRate >= 1.0 || rng.nextDouble() <= sampleRate) {
      stat = escape(stat) + "|@" + sampleRate;
      doSend(stat);
    }
  }

  private String escape(String stat) {
    return DISALLOWED_CHARS.replaceFrom(stat, '-');
  }

  protected abstract void doSend(String stat);
}
