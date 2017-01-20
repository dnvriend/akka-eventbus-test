# akka-eventbus-test
small study project on play in combination with conductr conductr

## Installing Conductr Sandbox

- Install Docker for Mac
- Get a free Lightbend developer account
- Read the [getting started page](https://www.lightbend.com/product/conductr/developer)
- Read the [conductr documentation](https://conductr.lightbend.com/)

## Quick install guide
Do the following:

```bash
sudo pip3 install conductr-cli

sandbox run 1.1.13 --feature visualization --nr-of-containers 3

open http://localhost:9909/
```

## SBT and conductr
The project already contains the necessary [sbt-conductr plugin](https://github.com/typesafehub/sbt-conductr), please
read up on the plugin and the available sbt commands.

## Conductr configuration
Conductr is configured as follows:

```scala
// conductr settings
BundleKeys.endpoints := Map(
  "play" -> Endpoint(bindProtocol = "http", bindPort = 0, services = Set(URI("http://:9000/eventbus"))),
  "akka-remote" -> Endpoint("tcp")
)

// the human readable name for your bundle
normalizedName in Bundle := "akka-eventbus-test"

// represents the clustered ActorSystem
BundleKeys.system := "Play"

BundleKeys.startCommand += "-Dhttp.address=$PLAY_BIND_IP -Dhttp.port=$PLAY_BIND_PORT -Dplay.akka.actor-system=$BUNDLE_SYSTEM"
```

ConductR will interpret the first path component, which is (eventbus), as the service name for the purposes of service lookup.
By default ConductR will then remove the first path component when rewriting the request. This means that your application or
service will receive everything under the root of `/`. This means that the service lookup will make the endpoints of the
'akka-eventbus-test' service available at:

```
http :9000/eventbus/
http :9000/eventbus/users
http :9000/eventbus/users/count
http POST :9000/eventbus/users name=foo age:=42
```

## Building and deploying the bundle
The conductr bundle can be created with the following commands:

- bundle:dist
- conduct load <tab>
- conduct run akka-eventbus-test --scale 1

## Consolidated logs
Conductr will consolidate logs of all the services it manages across nodes. These logs can be viewed with the following command:

- conduct logs akka-eventbus-test

## Uninstalling the bundle
The bundle can be uninstalled with the following commands:

- conduct stop akka-eventbus-test
- conduct unload akka-eventbus-test

## Stopping the sandbox
The sandbox can be stopped with the following commands:

```bash
sandbox stop
```