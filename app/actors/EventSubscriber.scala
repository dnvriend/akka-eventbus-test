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

package actors

import akka.actor.{ Actor, ActorLogging }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.LoggingReceive
import domains.Event

class EventSubscriber extends Actor with ActorLogging {

  override def preStart(): Unit = {
    val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe("events", self)
  }

  override def receive: Receive = LoggingReceive {
    case msg: Event        => log.info("Received from dist-pub/sub: {}", msg)
    case msg: SubscribeAck => log.info("Subscribed to dist-pub/sub topic: {}", msg)
  }
}
