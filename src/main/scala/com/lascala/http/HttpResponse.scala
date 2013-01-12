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

import akka.util.{ ByteString, ByteStringBuilder }
import HttpConstants._

trait HttpResponse {
  def body: ByteString
  def status: ByteString
  def reason: ByteString
  def mimeType: String
  def shouldKeepAlive: Boolean

  // default charset to utf-8 for now but should be editable in the future
  def contentType   = if (!mimeType.isEmpty) ByteString(s"Content-Type: ${mimeType}") else ByteString("")
  def cacheControl  = ByteString("Cache-Control: no-cache")
  def contentLength = ByteString(s"Content-Length: ${body.length.toString}")
}

object HttpResponse {
  val version = ByteString("HTTP/1.1")
  val date = ByteString("Date: ")
  val server = ByteString("Server: lascala-http")
  val connection = ByteString("Connection: ")
  val keepAlive = ByteString("Keep-Alive")
  val close = ByteString("Close")
	
  def bytes(rsp: HttpResponse) = {
    (new ByteStringBuilder ++=
    version ++= SP ++= rsp.status ++= SP ++= rsp.reason ++= CRLF ++=
    (if(rsp.body.nonEmpty) rsp.contentType ++ CRLF else ByteString.empty) ++=
    rsp.cacheControl ++= CRLF ++=
    date ++= ByteString(new java.util.Date().toString) ++= CRLF ++=
    server ++= CRLF ++=
    rsp.contentLength ++= CRLF ++=
    connection ++= (if (rsp.shouldKeepAlive) keepAlive else close) ++= CRLF ++= CRLF ++= rsp.body).result
  }
}

case class OKResponse(body: ByteString, shouldKeepAlive: Boolean = true, mimeType: String = "text/html") extends HttpResponse {
  val status = ByteString("200")
  val reason = ByteString("OK")
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

/**
 * HTTP 상수 모음
 */
object HttpConstants {
  val SP = ByteString(" ")
  val HT = ByteString("\t")
  val CRLF = ByteString("\r\n")
  val COLON = ByteString(":")
  val PERCENT = ByteString("%")
  val PATH = ByteString("/")
  val QUERY = ByteString("?")
}


