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

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.event.LoggingReceive
import domains.Event

class EventPublisher(mediator: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = LoggingReceive {
    case event: Event =>
      log.info("Publishing event: {}", event)
      mediator ! Publish("events", event)
  }
}
