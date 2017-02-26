package com.michalplachta.cats

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }
import com.michalplachta.cats.ImprisonedActors._
import org.scalatest.{ GivenWhenThen, Matchers, WordSpecLike }

class ImprisonedActorsSpec extends TestKit(ActorSystem("FreeActors"))
    with WordSpecLike with Matchers with ImplicitSender with GivenWhenThen {
  trait TestInterrogation {
    val probeA = TestProbe()
    val probeB = TestProbe()

    val prosecutor = system.actorOf(Props[Prosecutor])

    prosecutor ! Interrogate(Prisoner("A"), probeA.ref, self)
    expectMsg(WillInterrogate)

    prosecutor ! Interrogate(Prisoner("B"), probeB.ref, self)
    expectMsg(WillInterrogate)
  }

  "Prosecutor" should {
    "sentence both prisoners to 3 years if both betray" in new TestInterrogation {
      val questionA = probeA.expectMsgClass(classOf[TellIfGuilty])
      probeA.reply(Guilty(questionA.interrogationId))

      val questionB = probeB.expectMsgClass(classOf[TellIfGuilty])
      probeB.reply(Guilty(questionA.interrogationId))

      expectMsg(Sentences(prisonerAyears = 3, prisonerByears = 3))
    }

    "sentence only prisoner A if B betrayed him" in new TestInterrogation {
      val questionA = probeA.expectMsgClass(classOf[TellIfGuilty])
      probeA.reply(IWontTell(questionA.interrogationId))

      val questionB = probeB.expectMsgClass(classOf[TellIfGuilty])
      probeB.reply(Guilty(questionA.interrogationId))

      expectMsg(Sentences(prisonerAyears = 3, prisonerByears = 0))
    }

    "sentence only prisoner B if A betrayed him" in new TestInterrogation {
      val questionA = probeA.expectMsgClass(classOf[TellIfGuilty])
      probeA.reply(Guilty(questionA.interrogationId))

      val questionB = probeB.expectMsgClass(classOf[TellIfGuilty])
      probeB.reply(IWontTell(questionA.interrogationId))

      expectMsg(Sentences(prisonerAyears = 0, prisonerByears = 3))
    }

    "sentence everybody for 1 year if both silent" in new TestInterrogation {
      val questionA = probeA.expectMsgClass(classOf[TellIfGuilty])
      probeA.reply(IWontTell(questionA.interrogationId))

      val questionB = probeB.expectMsgClass(classOf[TellIfGuilty])
      probeB.reply(IWontTell(questionA.interrogationId))

      expectMsg(Sentences(prisonerAyears = 1, prisonerByears = 1))
    }
  }
}
