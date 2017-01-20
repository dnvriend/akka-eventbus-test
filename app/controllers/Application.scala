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

package controllers

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorSystem
import models.Person
import org.slf4j.{ Logger, LoggerFactory }
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{ Json, Writes }
import play.api.mvc.{ Action, Controller, Result }
import repositories.PersonRepository

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

object User {
  implicit val format = Json.format[User]
}
case class User(name: String, age: Int)

class Application @Inject() (system: ActorSystem, personRepository: PersonRepository)(implicit ec: ExecutionContext) extends Controller {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  def randomId: String = UUID.randomUUID.toString

  val userForm: Form[User] = Form(
    mapping(
      "name" -> text,
      "age" -> number
    )(User.apply)(User.unapply)
  )

  def index = Action.async {
    for {
      count <- personRepository.getPersonCount
      xs <- personRepository.getPersons
    } yield Ok(views.html.index(xs, count))
  }

  def postUser = Action.async { implicit request =>
    for {
      user <- Future.fromTry(Try(userForm.bindFromRequest.get))
      _ <- personRepository.createPerson(randomId, user.name, user.age)
    } yield Redirect(routes.Application.index())
  }

  def getUsers = Action.async {
    personRepository.getPersons.map { xs =>
      Ok(Json.toJson(xs))
    }
  }

  def getUserCount = Action.async {
    personRepository.getPersonCount.map(count => Ok(Json.toJson(Json.obj("count" -> count))))
  }

  def addUser = Action.async { request =>
    request.body.asJson.map(_.as[User]).map { user =>
      val id = randomId
      personRepository.createPerson(id, user.name, user.age).map { personCreated =>
        Ok(Json.toJson(Person(personCreated.id, personCreated.name, personCreated.age)))
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def getUserById(id: String) = Action.async {
    personRepository.getPersonById(id).map(x => maybeHandler(x))
  }

  def maybeHandler[A: Writes](maybe: Option[A]): Result =
    maybe.map(a => Ok(Json.toJson(a))).getOrElse(NotFound)
}

