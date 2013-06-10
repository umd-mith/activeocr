/*
 * #%L
 * Active OCR Utilities
 * %%
 * Copyright (C) 2011 - 2013 University of Maryland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.umd.mith.activeocr.util.model

import scala.io._
import scala.xml.pull._

object TestListify07 {
  def main(args: Array[String]): Unit = {
    val filename = "/luxmundi07.html"
    val source = Source.fromInputStream(getClass.getResourceAsStream(filename))
    val reader = new XMLEventReader(source)
    val pages = OcroReader.parsePage(reader, this.getClass.getResource("/luxmundi.png").toURI)
    var index = 0
    var output = ""
    var printer = new java.io.PrintWriter("/dev/null")
    for (page <- pages) {
      val nodes = page.bbList
      for (node <- nodes) {
        node match {
          case l@TermLine(s, x, y, w, h) => {
            output = "tmp" + f"$index%03d" + ".txt"
            index = index + 1
            printer = new java.io.PrintWriter(output)
            printer.println(s)
            printer.close
          }
          case _ => assert(false, "Unexpected Bbox type.")
        }
      }
    }
  }
}

