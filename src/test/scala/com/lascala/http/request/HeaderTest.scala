/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 *  Copyright 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.lascala.http.request

import org.scalatest._
import org.scalatest.matchers._
import Header.QParamHeader

class HeaderTest extends FlatSpec with ShouldMatchers {
  "Header" can "process Content-Encoding header with q parameters correctly" in {
    val res = Header.parseQparameters(Header("Content-Encoding","gzip; q=0.7, deflate; q=0.8, test; q=1.000, test2; q=0.000"))
    val expectedQParamHeaders = Seq(
      QParamHeader("test", 1.000),
      QParamHeader("deflate", 0.8),
      QParamHeader("gzip", 0.7),
      QParamHeader("test2", 0.000))

    res should be (expectedQParamHeaders)
  }

  it can "process Content-Encoding header WITHOUT q parameters correctly" in {
    val res = Header.parseQparameters(Header("Content-Encoding","gzip, deflate, test, test2"))
    val expectedQParamHeaders = Seq(
      QParamHeader("gzip", 1.0),
      QParamHeader("deflate", 1.0),
      QParamHeader("test", 1.0),
      QParamHeader("test2", 1.0))

    res should be (expectedQParamHeaders)
  }

  it can "process Content-Encoding header with values that may or may not have q values" in {
    val res = Header.parseQparameters(Header("Content-Encoding","gzip; q=0.7, deflate, test; q=0.5, test2"))

    val expectedQParamHeaders = Seq(
      QParamHeader("deflate", 1.0),
      QParamHeader("test2", 1.0),
      QParamHeader("gzip", 0.7),
      QParamHeader("test", 0.5))

    res should be (expectedQParamHeaders)
  }
}
