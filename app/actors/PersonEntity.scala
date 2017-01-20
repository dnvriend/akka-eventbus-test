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

import akka.actor.{ ActorLogging, PoisonPill, Props, ReceiveTimeout }
import akka.cluster.sharding.ShardRegion.{ HashCodeMessageExtractor, Passivate }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import domains.{ Command, Event, Person }

import scala.concurrent.duration.FiniteDuration

object PersonEntity {
  def props(passivationTimeout: FiniteDuration): Props =
    Props(classOf[PersonEntity], passivationTimeout)

  final val NumberOfShards: Int = 36

  final val messageExtractor = new HashCodeMessageExtractor(NumberOfShards) {
    override def entityId(message: Any): String = message match {
      case cmd: Command => cmd.id
      case _            => null
    }
  }
}

class PersonEntity(passivationTimeout: FiniteDuration) extends PersistentActor with ActorLogging {
  override def persistenceId: String = self.path.name

  override def preStart(): Unit = {
    super.preStart()
    context.setReceiveTimeout(passivationTimeout)
  }

  override def postStop(): Unit = {
    log.info("Stopped: {}", self.path.name)
    super.postStop()
  }

  var maybePerson: Option[Person] = Option.empty[Person]

  override def receiveRecover: Receive = {
    case event: Event =>
      maybePerson = Person.handleEvent(maybePerson, event)
      log.info("Recovering: {}", event)

    case RecoveryCompleted =>
      log.info("RecoveryCompleted, state={}", maybePerson)
  }

  override def receiveCommand: Receive = {
    case command: Command =>
      val (event, newMaybePerson) = Person.handleCommand(maybePerson, command)
      persist(event) { event =>
        maybePerson = newMaybePerson
        sender() ! event
      }

    case ReceiveTimeout =>
      log.info("Passifying: {}", self.path.name)
      context.parent ! Passivate(stopMessage = PoisonPill)
  }
}
