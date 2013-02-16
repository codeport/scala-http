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
package com.lascala.libs

import akka.util.ByteString
import concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global
import java.io.InputStream
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit


/**
 * Chunked encoding 을 서포트 하기위해 기본적인 Enumerator(or Producer) 패턴 을 구현한것이다.
 * future 가 각각 처리되어야 할 데이타 를 담당한다.
 * Foreach 메소드 가 이 Enumerator 와 연결되어있는 다른 enumerator 를 연동해
 * 각 Enumerator 의 future 를 호출해서 각각 chunk 데이타를 처리한다.
 *
 */
trait Enumerator[T] {
  parent =>

  /**
   * 스칼라 Future 를 사용해서 인풋을 처리함.
   *
   * @note Some 은 계속해서 처리할 인풋이 있다는 뜻이고 None 은 그 반대임.
   */
  def future: Future[Option[T]]

 /**
  * 각각 Enumerator 의 퓨처를 호출한후 리턴된 값을 주어진 콜백 function 에
  * 적용한다. 퓨처는 None 을 리턴하기까지 귀재 방식으로 계속해서 호출됨.
  *
  * @param f callbakc function 을 사용하요 퓨처가 리턴한 값을 처리함.
  */
 @scala.annotation.tailrec
  final def foreach[E](f: T => E) {
    Await.result(future, Duration.apply(60, TimeUnit.SECONDS)) match {
      case Some(v) => {
        f(v)
        foreach(f)
      }
      case None => if (next.isDefined) next.get.foreach(f)
    }
  }

  def next: Option[Enumerator[T]] = None

  /**
   * 여러 Enumerator 들을 연결시켜 주는 메소드
   *
   * @param other
   * @return
   */
  def andThen(other: Enumerator[T]) = {
    def _chainOtherEnumerators(current: Option[Enumerator[T]], other: Enumerator[T]): Option[Enumerator[T]] = current match {
      case Some(curr) => Some(new Enumerator[T] {
        def future = curr.future
        override def next = _chainOtherEnumerators(curr.next, other)
      })
      case None => Some(other)
    }

    new Enumerator[T] {
      def future: Future[Option[T]] = parent.future
      override def next = _chainOtherEnumerators(parent.next, other)
    }
  }
}

/**
 * Enumerator companion object.
 */
object Enumerator {

  def fromCallback(callback: () => Option[ByteString]) = new Enumerator[ByteString] {
    def future = Future(callback())
  }

  /**
   * Takes InputStream as input source and returns a Enumerator that produces data.
   * InputStream 을 받아서 Enumerator 를 리턴해주는 유틸펑션.
   *
   * @param in
   * @param chunkSize
   */
  def fromStream(in: InputStream, chunkSize: Int = 1024 * 8) = {
    def callback = { () =>
      val buffer = new Array[Byte](chunkSize)
      val chunk = in.read(buffer) match {
        case -1 =>
          in.close()
          None
        case read =>
          val input = new Array[Byte](read)
          System.arraycopy(buffer, 0, input, 0, read)
          Some(ByteString(input))
      }

      chunk
    }

    fromCallback(callback)
  }

  /**
   * Convinient function that takes a file and returns a Enumerator
   *
   * 파일 오브젝트를 받아서 Enumerator 를 리턴해주는 유틸펑션.
   *
   * @param file
   * @param chunkSize
   */
  def fromFile(file: java.io.File, chunkSize: Int = 1024 * 8) = fromStream(new java.io.FileInputStream(file), chunkSize)
}
