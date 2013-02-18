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
  final val ACCEPT                           = "Accept"
  final val ACCEPT_CHARSET                   = "Accept-Charset"
  final val ACCEPT_ENCODING                  = "Accept-Encoding"
  final val ACCEPT_LANGUAGE                  = "Accept-Language"
  final val ACCEPT_RANGES                    = "Accept-Ranges"
  final val AGE                              = "Age"
  final val ALLOW                            = "Allow"
  final val AUTHORIZATION                    = "Authorization"
  final val CACHE_CONTROL                    = "Cache-Control"
  final val CONNECTION                       = "Connection"
  final val CONTENT_ENCODING                 = "Content-Encoding"
  final val CONTENT_LANGUAGE                 = "Content-Language"
  final val CONTENT_LENGTH                   = "Content-Length"
  final val CONTENT_LOCATION                 = "Content-Location"
  final val CONTENT_MD5                      = "Content-MD5"
  final val CONTENT_RANGE                    = "Content-Range"
  final val CONTENT_TRANSFER_ENCODING        = "Content-Transfer-Encoding"
  final val CONTENT_TYPE                     = "Content-Type"
  final val COOKIE                           = "Cookie"
  final val DATE                             = "Date"
  final val ETAG                             = "Etag"
  final val EXPECT                           = "Expect"
  final val EXPIRES                          = "Expires"
  final val FROM                             = "From"
  final val HOST                             = "Host"
  final val IF_MATCH                         = "If-Match"
  final val IF_MODIFIED_SINCE                = "If-Modified-Since"
  final val IF_NONE_MATCH                    = "If-None-Match"
  final val IF_RANGE                         = "If-Range"
  final val IF_UNMODIFIED_SINCE              = "If-Unmodified-Since"
  final val LAST_MODIFIED                    = "Last-Modified"
  final val LOCATION                         = "Location"
  final val MAX_FORWARDS                     = "Max-Forwards"
  final val PRAGMA                           = "Pragma"
  final val PROXY_AUTHENTICATE               = "Proxy-Authenticate"
  final val PROXY_AUTHORIZATION              = "Proxy-Authorization"
  final val RANGE                            = "Range"
  final val REFERER                          = "Referer"
  final val RETRY_AFTER                      = "Retry-After"
  final val SERVER                           = "Server"
  final val SET_COOKIE                       = "Set-Cookie"
  final val SET_COOKIE2                      = "Set-Cookie2"
  final val TE                               = "Te"
  final val TRAILER                          = "Trailer"
  final val TRANSFER_ENCODING                = "Transfer-Encoding"
  final val UPGRADE                          = "Upgrade"
  final val USER_AGENT                       = "User-Agent"
  final val VARY                             = "Vary"
  final val VIA                              = "Via"
  final val WARNING                          = "Warning"
  final val WWW_AUTHENTICATE                 = "WWW-Authenticate"
  final val ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin"
  final val ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers"
  final val ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age"
  final val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"
  final val ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods"
  final val ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers"
  final val ORIGIN                           = "Origin"
  final val ACCESS_CONTROL_REQUEST_METHOD    = "Access-Control-Request-Method"
  final val ACCESS_CONTROL_REQUEST_HEADERS   = "Access-Control-Request-Headers"
}

case class Headers(headers: List[Header]) {
  def get(name: String): Option[Header]  = headers.find(_.name.toLowerCase == name.toLowerCase)
  def topAcceptEncoding: Option[String]  = get(Header.ACCEPT_ENCODING).flatMap(Headers.parseQparameters(_).headOption).map(_.value)
  def acceptEncodings: Seq[String]       = get(Header.ACCEPT_ENCODING).map(Headers.parseQparameters(_).map(_.value)).getOrElse(Seq.empty[String])
}

object Headers {
  private final val VALUE_SEPARATOR   = ","
  private final val Q_PARAM_SEPARATOR = ";"
  private final val Q_PARAM           = "q="

  case class QParamHeader(value: String, qParam: Double)

  /**
   * 1. q값은 0.000부터 1.000까지 가능하며(소수점 세자리까지 사용가능) 이 값에 의해 우선순위가 결정됩니다. 
   * 예) "Accept-Encoding: gzip; q=0.7, deflate; q=0.8" 라면 deflate가 우선순위가 높음
   * 
   * 2. q 파라메터가 지정되지 않은 경우, 순서는 우선순위와 아무 관련이 없습니다. 
   * 예) "Accept-Encoding: gzip, defalte"는 gzip이 deflate보다 우선순위가 높다는 의미가 아님.
   * 
   * 3. 2에도 불구하고, q 파라메터가 없는 경우에 순서를 우선순위로 간주하는 클라이언트가 있을 수 있습니다. 
   * 따라서 q 파라메터가 없을 때는 그냥 순서를 우선순위로 가정하고 처리하는 것이 좋습니다.
   * 그렇게 한다고 해서 스펙을 위반하는 것은 아니기 때문입니다.
   *
   * 4. A lack of a q param should be treated as having a q param = 1.0  . Thus, if some values are mssing
   * q param, thye should be taken as if they have q param = 1.0
   *
   * 5. Between "q" and "=" can be LWS(Linear White Space). 
   */
  def parseQparameters(header: Header) = header.value.replaceAll("\\s+","").split(VALUE_SEPARATOR).toSeq.map{
    _.split(Q_PARAM_SEPARATOR) match {
      case Array(value, qParam) =>
        val qRate = if(qParam.contains(Q_PARAM)) qParam.split(Q_PARAM).last.toDouble else 1.0
        QParamHeader(value.trim, qRate)
      case Array(value) => QParamHeader(value.trim, 1.0)
    }
  }.sortWith(_.qParam > _.qParam)
}
