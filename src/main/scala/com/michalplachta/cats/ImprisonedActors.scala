package com.michalplachta.cats

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import cats.{ ~>, Id }
import cats.free.Free

object ImprisonedActors extends App {
  final case class Prisoner(name: String)

  final case class TellIfGuilty(interrogationId: Int, prisoner: Prisoner)
  final case class Guilty(interrogationId: Int)
  final case class IWontTell(interrogationId: Int)

  final case class Interrogate(prisoner: Prisoner, actorRef: ActorRef, sendVerdictTo: ActorRef)
  case object WillInterrogate
  case object WontInterrogate

  final case class Sentence(years: Int)
  final case class Sentences(prisonerAyears: Int, prisonerByears: Int)

  class Prosecutor extends Actor {
    var maybePrisonerAndActorA: Option[(Prisoner, ActorRef)] = None
    var maybePrisonerAndActorB: Option[(Prisoner, ActorRef)] = None
    var responseA: Option[Boolean] = None
    var responseB: Option[Boolean] = None
    var interrogationId: Int = 0
    var requester: Option[ActorRef] = None

    def receive = {
      case Interrogate(prisoner, actorRef, sendVertictTo) ⇒
        requester = Some(sendVertictTo)
        if (maybePrisonerAndActorA.isEmpty) {
          maybePrisonerAndActorA = Some((prisoner, actorRef))
          sender ! WillInterrogate
        } else if (maybePrisonerAndActorB.isEmpty) {
          maybePrisonerAndActorB = Some((prisoner, actorRef))
          sender ! WillInterrogate
        } else sender ! WontInterrogate
        interrogateIfAllPrisonersHere()
      case guilty: Guilty ⇒
        if (guilty.interrogationId == interrogationId) {
          for {
            (prisonerA, actorRefA) ← maybePrisonerAndActorA
            (prisonerB, actorRefB) ← maybePrisonerAndActorB
          } yield {
            if (sender == actorRefA) responseA = Some(true)
            if (sender == actorRefB) responseB = Some(true)
          }
          sendVerdictIfPossible()
        }
      case wontTell: IWontTell ⇒
        if (wontTell.interrogationId == interrogationId) {
          for {
            (prisonerA, actorRefA) ← maybePrisonerAndActorA
            (prisonerB, actorRefB) ← maybePrisonerAndActorB
          } yield {
            if (sender == actorRefA) responseA = Some(false)
            if (sender == actorRefB) responseB = Some(false)
          }
          sendVerdictIfPossible()
        }
    }

    private def interrogateIfAllPrisonersHere(): Unit = {
      for {
        (prisonerA, actorRefA) ← maybePrisonerAndActorA
        (prisonerB, actorRefB) ← maybePrisonerAndActorB
      } yield {
        actorRefA ! TellIfGuilty(interrogationId, prisonerB)
        actorRefB ! TellIfGuilty(interrogationId, prisonerA)
      }
    }

    private def sendVerdictIfPossible(): Unit = {
      for {
        (prisonerA, actorRefA) ← maybePrisonerAndActorA
        (prisonerB, actorRefB) ← maybePrisonerAndActorB
        guiltyB ← responseA
        guiltyA ← responseB
      } yield {
        if (guiltyA && guiltyB)
          requester.foreach { _ ! Sentences(3, 3) }
        else if (guiltyA)
          requester.foreach { _ ! Sentences(3, 0) }
        else if (guiltyB)
          requester.foreach { _ ! Sentences(0, 3) }
        else
          requester.foreach { _ ! Sentences(1, 1) }

        maybePrisonerAndActorA = None
        maybePrisonerAndActorB = None
        responseA = None
        responseB = None
        interrogationId += 1
        requester = None
      }
    }
  }

  object ClassicApproach {
    class PrisonerActor(prisoner: Prisoner) extends Actor {
      def receive = {
        case question: TellIfGuilty ⇒
          if (question.prisoner.name.startsWith("a"))
            sender ! Guilty(question.interrogationId)
          else
            sender ! IWontTell(question.interrogationId)
        case Sentence(years) ⇒
          println("I, " + prisoner.name + " got " + years + " years")
      }
    }

    def run() = {
      val prisoner = Prisoner("ai-prisoner")

      implicit val system = ActorSystem("FreeActors")
      val prisonerActor = system.actorOf(Props(classOf[Prisoner], prisoner), prisoner.name)

      system.terminate()
    }
  }

  ClassicApproach.run()
}
