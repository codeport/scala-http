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
