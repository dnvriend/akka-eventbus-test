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

package models

import actors.AllPersonsService.{ Person => AllPerson }
import play.api.libs.json._

import scala.language.implicitConversions

object Person {
  implicit val format: Format[Person] = Json.format[Person]
  implicit def allPersonsToPerson(p: AllPerson): Person =
    Person(p.id, p.name, p.age.toInt)

  implicit def listOfAllPersonsToPerson(xs: List[AllPerson]): List[Person] =
    xs.map(allPersonsToPerson)

  implicit def optAllPersonToPerson(opt: Option[AllPerson]): Option[Person] =
    opt.map(allPersonsToPerson)
}

case class Person(id: String, name: String, age: Int)
