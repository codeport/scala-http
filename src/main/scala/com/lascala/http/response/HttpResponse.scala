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
package com.lascala.http

import com.lascala.http.HttpConstants._

import akka.util.{ByteString, ByteStringBuilder}
import java.io.File
import java.io.FileInputStream
import java.util.Date
import akka.actor.IO
import com.lascala.libs.Enumerator
import java.security.MessageDigest
import java.security.DigestInputStream
import java.io.BufferedInputStream
import response.Compression

trait HttpResponse {
  def lastModified: Date
  def etag: ByteString
  def body: ByteString
  def status: ByteString
  def reason: ByteString
  def mimeType: String
  def shouldKeepAlive: Boolean
  def bodyData: ByteString = body

  def contentType   = ByteString(s"${Header.CONTENT_TYPE}: ${mimeType}")
  def cacheControl  = ByteString(Header.CACHE_CONTROL + ": no-cache")
  def contentLength = ByteString(s"${Header.CONTENT_LENGTH}: ${bodyData.length.toString}")
}

object HttpResponse {
  val version      = ByteString("HTTP/1.1")
  val server       = ByteString(s"${Header.SERVER}: lascala-http")
  val connection   = ByteString(s"${Header.CONNECTION}: ")
  val keepAlive    = ByteString(s"Keep-Alive")
  val close        = ByteString("Close")
  val date         = ByteString(s"${Header.DATE}: ")
  val lastModified = ByteString(s"${Header.LAST_MODIFIED}: ")
  val etag         = ByteString(s"${Header.ETAG}: ")

	def readFile(file: File) = {
    val resource = new Array[Byte](file.length.toInt)
    val in       = new FileInputStream(file)

    in.read(resource)
    in.close()
    ByteString(resource)
  }

  def computeETag(file: File) = {
    val algorithm = MessageDigest.getInstance("SHA1")
    val dis       = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), algorithm)
    while (dis.read() != -1) {}
    algorithm.digest().fold("")(_ + "%02x" format _).toString
  }

  def headers(rsp: HttpResponse, chunkedEncoding: Boolean = false) = {
   val header = new ByteStringBuilder ++=
      version ++= SP ++= rsp.status ++= SP ++= rsp.reason ++= CRLF ++=
      (if(rsp.bodyData.nonEmpty || rsp.mimeType.nonEmpty) rsp.contentType ++ CRLF else ByteString.empty) ++=
      rsp.cacheControl ++= CRLF ++=
      date ++= ByteString(HttpDate(new Date).asString) ++= CRLF ++=
      Option(rsp.lastModified).map((v) => lastModified ++ ByteString(HttpDate(v).asString) ++ CRLF).getOrElse(ByteString("")) ++=
      Option(rsp.etag).map(etag ++ _ ++ CRLF).getOrElse(ByteString("")) ++=
      server ++= CRLF ++=
      (if(chunkedEncoding) ByteString.empty else rsp.contentLength) ++=
      CRLF ++= connection ++= (if (rsp.shouldKeepAlive) keepAlive else close)

   // If content encoding has been applied, specify so in the header
   rsp match {
     case c: Compression => header ++= CRLF ++= ByteString(s"${Header.CONTENT_ENCODING}: ${c.compressionMethod}")
     case _ => header
   }
  }

  def bytes(rsp: HttpResponse) = (headers(rsp) ++= CRLF ++= CRLF ++= rsp.bodyData).result

  def stream(rsp: HttpResponse with ChunkedEncodable, socket: IO.SocketHandle) {
    val header = (headers(rsp, chunkedEncoding = true) ++= CRLF
      ++= ByteString("Transfer-Encoding: chunked") ++= CRLF ++= CRLF).result

    socket write header.compact

    rsp.chunkedData.foreach { chunk =>
      val chunkedMessageBody = (
        new ByteStringBuilder ++= ByteString(chunk.size.toHexString) ++= CRLF
          ++= chunk ++= CRLF).result

      socket write chunkedMessageBody.compact
    }

    // According to the HTTP 1.1 spec, need to write 0 at the end of the chunk data
    socket write ByteString("0") ++ CRLF ++ CRLF
  }
}

/**
 * Represents chunked data response
 */
trait ChunkedEncodable extends HttpResponse {
  def chunkedData: Enumerator[ByteString]
}

