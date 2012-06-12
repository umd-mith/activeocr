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

object HocrReader {
  def main(args: Array[String]) {
    val source = Source.fromInputStream(
      getClass.getResourceAsStream("/luxmundi.html")
    )
    val reader = new XMLEventReader(source)
    while (reader.hasNext) {
      val event = reader.next
      event match {
        case EvElemStart(_, label, attrs, _) => {
          // Labels with no attributes:
          // body, em, head, html, strong, title
          if (label == "meta") { }
          // p: ocr_par
          if (label == "p") { } 
          // div: ocr_carea, ocr_page
          // span: ocr_line, ocr_word, ocrx_word
          if (label == "div" || label == "span") {
            doStuffWithAttributes(attrs.toString)
          }
        }
        case EvElemEnd(_, label) => { }
        case EvText(text) => { }
      }
    }
    source.close
  }

  def doStuffWithAttributes(attributes: String) {
    val Re = " title=\"(.+)\" id=\"(\\w+)\" class=\"(\\w+)\"".r
    val Re(ocrTitle, ocrId, ocrClass) = attributes
    if (ocrClass != "ocrx_word") {
      val Re = ".*bbox (\\d+) (\\d+) (\\d+) (\\d+)".r
      val Re(x0, y0, x1, y1) = ocrTitle
      val x = x0.toInt; val y = y0.toInt
      val w = x1.toInt - x0.toInt
      val h = y1.toInt - y0.toInt
      println("x = " + x + ", y = " + y + ", w = " + w + ", h = " + h)
    }
  }
}
