package com.lascala.libs

/**
 * Content-coding tokens
 *
 *  $ - compress: UNIX "compress" program method
 *  $ - deflate: Despite its name the zlib compression (RFC 1950) should be used (in combination with the deflate 
 *                compression (RFC 1951)) as described in the RFC 2616. The implementation in the real world however 
 *                seems to vary between the zlib compression and the (raw) deflate compression.[4][5] Due to this 
 *                confusion, gzip has positioned itself as the more reliable default method (March 2011).
 *  $ - gzip: GNU zip format (described in RFC 1952). This method is the most broadly supported as of March 2011.[6]
 *  $ - bzip2: Free and open source lossless data compression algorithm
 */
trait SupportedCompression 
object SupportedCompression {
  case object GZip extends SupportedCompression
  case object Compress extends SupportedCompression
  case object Deflate extends SupportedCompression
  case object BZip2 extends SupportedCompression

  // Note the order is signicat here. Always favor GZip over other compression and so on.
  val all = List(GZip, Deflate, Compress, BZip2)
}
