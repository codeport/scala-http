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

/**
 * iteratee: http://www.haskell.org/haskellwiki/Iteratee_I/O
 * 파서
 */
object HttpIteratees {
  import HttpConstants._
	
  def readRequest = {
    for {
      requestLine <- readRequestLine
      (meth, (path, query), httpver) = requestLine
      headers <- readHeaders
      body <- readBody(headers)
    } yield HttpRequest(meth, path, query, httpver, headers, body)
	}
	
	def ascii(bytes: ByteString): String = bytes.decodeString("US-ASCII").trim
	
	def readRequestLine = {
		for {
			meth <- IO takeUntil SP
			uri <- readRequestURI
			_ <- IO takeUntil SP // ignore the rest
			httpver <- IO takeUntil CRLF
		} yield (ascii(meth), uri, ascii(httpver))
	}
			
	def readRequestURI = {
		IO peek 1 flatMap {
			case PATH =>
				for {
					path <- readPath
					query <- readQuery
				} yield (path, query)
					case _ => sys.error("Not Implemented")
		}
	}
	
	def readPath = {
		def step(segments: List[String]): IO.Iteratee[List[String]] = IO peek 1 flatMap {
			case PATH => IO drop 1 flatMap (_ => readUriPart(pathchar) flatMap (segment => step(segment :: segments)))
			case _ => segments match {
				case "" :: rest => IO Done rest.reverse
				case _          => IO Done segments.reverse
			}
		}
		step(Nil)
	}
		
	def readQuery: IO.Iteratee[Option[String]] = {
		IO peek 1 flatMap {
			case QUERY => IO drop 1 flatMap (_ => readUriPart(querychar) map (Some(_)))
			case _     => IO Done None
		}
	}
	
	val alpha = Set.empty ++ ('a' to 'z') ++ ('A' to 'Z') map (_.toByte)
	val digit = Set.empty ++ ('0' to '9') map (_.toByte)
	val hexdigit = digit ++ (Set.empty ++ ('a' to 'f') ++ ('A' to 'F') map (_.toByte))
	val subdelim = Set('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=') map (_.toByte)
	val pathchar = alpha ++ digit ++ subdelim ++ (Set(':', '@') map (_.toByte))
	val querychar = pathchar ++ (Set('/', '?') map (_.toByte))
	
	def readUriPart(allowed: Set[Byte]): IO.Iteratee[String] = {
		for {
			str <- IO takeWhile allowed map ascii
			pchar <- IO peek 1 map (_ == PERCENT)
			all <- if (pchar) readPChar flatMap (ch => readUriPart(allowed) map (str + ch + _)) else IO Done str
		} yield all
	}
	
	def readPChar = { 
		IO take 3 map {
			case Seq('%', rest @ _*) if rest forall hexdigit =>
				java.lang.Integer.parseInt(rest map (_.toChar) mkString, 16).toChar
		}
	}
	
	def readHeaders = {
		def step(found: List[Header]): IO.Iteratee[List[Header]] = {
			IO peek 2 flatMap {
				case CRLF => IO takeUntil CRLF flatMap (_ => IO Done found)
				case _    => readHeader flatMap (header => step(header :: found))
			}
		}
		step(Nil)
	}
	
	def readHeader = {
		for { 
			name <- IO takeUntil COLON
			value <- IO takeUntil CRLF flatMap readMultiLineValue
		} yield Header(ascii(name), ascii(value))
	}
	
	def readMultiLineValue(initial: ByteString): IO.Iteratee[ByteString] = IO peek 1 flatMap {
		case SP => IO takeUntil CRLF flatMap (bytes => readMultiLineValue(initial ++ bytes))
		case _  => IO Done initial
	}
	
	def readBody(headers: List[Header]) = {
		if (headers.exists(header => header.name == "Content-Length" || header.name == "Transfer-Encoding")) IO.takeAll map (Some(_))
		else IO Done None	
	}
}

