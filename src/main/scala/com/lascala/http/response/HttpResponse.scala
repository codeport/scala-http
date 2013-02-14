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
import org.apache.tika.Tika
import java.io.FileInputStream
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone
import akka.actor.IO
import util.Failure
import com.lascala.libs.Enumerator
import java.security.MessageDigest
import java.security.DigestInputStream
import java.io.BufferedInputStream
import com.lascala.http.HttpDate._

trait HttpResponse {
  def lastModified: Date
  def etag: ByteString
  def body: ByteString
  def status: ByteString
  def reason: ByteString
  def mimeType: String
  def shouldKeepAlive: Boolean

  def contentType   = ByteString(s"Content-Type: ${mimeType}")
  def cacheControl  = ByteString("Cache-Control: no-cache")
  def contentLength = ByteString(s"Content-Length: ${body.length.toString}")
}

object HttpResponse {
  val version      = ByteString("HTTP/1.1")
  val server       = ByteString("Server: lascala-http")
  val connection   = ByteString("Connection: ")
  val keepAlive    = ByteString("Keep-Alive")
  val close        = ByteString("Close")
  val date         = ByteString("Date: ")
  val lastModified = ByteString("Last-Modified: ")
  val etag = ByteString("ETag: ")

	def readFile(file: File) = {
    val resource = new Array[Byte](file.length.toInt)
    val in = new FileInputStream(file)
    in.read(resource)
    in.close()
    ByteString(resource)
  }

  def computeETag(file: File) = {
    val algorithm = MessageDigest.getInstance("SHA1")
    val dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), algorithm)
    while (dis.read() != -1) {}
    algorithm.digest().fold("")(_ + "%02x" format _).toString
  }

  def bytes(rsp: HttpResponse) = {
    (new ByteStringBuilder ++=
    version ++= SP ++= rsp.status ++= SP ++= rsp.reason ++= CRLF ++=
    (if(rsp.body.nonEmpty || rsp.mimeType.nonEmpty) rsp.contentType ++ CRLF else ByteString.empty) ++=
    rsp.cacheControl ++= CRLF ++=
    date ++= ByteString(HttpDate(new Date).asString) ++= CRLF ++=
    Option(rsp.lastModified).map((v) => lastModified ++ ByteString(HttpDate(v).asString) ++ CRLF).getOrElse(ByteString("")) ++=
    Option(rsp.etag).map(etag ++ _ ++ CRLF).getOrElse(ByteString("")) ++=
    server ++= CRLF ++=
    rsp.contentLength ++= CRLF ++=
    connection ++= (if (rsp.shouldKeepAlive) keepAlive else close) ++= CRLF ++= CRLF ++= rsp.body).result
  }

  def stream(rsp: HttpResponse with ChunkedEncodable, socket: IO.SocketHandle) = {
    val headers = (new ByteStringBuilder ++=
      version ++= SP ++= rsp.status ++= SP ++= rsp.reason ++= CRLF ++=
      rsp.contentType ++ CRLF ++=
      rsp.cacheControl ++= CRLF ++=
      date ++= ByteString(HttpDate(new Date).asString) ++= CRLF ++=
      Option(rsp.lastModified).map((v) => lastModified ++ ByteString(HttpDate(v).asString) ++ CRLF).getOrElse(ByteString("")) ++=
      server ++= CRLF ++=
      connection ++= (if (rsp.shouldKeepAlive) keepAlive else close) ++= CRLF ++=
      ByteString("Transfer-Encoding: chunked") ++= CRLF ++= CRLF).result

    socket write headers.compact

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

