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

package domains

sealed trait Command {
  def id: String
}
final case class CreatePerson(id: String, name: String, age: Int) extends Command
final case class ChangeName(id: String, name: String) extends Command
final case class ChangeAge(id: String, age: Int) extends Command

sealed trait Event
final case class PersonCreated(id: String, name: String, age: Int) extends Event
final case class NameChanged(id: String, oldName: String, newName: String) extends Event
final case class AgeChanged(id: String, oldAge: Int, newAge: Int) extends Event

case class Person(id: String, name: String, age: Int)

object Person {
  def handleEvent(maybePerson: Option[Person], event: Event): Option[Person] = event match {
    case PersonCreated(id, name, age) => Option(Person(id, name, age))
    case NameChanged(_, _, newName)   => maybePerson.map(_.copy(name = newName))
    case AgeChanged(_, _, newAge)     => maybePerson.map(_.copy(age = newAge))
  }

  def handleCommand(maybePerson: Option[Person], command: Command): (Event, Option[Person]) = command match {
    case CreatePerson(id, name, age) =>
      (PersonCreated(id, name, age), Option(Person(id, name, age)))
    case ChangeName(id, newName) =>
      val oldName: String = maybePerson.map(_.name).getOrElse("")
      (NameChanged(id, oldName, newName), maybePerson.map(_.copy(name = newName)))
    case ChangeAge(id, newAge) =>
      val oldAge: Int = maybePerson.map(_.age).getOrElse(0)
      (AgeChanged(id, oldAge, newAge), maybePerson.map(_.copy(age = newAge)))
  }
}
