package com.michalplachta.cats

import cats.Id
import org.scalatest.{ AsyncWordSpec, Matchers }

import scala.concurrent.Future

class GettingRidOfFuturesAsyncSpec extends AsyncWordSpec with Matchers {
  "The tests are not very nice" when {
    "addIntsInFuture" should {
      "return a 5 when adding 2 and future 3 (async assertion)" in {
        val resultPromise: Future[Int] = GettingRidOfFutures.addIntsInFuture(2, Future.successful(3))
        resultPromise map { _ should be(5) }
      }
    }
  }

  "The tests are nice" when {
    "addIntsInContext" should {
      "return a 5 when adding 2 and 3" in {
        val result = GettingRidOfFutures.addIntsInContext[Id](2, 3)
        result should be(5)
      }
    }

    "addInContext" should {
      "return a 5.0 when adding 2.0 and 3.0" in {
        val result = GettingRidOfFutures.addInContext[Id, Double](2.0, 3.0)
        result should be(5.0)
      }
    }
  }
}
