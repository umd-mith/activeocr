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

import java.net.URI
import javax.media.jai._
import scala.util.control.Breaks._
import scala.xml.MetaData
import scala.xml.pull._

class HocrReader { 
  def parsePage(reader: XMLEventReader, facsimileUri: URI): Seq[Page] = {
    var pages = Seq[Page]()
    val image = JAI.create(
      "fileload", new java.io.File(facsimileUri).getPath
    )
    while (reader.hasNext) {
      reader.next match {
        case EvElemStart(_, "div", attrs, _) =>
          val clss = attrs.asAttrMap.getOrElse("class", "")
          if (clss == "ocr_page") {
            val page = makeNewPage(
              reader, attrs, facsimileUri, image.getWidth, image.getHeight
            )
            pages = pages :+ page
          }
          else assert(false, "Unexpected <div>.")
        case EvElemStart(_, "title", _, _) => eatTitle(reader)
        case EvElemStart(_, "body"|"head"|"html"|"meta", _, _) => ()
        case EvElemEnd(_, "body"|"head"|"html"|"meta") => ()
        case EvText(text) => assume(text.trim.isEmpty)
        case _: EvComment => ()
        case _ => assert(false, "Unexpected XML event.")
      }
    }
    pages
  }

  // Intended to be overridden.
  def makeNewPage(reader: XMLEventReader, attributes: MetaData,
      uri: URI, imageW: Int, imageH: Int): Page = {
    var page = new Page(IndexedSeq[Zone](), uri.toString, imageW, imageH)
    page
  }

  // Don't need this after all right now, but it's potentially useful (TB).
  private def eatElement(reader: XMLEventReader) {
    var depth = 1
    while (reader.hasNext && depth > 0) {
      reader.next match {
        case _: EvElemStart => depth + 1
        case _: EvElemEnd => depth - 1
      }
    }
  }

  def eatTitle(reader: XMLEventReader) = {
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemEnd(_, "title") => break
          case EvText(text) => ()
        }
      }
    }
  }

  def unpackDimensions(title: String): (Int, Int, Int, Int) = {
    val Re = ".*bbox (\\d+) (\\d+) (\\d+) (\\d+).*".r
    val Re(x0, y0, x1, y1) = title
    val x = x0.toInt; val y = y0.toInt
    val w = x1.toInt - x0.toInt
    val h = y1.toInt - y0.toInt
    (x, y, w, h)
  }
}

