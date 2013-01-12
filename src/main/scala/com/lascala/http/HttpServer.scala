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
import java.net.InetSocketAddress
import util.{Success, Failure}

object HttpServer {
  import HttpIteratees._
  import akka.pattern.ask
  import akka.util.Timeout
  import concurrent.ExecutionContext.Implicits.global

  def processRequest(socket: IO.SocketHandle, handler: ActorRef): IO.Iteratee[Unit] = {
    def processResponse(response: HttpResponse) {
      socket write HttpResponse.bytes(response).compact
      if (!response.shouldKeepAlive) socket.close()
    }

    // Never timeout for now. We need to make this configurate in the future
    implicit val timeout = new Timeout(20000)

    IO repeat {
      for {
        request <- readRequest
      } yield {
        // Future 를 이용해서 asynchronously receive and handle response
        val future = handler ? request
        future.mapTo[HttpResponse].onComplete {
          case Success(response: HttpResponse) => processResponse(response)
          case Failure(error)                  => processResponse(InternalServerError())
        }
      }
    }	
	}
}

class HttpServer(handler: ActorRef, port: Int) {

  private class HttpServerActor extends Actor {

    val state = IO.IterateeRef.Map.async[IO.Handle]()(context.dispatcher)

    override def preStart {
      IOManager(context.system) listen new InetSocketAddress(port)
    }

    def receive = {
      case IO.NewClient(server) => {
        val socket = server.accept()
        state(socket) flatMap (_ => HttpServer.processRequest(socket, handler))
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

  ActorSystem().actorOf(Props(new HttpServerActor))
}
