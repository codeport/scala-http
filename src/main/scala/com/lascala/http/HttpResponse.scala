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

import akka.util.{ ByteString, ByteStringBuilder }
import java.io.File
import org.apache.tika.Tika
import java.io.FileInputStream
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone

trait HttpResponse {
  def lastModified: Date = null
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
  val version = ByteString("HTTP/1.1")
  val server = ByteString("Server: lascala-http")
  val connection = ByteString("Connection: ")
  val keepAlive = ByteString("Keep-Alive")
  val close = ByteString("Close")
  val date = ByteString("Date: ")
  val lastModified = ByteString("Last-Modified: ")

  def httpDateFormat = {
    val dateFormat = new SimpleDateFormat(RFC1123_DATE_PATTERN, Locale.ENGLISH)
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    dateFormat
  }

  def httpDate(date: Date) = ByteString(httpDateFormat.format(date))

  def bytes(rsp: HttpResponse) = {
    (new ByteStringBuilder ++=
    version ++= SP ++= rsp.status ++= SP ++= rsp.reason ++= CRLF ++=
    (if(rsp.body.nonEmpty || rsp.mimeType.nonEmpty) rsp.contentType ++ CRLF else ByteString.empty) ++=
    rsp.cacheControl ++= CRLF ++=
    date ++= httpDate(new Date) ++= CRLF ++=
    Option(rsp.lastModified).map(lastModified ++ httpDate(_) ++ CRLF).getOrElse(ByteString("")) ++=
    server ++= CRLF ++=
    rsp.contentLength ++= CRLF ++=
    connection ++= (if (rsp.shouldKeepAlive) keepAlive else close) ++= CRLF ++= CRLF ++= rsp.body).result
  }
}

case class OKFileResponse(file: File, shouldKeepAlive: Boolean = true) extends HttpResponse {
	def readFile(file: File) = {
    val resource = new Array[Byte](file.length.toInt)
    val in = new FileInputStream(file)
    in.read(resource)
    in.close()
    ByteString(resource)
  }
  val body = readFile(file)
  val mimeType = new Tika().detect(file)
  val status = ByteString("200")
  val reason = ByteString("OK")
  override def lastModified = new Date(file.lastModified)
}

case class OKResponse(body: ByteString, shouldKeepAlive: Boolean = true, mimeType: String = "text/html") extends HttpResponse {
  val status = ByteString("200")
  val reason = ByteString("OK")
}

case class NotModifiedResponse(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
  val status = ByteString("304")
  val reason = ByteString("Not Modified")
}

case class NotFoundError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
  val status = ByteString("404")
  val reason = ByteString("Not Found")
}

case class MethodNotAllowedError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
  val status = ByteString("405")
  val reason = ByteString("Method Not Allowed")
}

case class InternalServerError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
  val status = ByteString("500")
  val reason = ByteString("Internal Server Error")
}
