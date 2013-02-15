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

case class Header(name: String, value: String)

object Header {
  final val ACCEPT                           = "Accept";
  final val ACCEPT_CHARSET                   = "Accept-Charset";
  final val ACCEPT_ENCODING                  = "Accept-Encoding";
  final val ACCEPT_LANGUAGE                  = "Accept-Language";
  final val ACCEPT_RANGES                    = "Accept-Ranges";
  final val AGE                              = "Age";
  final val ALLOW                            = "Allow";
  final val AUTHORIZATION                    = "Authorization";
  final val CACHE_CONTROL                    = "Cache-Control";
  final val CONNECTION                       = "Connection";
  final val CONTENT_ENCODING                 = "Content-Encoding";
  final val CONTENT_LANGUAGE                 = "Content-Language";
  final val CONTENT_LENGTH                   = "Content-Length";
  final val CONTENT_LOCATION                 = "Content-Location";
  final val CONTENT_MD5                      = "Content-MD5";
  final val CONTENT_RANGE                    = "Content-Range";
  final val CONTENT_TRANSFER_ENCODING        = "Content-Transfer-Encoding";
  final val CONTENT_TYPE                     = "Content-Type";
  final val COOKIE                           = "Cookie";
  final val DATE                             = "Date";
  final val ETAG                             = "Etag";
  final val EXPECT                           = "Expect";
  final val EXPIRES                          = "Expires";
  final val FROM                             = "From";
  final val HOST                             = "Host";
  final val IF_MATCH                         = "If-Match";
  final val IF_MODIFIED_SINCE                = "If-Modified-Since";
  final val IF_NONE_MATCH                    = "If-None-Match";
  final val IF_RANGE                         = "If-Range";
  final val IF_UNMODIFIED_SINCE              = "If-Unmodified-Since";
  final val LAST_MODIFIED                    = "Last-Modified";
  final val LOCATION                         = "Location";
  final val MAX_FORWARDS                     = "Max-Forwards";
  final val PRAGMA                           = "Pragma";
  final val PROXY_AUTHENTICATE               = "Proxy-Authenticate";
  final val PROXY_AUTHORIZATION              = "Proxy-Authorization";
  final val RANGE                            = "Range";
  final val REFERER                          = "Referer";
  final val RETRY_AFTER                      = "Retry-After";
  final val SERVER                           = "Server";
  final val SET_COOKIE                       = "Set-Cookie";
  final val SET_COOKIE2                      = "Set-Cookie2";
  final val TE                               = "Te";
  final val TRAILER                          = "Trailer";
  final val TRANSFER_ENCODING                = "Transfer-Encoding";
  final val UPGRADE                          = "Upgrade";
  final val USER_AGENT                       = "User-Agent";
  final val VARY                             = "Vary";
  final val VIA                              = "Via";
  final val WARNING                          = "Warning";
  final val WWW_AUTHENTICATE                 = "WWW-Authenticate";
  final val ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin";
  final val ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers";
  final val ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age";
  final val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
  final val ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods";
  final val ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers";
  final val ORIGIN                           = "Origin";
  final val ACCESS_CONTROL_REQUEST_METHOD    = "Access-Control-Request-Method";
  final val ACCESS_CONTROL_REQUEST_HEADERS   = "Access-Control-Request-Headers";
}

case class Headers(headers: List[Header]) {
  def get(name: String): Option[Header]  = headers.find(_.name.toLowerCase == name.toLowerCase)
  def topContentEncoding: Option[String] = get(Header.CONTENT_ENCODING).flatMap(Headers.parseQparameters(_).headOption).map(_.value)
  def contentEncodings: Seq[String]      = get(Header.CONTENT_ENCODING).map(Headers.parseQparameters(_).map(_.value)).getOrElse(Seq.empty[String])
}

object Headers {
  private final val VALUE_SEPARATOR   = ","
  private final val Q_PARAM_SEPARATOR = ";"
  private final val Q_PARAM           = "q="

  case class QParamHeader(value: String, qParam: Double)

  def parseQparameters(header: Header) = header.value.split(VALUE_SEPARATOR).toSeq.map{
    _.split(Q_PARAM_SEPARATOR) match {
      case Array(value, qParam) =>
        val qRate = if(qParam.contains(Q_PARAM)) qParam.split(Q_PARAM).last.toDouble else 1.0
        QParamHeader(value.trim, qRate)
      case Array(value) => QParamHeader(value.trim, 1.0)
    }
  }.sortWith(_.qParam > _.qParam)
}