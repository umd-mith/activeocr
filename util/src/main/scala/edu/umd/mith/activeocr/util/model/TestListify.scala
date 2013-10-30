/*
 * #%L
 * Active OCR Utilities
 * %%
 * Copyright (C) 2011 - 2012 Maryland Institute for Technology in the Humanities
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

object TestListify {
  def main(args: Array[String]): Unit = {
    val filename = "/luxmundi302.html"
    val source = Source.fromInputStream(getClass.getResourceAsStream(filename))
    val reader = new XMLEventReader(source)
    val pages = TessReader.parsePage(reader, this.getClass.getResource("/luxmundi.jpeg").toURI)
    for (page <- pages) {
      val nodes = page.bbList
      for (node <- nodes) {
        node match {
          case t@TermWord(s, x, y, w, h) => println(s + " " + x + " " + t)
          case g@Glyph(c, x, y, w, h) => println(c + " " + x + " " + g)
          case _ => () // do nothing
        }
      }
    }
  }
}

