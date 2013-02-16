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

import com.lascala.http.HttpResponse
import akka.util.ByteString
import java.util.zip.{DeflaterOutputStream, GZIPOutputStream}
import java.io.{OutputStream, ByteArrayOutputStream}

/**
 * Trait for Gzip support. Mix-in to response to compress body with GZIP
 */
trait GZipSupport extends BodyCompression {
  val compressionMethod = "gzip"
  def deflateOutputStream(os: OutputStream): DeflaterOutputStream = new GZIPOutputStream(os)
}

/**
 * Trait for deflate support. Mix-in to response to compress body with deflater
 */
trait DeflateSupport extends BodyCompression {
  val compressionMethod = "deflate"
  def deflateOutputStream(os: OutputStream): DeflaterOutputStream = new DeflaterOutputStream(os)
}

/**
 * Trait that provides default implementation for compressing body 
 */
trait BodyCompression extends Compression {
  def deflateOutputStream(os: OutputStream): DeflaterOutputStream
  def compressionMethod: String

  abstract override val bodyData: ByteString = {
    val originalBody = super.bodyData.toArray
    val os = new ByteArrayOutputStream(originalBody.size)
    val compress = deflateOutputStream(os)

    compress.write(originalBody)
    compress.finish() // Need to call finish after write otherwise compression won't work.
    val compressedBody = ByteString(os.toByteArray)

    compress.close()
    os.close()

    compressedBody
  }
}

/**
 * Base trait for compression support. 
 * As of Feb 2012, only gzip and deflate is honored. 
 * If user wants to provide other compression support,
 * this trait can be extended and mixed in to a response. 
 */
trait Compression extends HttpResponse {
  def compressionMethod: String
  override def bodyData: ByteString
}

object SupportedCompression {
  val GZIP = "gzip"
  val DEFLATE = "deflate"
}
