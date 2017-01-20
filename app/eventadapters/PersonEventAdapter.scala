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

package eventadapters

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{ EventAdapter, EventSeq }
import domains.Event

class PersonEventAdapter(system: ExtendedActorSystem) extends EventAdapter {
  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = {
    event match {
      case event: Event =>
        // please note that the eventStream is only for *this* actor system
        // so events won't be propagated in clustered mode!
        system.eventStream.publish(event)
        event
    }
  }

  override def fromJournal(event: Any, manifest: String): EventSeq = EventSeq(event)
}
