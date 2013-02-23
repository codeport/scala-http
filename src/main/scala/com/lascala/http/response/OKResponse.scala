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

import akka.util.ByteString
import com.lascala.http.{ChunkedEncodable, HttpResponse}
import com.lascala.libs.Enumerator
import java.io.File
import java.util.Date
import org.apache.tika.Tika

case class OKResponse(body: ByteString         = ByteString.empty,
                      shouldKeepAlive: Boolean = true,
                      mimeType: String         = "text/html",
                      lastModified: Date       = null,
                      etag: ByteString         = null) extends HttpResponse {
  val status = ByteString("200")
  val reason = ByteString("OK")

  def withMimeType(mimeType: String) = this match {
    // In case of ChunkedEncodable, need to manually instantiate a new OKResponse with ChunkedEncodable
    // Instead of just using copy method in order to preserve the ChunkedEncodable type.
    case t: ChunkedEncodable =>
      new OKResponse(this.body, this.shouldKeepAlive, mimeType, this.lastModified, this.etag) with ChunkedEncodable {
        def chunkedData: Enumerator[ByteString] = t.chunkedData
      }
    case _ => this.copy(mimeType = mimeType)
  }

  def withGzipCompression = this match {
    // In case of ChunkedEncodable, need to manually instantiate a new OKResponse with ChunkedEncodable
    // Instead of just using copy method in order to preserve the ChunkedEncodable type.
    case t: ChunkedEncodable =>
      new OKResponse(this.body, this.shouldKeepAlive, this.mimeType, this.lastModified, this.etag) with GZipSupport with ChunkedEncodable {
        def chunkedData: Enumerator[ByteString] = t.chunkedData
      }
    case _ => new OKResponse(this.body, this.shouldKeepAlive, this.mimeType, this.lastModified, this.etag) with GZipSupport
  }
}

object OKResponse {

  def stream(chunk: Enumerator[ByteString]) =
    new OKResponse(body = ByteString.empty, mimeType = "text/html") with ChunkedEncodable {
      def chunkedData: Enumerator[ByteString] = chunk
    }

  def fromFile(file: File) = new OKResponse(
    body            = HttpResponse.readFile(file),
    shouldKeepAlive = true,
    mimeType        = new Tika().detect(file),
    lastModified    = new Date(file.lastModified),
    etag            = ByteString(HttpResponse.computeETag(file)))
}
