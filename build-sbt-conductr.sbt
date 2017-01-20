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
