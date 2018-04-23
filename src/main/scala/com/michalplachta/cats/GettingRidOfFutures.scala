package com.michalplachta.cats

import cats.Functor
import cats.implicits._

import scala.Numeric.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.higherKinds

object GettingRidOfFutures extends App {
  import ExecutionContext.Implicits.global

  def addIntsInFuture(local: Int, fetchFromRemote: Future[Int]): Future[Int] = {
    for {
      fetched ← fetchFromRemote
    } yield local + fetched
  }

  def addIntsInContext[F[_]: Functor](local: Int,
                                      fetchFromContext: F[Int]): F[Int] = {
    for {
      fetched ← fetchFromContext
    } yield local + fetched
  }

  def addInContext[F[_]: Functor, A: Numeric](local: A,
                                              fetchFromContext: F[A]): F[A] = {
    for {
      fetched ← fetchFromContext
    } yield local + fetched
  }

  private def get[A](f: Future[A]): A = Await.result(f, 5.seconds)

  println(s"addIntsInFuture: ${get(addIntsInFuture(2, Future.successful(3)))}")
  println(
    s"addIntsInContext: ${get(addIntsInContext(2, Future.successful(3)))}")
  println(s"addInContext: ${get(addInContext(2.0, Future.successful(3.0)))}")
}
