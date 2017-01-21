/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.inject.Named

import actors.{ AllPersonsService, EventSubscriber, PersonCounter, PersonEntity }
import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings }
import akka.util.Timeout
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Inject, Provider }
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.duration._

object Module {
  final val DefaultDuration: FiniteDuration = 10.seconds
}

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[EventSubscriber]("pub-sub-event-subscriber")

    bind(classOf[ActorRef])
      .annotatedWith(Names.named("pub-sub-mediator"))
      .toProvider(classOf[DistPubSubProvider])
      .asEagerSingleton()

    bind(classOf[ActorRef])
      .annotatedWith(Names.named("all-persons-service"))
      .toProvider(classOf[AllPersonsSingletonManager])
      .asEagerSingleton()

    bind(classOf[ActorRef])
      .annotatedWith(Names.named("person-counter-service"))
      .toProvider(classOf[PersonCounterSingletonManager])
      .asEagerSingleton()

    bind(classOf[ActorRef])
      .annotatedWith(Names.named("person-region"))
      .toProvider(classOf[PersonShardRegionProvider])
      .asEagerSingleton()

    bind(classOf[Timeout]).toInstance(Module.DefaultDuration)
  }
}

class DistPubSubProvider @Inject() (system: ActorSystem) extends Provider[ActorRef] {
  override def get(): ActorRef = {
    DistributedPubSub(system).mediator
  }
}

class PersonShardRegionProvider @Inject() (system: ActorSystem) extends Provider[ActorRef] {
  override def get(): ActorRef = {
    ClusterSharding(system).start(
      typeName = "Persons",
      entityProps = PersonEntity.props(Module.DefaultDuration),
      settings = ClusterShardingSettings(system),
      messageExtractor = PersonEntity.messageExtractor
    )
  }
}

class PersonCounterSingletonManager @Inject() (system: ActorSystem, @Named("pub-sub-mediator") mediator: ActorRef) extends AbstractSingletonManager(system, "person-counter", PersonCounter.props(mediator))

class AllPersonsSingletonManager @Inject() (system: ActorSystem, @Named("pub-sub-mediator") mediator: ActorRef) extends AbstractSingletonManager(system, "all-person", AllPersonsService.props(mediator))

abstract class AbstractSingletonManager(system: ActorSystem, name: String, singletonProps: Props) extends Provider[ActorRef] {
  final val SingletonManagerName = s"$name-singleton-manager"
  final val SingletonProxyName = s"$name-singleton-proxy"
  val managerSettings = ClusterSingletonManagerSettings(system)
  val proxySettings = ClusterSingletonProxySettings(system)
  override def get(): ActorRef = {
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = singletonProps,
        terminationMessage = PoisonPill,
        settings = managerSettings
      ),
      name = SingletonManagerName
    )
    system.actorOf(ClusterSingletonProxy.props(s"/user/$SingletonManagerName", proxySettings), SingletonProxyName)
  }
}