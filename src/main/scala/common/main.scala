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
import akka.util.ByteString

/**
 * Sample Demo Application
 */ 
object Main extends App {
  val port = Option(System.getenv("PORT")) map (_.toInt) getOrElse 8080
  val requestHandler = ActorSystem().actorOf(Props[RequestHandler])
  new HttpServer(requestHandler, port)
}

class RequestHandler extends Actor {
  def receive = {
    case HttpRequest("GET", "ping" :: Nil, _, _, headers, _) => {
      sender ! OKResponse(
        ByteString("<p>pong</p>"),
        headers.exists { case Header(n, v) => n.toLowerCase == "connection" && v.toLowerCase == "keep-alive" })
    }
    case req: HttpRequest => {
      sender ! OKResponse(
        ByteString("<p>" + req.toString + "</p>"),
        req.headers.exists { case Header(n, v) => n.toLowerCase == "connection" && v.toLowerCase == "keep-alive" })
    }
  }
}


