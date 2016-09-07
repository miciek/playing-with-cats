package com.michalplachta.cats.futures

import cats.Monad
import cats.implicits._

import language.higherKinds
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object Example extends App {
  import ExecutionContext.Implicits.global

  def add0(local: Int, remote: () => Future[Int]): Future[Int] = {
    for {
      fetched <- remote()
    } yield local + fetched
  }

  def add1[M[_] : Monad](local: Int, fetchFromContext: () => M[Int]): M[Int] = {
    for {
      fetched <- fetchFromContext()
    } yield local + fetched
  }

  def print[A](f: Future[A]): Unit = println(Await.result(f, 5.seconds))

  print(add0(2, () => Future.successful(3)))
  print(add1[Future](2, () => Future.successful(3)))
}
