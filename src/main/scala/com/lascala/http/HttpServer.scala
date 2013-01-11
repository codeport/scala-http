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

import akka.actor._
import akka.util.ByteString
import java.net.InetSocketAddress

object HttpServer {
  import HttpIteratees._
	
  def processRequest(socket: IO.SocketHandle): IO.Iteratee[Unit] = {
    IO repeat {
      for {
        request <- readRequest
      } yield {
        val rsp = request match {
          case Request("GET", "ping" :: Nil, _, _, headers, _) => {
						OKResponse(ByteString("<p>pong</p>"), request.headers.exists { case Header(n, v) => n.toLowerCase == "connection" && v.toLowerCase == "keep-alive" })
					}
          case req => {
						OKResponse(ByteString("<p>" + req.toString + "</p>"), request.headers.exists { case Header(n, v) => n.toLowerCase == "connection" && v.toLowerCase == "keep-alive" })
					}
        }
        socket write OKResponse.bytes(rsp).compact
        if (!rsp.keepAlive) socket.close()
      }
    }	
	}
}

class HttpServer(port: Int) extends Actor {
  val state = IO.IterateeRef.Map.async[IO.Handle]()(context.dispatcher)
	
  override def preStart {
    IOManager(context.system) listen new InetSocketAddress(port)
  }
	
  def receive = {
    case IO.NewClient(server) => {
			val socket = server.accept()
			state(socket) flatMap (_ => HttpServer.processRequest(socket))
		}

    case IO.Read(socket, bytes) => {
			state(socket)(IO Chunk bytes)
		}
		
    case IO.Closed(socket, cause) => {
			state(socket)(IO EOF)
			state -= socket
		}
  }
}
