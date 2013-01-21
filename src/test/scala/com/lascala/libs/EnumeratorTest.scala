package com.lascala.libs

import org.scalatest._
import org.scalatest.matchers._
import akka.util.ByteString
import java.io.ByteArrayInputStream

class EnumeratorTest extends FlatSpec with ShouldMatchers {
  
  "Enumerator" can "take a callback function and execute future baesd on the callback" in {
    var counter = 0
    val callback = { () =>
      if(counter == 0) {
        counter += 1
        Option(ByteString("Test Data"))
      } else None
    }
    
    val enum = Enumerator.fromCallback(callback)
    enum.foreach(v => v.utf8String should equal ("Test Data"))
  }

  "Enumerator" can "take an InputStream and stream the input" in {
    var str  = "Enumerator Test"
    val in   = new ByteArrayInputStream(str.getBytes)
    val enum = Enumerator.fromStream(in, 1) // get each byte at a time
   
    enum foreach { v => 
      // the v should be the first char of the str
      str should startWith(v.utf8String) 
      // drop the first char of the str so that next returned value is the first char of the remaining str
      str = str.drop(1) 
    }
  }

  "Enumerator" can "take a file and stream the file" in {
    val file     = new java.io.File("src/main/resource", "stream_data1.txt")
    // Expected number of chunks. 1024 * 8 is the size of each chunk
    val chunkNum = (file.length() / (1024 * 8)) + 1 
    var count    = 0
    val enum     = Enumerator.fromFile(file)

    enum foreach { v =>
      // Verify that contents are read from each file
      v.utf8String.size should be > (0)
      count += 1
    }

    count should be (chunkNum)
  }

  "Enumerator" can "be chained with other enumerators" in {
    var count = 1
    def callback(num: String) = { () =>
      if(count % 2 == 1){ 
        count += 1
        Some(ByteString(s"Enum ${num} "))
      } else {
        count += 1
        None
      }
    }
    val enum1 = Enumerator.fromCallback(callback("1"))
    val enum2 = Enumerator.fromCallback(callback("2"))
    val enum3 = Enumerator.fromCallback(callback("3"))
    val enum4 = Enumerator.fromCallback(callback("4"))
    val enum  = enum1 andThen enum2 andThen enum3 andThen enum4
    var result = ""

    enum foreach { v => 
      result += v.utf8String 
    }
    result should equal ("Enum 1 Enum 2 Enum 3 Enum 4 ")
  }
}
