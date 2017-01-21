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

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.LoggingReceive
import domains.{ Event, PersonCreated }

object AllPersonsService {
  // getAllPersons
  case object GetAllPersonsRequest
  case class GetAllPersonsResponse(xs: List[Person])
  // getPersonById
  case class GetPersonByIdRequest(id: String)
  case class GetPersonByIdResponse(maybePerson: Option[Person])
  // service owned
  case class Person(id: String, name: String, age: String)

  def props(mediator: ActorRef): Props = Props(classOf[AllPersonsService], mediator)
}

class AllPersonsService(mediator: ActorRef) extends Actor with ActorLogging {
  import AllPersonsService._
  override def preStart(): Unit = {
    // please note that the eventStream is only for *this* actor system
    // so events won't be propagated in clustered mode!
    //    context.system.eventStream.subscribe(self, classOf[Event])
    mediator ! Subscribe("events", self)
  }

  override def receive: Receive = people(List.empty[Person])

  def people(xs: List[Person]): Receive = LoggingReceive {
    case PersonCreated(id, name, age) =>
      val person = Person(id.toString, name, age.toString)
      log.info("===> PersonQueryActor: Adding person: {}", person)
      context.become(people(xs :+ person))

    case GetAllPersonsRequest =>
      log.info("===> PersonQueryActor: Returning {}", xs)
      sender() ! GetAllPersonsResponse(xs)

    case GetPersonByIdRequest(id) =>
      sender() ! GetPersonByIdResponse(xs.find(_.id == id))

    case msg: SubscribeAck => log.info("Subscribed to dist-pub/sub topic: {}", msg)
  }
}
