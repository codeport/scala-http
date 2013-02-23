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
package com.lascala.http.response

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import akka.util.ByteString
import java.io.ByteArrayInputStream
import java.util.zip.{InflaterInputStream, GZIPInputStream}

class HttpResponseTest extends FlatSpec with ShouldMatchers {
  "HttpResponse" should "be able to compress body using gzip compression" in {
    val resp = new OKResponse(ByteString("gzip comression test")) with GZipSupport
    resp.bodyData.utf8String should not be ("gzip comression test")

    val is = new ByteArrayInputStream(resp.bodyData.toArray)
    val gzip = new GZIPInputStream(is)
    val decompressedBody = scala.io.Source.fromInputStream(gzip).getLines().mkString

    decompressedBody should be ("gzip comression test")
  }

  it should "be able to compress body using deflater" in {
    val resp = new OKResponse(ByteString("deflater support test")) with DeflateSupport
    resp.bodyData.utf8String should not be ("deflater support test")

    val is = new ByteArrayInputStream(resp.bodyData.toArray)
    val inflater = new InflaterInputStream(is)
    val decompressedBody = scala.io.Source.fromInputStream(inflater).getLines().mkString

    decompressedBody should be ("deflater support test")
  }
}
