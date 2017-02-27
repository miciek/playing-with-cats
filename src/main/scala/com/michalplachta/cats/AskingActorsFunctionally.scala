package com.michalplachta.cats

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import cats.Monad
import cats.data.ReaderT
import cats.implicits._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.higherKinds
import scala.reflect.ClassTag

object AskingActorsFunctionally extends App {
  object ExternalStuffDoNotTouch {
    case class StringStats(length: Int, palindrome: Boolean)

    class LengthCalculator extends Actor {
      def receive = {
        case subject: String ⇒ sender ! subject.length
      }
    }

    class PalindromeChecker extends Actor {
      def receive = {
        case subject: String ⇒ sender ! (subject.reverse == subject)
      }
    }
  }

  object ClassicApproach {
    import ExternalStuffDoNotTouch._

    class StringStatsCalculator(lengthCalculator: ActorRef, palindromeChecker: ActorRef) extends Actor {
      import ExecutionContext.Implicits.global
      implicit val timeout: Timeout = 1 second

      def receive = {
        case subject: String ⇒
          val futureLength = (lengthCalculator ? subject).mapTo[Int]
          val futurePalindromeCheck = (palindromeChecker ? subject).mapTo[Boolean]
          val futureStats = for {
            length ← futureLength
            palindrome ← futurePalindromeCheck
          } yield StringStats(length, palindrome)

          val requester = sender
          futureStats.foreach(requester ! _)
      }
    }

    def runStringStatsApp(subject: String): StringStats = {
      implicit val system = ActorSystem("AskingActorsClassically")
      implicit val timeout: Timeout = 1 second
      val lengthCalculator = system.actorOf(Props[LengthCalculator], "lengthCalculator")
      val palindromeChecker = system.actorOf(Props[PalindromeChecker], "palindromeChecker")

      val statsCalculator = system.actorOf(Props(classOf[StringStatsCalculator], lengthCalculator, palindromeChecker), "statsCalculator")
      val futureStats = (statsCalculator ? subject).mapTo[StringStats]
      val stats = Await.result(futureStats, 5 seconds)

      system.terminate()
      stats
    }
  }

  object FunctionalApproach {
    import ExternalStuffDoNotTouch._

    def stringStats[F[_]: Monad](
      calculateLength:   ReaderT[F, String, Int],
      checkIfPalindrome: ReaderT[F, String, Boolean]
    ): ReaderT[F, String, StringStats] = {
      for {
        length ← calculateLength
        isPalindrome ← checkIfPalindrome
      } yield StringStats(length, isPalindrome)
    }

    def askActor[Q, A: ClassTag](actor: ActorRef): ReaderT[Future, Q, A] = ReaderT { (question: Q) ⇒
      actor.ask(question)(1 second).mapTo[A]
    }

    def runStringStatsApp(subject: String): StringStats = {
      import ExecutionContext.Implicits.global

      implicit val system = ActorSystem("AskingActorsFunctionally")
      val lengthCalculator = system.actorOf(Props[LengthCalculator], "lengthCalculator")
      val palindromeChecker = system.actorOf(Props[PalindromeChecker], "palindromeChecker")

      val calculateStats = stringStats(askActor(lengthCalculator), askActor(palindromeChecker))
      val futureStats = calculateStats.run(subject)
      val result = Await.result(futureStats, 5 second)

      system.terminate()
      result
    }
  }

  println(s"[ClassicApproach] StringStats for 'abba': ${ClassicApproach.runStringStatsApp("abba")}")
  println(s"[FunctionalApproach] StringStats for 'abba': ${FunctionalApproach.runStringStatsApp("abba")}")
}
