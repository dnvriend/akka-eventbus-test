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

import actors.PersonCounter.{ GetPersonCountRequest, GetPersonCountResponse }
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.LoggingReceive
import domains.PersonCreated

object PersonCounter {
  case object GetPersonCountRequest
  case class GetPersonCountResponse(count: Long)

  def props(mediator: ActorRef): Props = Props(classOf[PersonCounter], mediator)
}

class PersonCounter(mediator: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    // please note that the eventStream is only for *this* actor system
    // so events won't be propagated in clustered mode!
    //    context.system.eventStream.subscribe(self, classOf[Event])
    mediator ! Subscribe("events", self)
  }
  override def receive: Receive = personCount(0)

  def personCount(numOfPersons: Long): Receive = LoggingReceive {
    case e: PersonCreated      => context.become(personCount(numOfPersons + 1))
    case GetPersonCountRequest => sender() ! GetPersonCountResponse(numOfPersons)
    case msg: SubscribeAck     => log.info("Subscribed to dist-pub/sub topic: {}", msg)
  }
}
