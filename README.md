dropship-statsd-agent
=====================

Dropship agent that publishes JMX metrics to [statsd](https://github.com/etsy/statsd/).

### How-To

In `dropship.properties`:

    statsd.host = statsdhost.foobar.com
    statsd.port = 8125

    # optional (defaults to 1.0, or 100%)
    statsd.sample-rate = 0.5

If a valid statsd hostname/port are found, the Dropship statsd agent will output stats to statsd in the form:
`dropship.hostname.group.artifact.mainclass.statname`

Dropship will output:

* heap memory used
* CPU ms
* uptime ms
* disk free
* thread counts
* number of classes loaded
* GC stats
* **and more**

![dropship stats](https://github.com/zulily/dropship-statsd-agent/raw/master/stats.png)
![more dropship stats](https://github.com/zulily/dropship-statsd-agent/raw/master/more_stats.png)

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

