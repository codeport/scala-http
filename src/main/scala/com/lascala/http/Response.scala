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

object OKResponse {
  import HttpConstants.CRLF
	
  val okStatus = ByteString("HTTP/1.1 200 OK")
  val contentType = ByteString("Content-Type: text/html; charset=utf-8")
  val cacheControl = ByteString("Cache-Control: no-cache")
  val date = ByteString("Date: ")
  val server = ByteString("Server: Akka")
  val contentLength = ByteString("Content-Length: ")
  val connection = ByteString("Connection: ")
  val keepAlive = ByteString("Keep-Alive")
  val close = ByteString("Close")
	
  def bytes(rsp: OKResponse) = {
    new ByteStringBuilder ++=
    okStatus ++= CRLF ++=
    contentType ++= CRLF ++=
    cacheControl ++= CRLF ++=
    date ++= ByteString(new java.util.Date().toString) ++= CRLF ++=
    server ++= CRLF ++=
    contentLength ++= ByteString(rsp.body.length.toString) ++= CRLF ++=
    connection ++= (if (rsp.keepAlive) keepAlive else close) ++= CRLF ++= CRLF ++= rsp.body result
  }
}

case class OKResponse(body: ByteString, keepAlive: Boolean)
