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

package repositories

import javax.inject.{ Inject, Named, Singleton }

import actors.AllPersonsService.{ GetAllPersonsResponse, GetPersonByIdRequest, GetPersonByIdResponse }
import actors.PersonCounter.{ GetPersonCountRequest, GetPersonCountResponse }
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import domains.{ CreatePerson, PersonCreated }
import models.Person

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

@Singleton
class PersonRepository @Inject() (
    @Named("all-persons-service") allPersons: ActorRef,
    @Named("person-counter-service") personCounter: ActorRef,
    @Named("person-region") personRegion: ActorRef
)(implicit ec: ExecutionContext, timeout: Timeout) {
  def getPersons: Future[List[Person]] =
    (allPersons ? actors.AllPersonsService.GetAllPersonsRequest)
      .mapTo[GetAllPersonsResponse]
      .map(_.xs)

  def getPersonById(id: String): Future[Option[Person]] =
    (allPersons ? GetPersonByIdRequest(id))
      .mapTo[GetPersonByIdResponse]
      .map(_.maybePerson)

  def createPerson(id: String, name: String, age: Int): Future[PersonCreated] =
    (personRegion ? CreatePerson(id, name, age)).mapTo[PersonCreated]

  def getPersonCount: Future[Long] =
    (personCounter ? GetPersonCountRequest)
      .mapTo[GetPersonCountResponse]
      .map(_.count)
}
