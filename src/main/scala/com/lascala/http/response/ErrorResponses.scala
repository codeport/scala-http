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
package com.lascala.http.response

import akka.util.ByteString
import com.lascala.http.HttpResponse

package error {
  case class NotFoundError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
    val status       = ByteString("404")
    val reason       = ByteString("Not Found")
    val lastModified = null
    val etag         = null
  }

  case class MethodNotAllowedError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
    val status       = ByteString("405")
    val reason       = ByteString("Method Not Allowed")
    val lastModified = null
    val etag         = null
  }

  case class InternalServerError(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
    val status       = ByteString("500")
    val reason       = ByteString("Internal Server Error")
    val lastModified = null
    val etag         = null
  }
}

package other {
  case class NotModifiedResponse(body: ByteString = ByteString.empty, shouldKeepAlive: Boolean = false, mimeType: String = "") extends HttpResponse {
    val status = ByteString("304")
    val reason = ByteString("Not Modified")
    val lastModified = null
    val etag = null
  }
}
