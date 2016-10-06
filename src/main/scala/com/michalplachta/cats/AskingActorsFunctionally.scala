package com.michalplachta.cats

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import cats.Monad
import cats.data.ReaderT
import cats.implicits._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.higherKinds
import scala.reflect.ClassTag

object AskingActorsFunctionally extends App {
  object ExternalStuffDoNotTouch {
    case class StringStats(length: Int, palindrome: Boolean)

    class LengthCalculator extends Actor {
      def receive = {
        case subject: String => sender ! subject.length
      }
    }

    class PalindromeChecker extends Actor {
      def receive = {
        case subject: String => sender ! (subject.reverse == subject)
      }
    }
  }

  object ClassicApproach {
    import ExternalStuffDoNotTouch._

    class StringStatsCalculator(lengthCalculator: ActorRef, palindromeChecker: ActorRef) extends Actor {
      import ExecutionContext.Implicits.global
      implicit val timeout: Timeout = 1 second

      def receive = {
        case subject: String =>
          val lengthFuture = (lengthCalculator ? subject).mapTo[Int]
          val palindromeFuture = (palindromeChecker ? subject).mapTo[Boolean]
          val resultFuture = for {
            length <- lengthFuture
            palindrome <- palindromeFuture
          } yield StringStats(length, palindrome)

          val requester = sender
          resultFuture.onSuccess { case stats: StringStats => requester ! stats }
      }
    }

    def runStringStatsApp(subject: String): StringStats = {
      implicit val system = ActorSystem("AskingActorsClassically")
      implicit val timeout: Timeout = 1 second
      val lengthCalculator = system.actorOf(Props[LengthCalculator], "lengthCalculator")
      val palindromeChecker = system.actorOf(Props[PalindromeChecker], "palindromeChecker")

      val stringProcessor = system.actorOf(Props(classOf[StringStatsCalculator], lengthCalculator, palindromeChecker), "stringProcessor")
      val futureStats: Future[StringStats] = (stringProcessor ? subject).mapTo[StringStats]
      val result = Await.result(futureStats, 5 seconds)

      system.terminate()
      result
    }
  }

  object FunctionalApproach {
    import ExternalStuffDoNotTouch._

    def stringStats[F[_]: Monad](calculateLength: ReaderT[F, String, Int],
                                 checkIfPalindrome: ReaderT[F, String, Boolean]): ReaderT[F, String, StringStats] = {
      for {
        length <- calculateLength
        palindrome <- checkIfPalindrome
      } yield StringStats(length, palindrome)
    }

    def askActor[A, B: ClassTag](actor: ActorRef): ReaderT[Future, A, B] = ReaderT { (question: A) =>
      actor.ask(question)(1 second).mapTo[B]
    }

    def runStringStatsApp(subject: String): StringStats = {
      import ExecutionContext.Implicits.global

      implicit val system = ActorSystem("AskingActorsClassically")
      val lengthCalculator = system.actorOf(Props[LengthCalculator], "lengthCalculator")
      val palindromeChecker = system.actorOf(Props[PalindromeChecker], "palindromeChecker")
      val futureStats: Future[StringStats] = stringStats(askActor(lengthCalculator), askActor(palindromeChecker)).run(subject)
      val result = Await.result(futureStats, 5 second)

      system.terminate()
      result
    }
  }

  println(s"[ClassicApproach] StringStats for 'abba': ${ClassicApproach.runStringStatsApp("abba")}")
  println(s"[FunctionalApproach] StringStats for 'abba': ${FunctionalApproach.runStringStatsApp("abba")}")
}
