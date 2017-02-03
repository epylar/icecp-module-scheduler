ICECP Module Scheduler
=====================

#### Brief Description
This ICECP module allows for the scheduling of time based events. These
events are called **triggers**. Triggers are set through the module configuration.

Currently, only **daily** triggers are allowed. You can set an **exact time** trigger
that will fire at the specified time. Also, you can set a **range** trigger, which
will select a random time between a specified start and end time, and then fire
on a daily basis.

When a trigger is fired, a **trigger message** will be published on a channel
specified in the trigger configuration. Other ICECP modules can subscribe to the
channel, receive the trigger messages, and take module specific actions based on
the message contents.

#### Attributes

Please read the `Note` section too for few design specific points.

- `scheduler-triggers`: list of valid `RangeTriggers` and `IntervalTriggers` as a String
This attribute is defined in `configuration/config.json` and may look like this:

`{
  "scheduler-triggers": {
    "range-triggers":
    [{
      "id": "dex-trigger",
      "startTime": "11:00 PM",
      "endTime": "5:00 AM",
      "publishChannel": "ndn:/intel/scheduler/dex/triggers"
    }],
    "interval-triggers":
    [{
      "id": "ack-trigger",
      "interval": "10",
      "unit": "MINUTES"
      "publishChannel": "ndn:/intel/scheduler/ack/triggers"
    }]
  }
}`


Note:

- `scheduler-triggers` is a READ-ONLY attribute which means it is set only once during module load as part of
`config.json`. If any one of the trigger messages contains junk keys like `"foo": "bar"` and junk values for `startTime`
and `endTime`, then none of the other triggers get added and the remote user/sysadmin will need to fix the invalid trigger
and restart `icecp-module-scheduler`.
- Until Bug-1536 is fixed, the `config.json` file will represent the value of the attribute as a String like:

`{
   "scheduler-triggers": "{\"rangeTriggers\":[{\"id\": \"dex-trigger\",\"startTime\": \"01:26 PM\",\"publishChannel\":\"ndn:/intel/scheduler/dex/triggers\"}],\"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 5, \"unit\": \"SECONDS\", \"publishChannel\":\"ndn:/intel/scheduler/ack/triggers\"}]}"
 }`

- For `icecp-module-dex`, `publishChannel` field currently the value SHOULD always be `ndn:/intel/scheduler/dex/triggers`
- For `icecp-module-ack`, `publishChannel` field currently the value SHOULD always be `ndn:/intel/scheduler/ack/triggers`



### Install

Clone this repository and run: mvn install.

### Running from the IDE
* Configure the IDE to run the module. Use the following settings (proxy settings may need to change based on location):
   * **Main class** - `com.intel.icecp.main.MainDaemon`
   * **VM options** - `-Dicecp.sandbox=disabled -Dsocks.proxyHost=proxy-jf.intel.com -Dsocks.proxyPort=1080`
   * **Program arguments** - `./target/icecp-module-scheduler-0.1.jar -Dlog4j.configurationFile = ./configuration/log4j2.xml`
* Start the module in the IDE

### Documentation

 - [Wiki](https://github.intel.com/iSPA/icecp-module-scheduler/wiki)
 - [Javadoc](https://github.intel.com/pages/iSPA/icecp-module-scheduler)


### License

Copyright &copy; 2016, Intel Corporation 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
