/**
 * la-scala http server
 * source from http://doc.akka.io/docs/akka/2.1.0/scala/io.html
 */

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

package common

import akka.actor._
import com.lascala.http._
import com.lascala.http.HttpResponse._
import com.lascala.http.HttpDate._
import com.lascala.http.HttpConstants._
import akka.util.ByteString
import java.io.File
import com.lascala.libs.Enumerator
import org.apache.tika.Tika
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Sample Demo Application
 */ 
object Main extends App {
  val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8080
  val requestHandler = ActorSystem().actorOf(Props[RequestHandler])
  new HttpServer(requestHandler, port)
}

class RequestHandler extends Actor {
  val docroot = "."

  def matchETag(headerValue: String, etag: String) =
    headerValue == "*" || headerValue == etag

  def matchETag(headerValue: String, file: File): Boolean =
    matchETag(headerValue, computeETag(file))

  // With If-Modified-Since, If-Match or If-None-Match header in request, check
  // that this file is different with what the client has, so the server should
  // resend this.
  def isModified(file: File, headers: Headers) =
    headers.get("if-modified-since") match {
      case Some(Header(_, value)) =>
        HttpDate(value).asDate.compareTo(HttpDate(file.lastModified).asDate) match {
          case -1 => false
          case 0 => headers.get("if-match") match {
            case Some(Header(_, value)) => matchETag(value, file)
            case _ => headers.get("if-none-match") match {
              case Some(Header(_, value)) => !matchETag(value, file)
              case _ => false
            }
          }
          case _ => true
        }
      case _ => true
    }

  def receive = {
    case HttpRequest("GET", List("chunked"), _, _, headers, _) => 
      sender !  OKResponse.stream(getEnumerator)
    case HttpRequest("GET", List("download"), _, _, headers, _) =>
      sender !  OKResponse.stream(getEnumerator).withMimeType("text/plain")
    case HttpRequest("GET", pathSegments, _, _, headers, _) => 
      new File(docroot, "/" + pathSegments.mkString(File.separator)) match {
        case file if file.isFile() =>
          if (isModified(file, headers)) sender ! OKResponse.fromFile(file)
          else sender ! NotModifiedResponse()
        case _ => sender ! NotFoundError()
      }
    case _ => sender ! MethodNotAllowedError()
  }

  private def getEnumerator = {
    // chunked encoding 을 사용하여 사이즈가 큰 파일을 스트리밍으로 처리하기 받기.
    val file1 = new File(docroot + "/src/main/resource", "stream_data1.txt")
    val file2 = new File(docroot + "/src/main/resource", "stream_data2.txt")
    val enum1 = Enumerator.fromFile(file1)
    val enum2 = Enumerator.fromFile(file2) 

    // enume 을 연결시켜서 여러가지 input source 를 한 스트림으로 순서대로 처리하기.
    enum1 andThen enum2  
  }
}
