package util

import doobie._
import doobie.implicits._
import cats.effect.IO
import scala.concurrent.ExecutionContext

import cats.effect.unsafe.implicits.global

class Database(
              driver: String,
              url: String,
              user: String,
              password: String
              ) {

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:scala_api", "postgres", "password"
  )
}
