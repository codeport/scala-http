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

import akka.util.ByteString

/**
 * HTTP 상수 모음
 */
object HttpConstants {
  val SP                   = ByteString(" ")
  val HT                   = ByteString("\t")
  val CRLF                 = ByteString("\r\n")
  val COLON                = ByteString(":")
  val PERCENT              = ByteString("%")
  val PATH                 = ByteString("/")
  val QUERY                = ByteString("?")
  val RFC1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz"
}

// vim: set ts=2 sw=2 et:
