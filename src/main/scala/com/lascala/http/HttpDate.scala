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
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

trait HttpDate {
  def asString: String
  def asDate: Date
  def asLong: Long
}

object HttpDate {
  def format = {
    val dateFormat = new SimpleDateFormat(RFC1123_DATE_PATTERN, Locale.ENGLISH)
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    dateFormat
  }

  case class HttpDateBasedString(date: String) extends HttpDate {
    def asString = date
    def asLong = asDate.getTime
    def asDate = format.parse(date)
  }
  
  case class HttpDateBasedLong(date: Long) extends HttpDate {
    def asString = format.format(date)
    def asLong = date
    def asDate = new Date(date)
  }
  
  case class HttpDateBasedDate(date: Date) extends HttpDate {
    def asString = format.format(date)
    def asLong = date.getTime
    def asDate = date
  }

  def apply(date: String) = HttpDateBasedString(date)
  def apply(date: Long) = HttpDateBasedLong(date)
  def apply(date: Date) = HttpDateBasedDate(date)
}
