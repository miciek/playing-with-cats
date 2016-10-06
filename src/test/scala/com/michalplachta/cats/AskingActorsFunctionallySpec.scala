package com.michalplachta.cats

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}

import cats.Id
import cats.data.ReaderT

import com.michalplachta.cats.AskingActorsFunctionally.ClassicApproach.StringStatsCalculator
import com.michalplachta.cats.AskingActorsFunctionally.ExternalStuffDoNotTouch.StringStats
import com.michalplachta.cats.AskingActorsFunctionally.FunctionalApproach.stringStats

import org.scalatest.{GivenWhenThen, Matchers, WordSpecLike}

class AskingActorsFunctionallySpec extends TestKit(ActorSystem("AskingActors"))
                                   with WordSpecLike with Matchers with ImplicitSender with GivenWhenThen {
  "The tests are not very nice" when {
    "StringStatsCalculator actor should return proper StringStats for 'abba'" in {
      Given("properly configured actor system")
      val lengthCalculatorProbe = TestProbe()
      val palindromeCheckerProbe = TestProbe()
      val underTest = system.actorOf(Props(classOf[StringStatsCalculator], lengthCalculatorProbe.ref, palindromeCheckerProbe.ref))

      When("we send a String to the tested actor")
      underTest ! "abba"

      And("mocked palindrome and length responses are provided")
      lengthCalculatorProbe.expectMsg("abba")
      lengthCalculatorProbe.reply(4)

      palindromeCheckerProbe.expectMsg("abba")
      palindromeCheckerProbe.reply(true)

      Then("we should get proper StringStats back")
      expectMsg(StringStats(length = 4, palindrome = true))
    }
  }

  "The tests are nice" when {
    "stringStats function should return proper StringStats for 'abba'" in {
      Given("some mocked responses")
      val mockedCalculateLength = ReaderT[Id, String, Int] { (s: String) => 4 }
      val mockedCheckPalindrome = ReaderT[Id, String, Boolean] { (s: String) => true }

      When("we run stringStats function against a String")
      val result = stringStats(mockedCalculateLength, mockedCheckPalindrome).run("abba")

      Then("we should get proper StringStats back")
      result should be(StringStats(length = 4, palindrome = true))
    }
  }
}
