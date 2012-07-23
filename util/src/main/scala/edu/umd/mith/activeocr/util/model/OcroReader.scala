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
import scala.util.control.Breaks._
import scala.xml.MetaData
import scala.xml.pull._

object OcroReader extends HocrReader {
  override def makeNewPage(reader: XMLEventReader, attributes: MetaData,
      uri: URI, imageW: Int, imageH: Int): Page = {
    var page = new Page(IndexedSeq[Zone](), uri.toString, imageW, imageH)
    var zone = new Zone(IndexedSeq[Line]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, "p", _, _) => ()
          case EvElemStart(_, "span", attrs, _) =>
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_line")
              zone = zone.addChild(makeNewLine(reader, attrs))
          case EvElemEnd(_, "div") => break
          case EvElemEnd(_, "p") => ()
          case EvText(text) => assume(text.trim.isEmpty)
          case _ => assert(false, "Unexpected XML event.")
        }
      }
    }
    page = page.addChild(zone)
    page
  }

  def makeNewLine(reader: XMLEventReader, attributes: MetaData): TermLine = {
    val title = attributes.asAttrMap.getOrElse("title", "")
    val (x, y, w, h) = unpackDimensions(title)
    var tmpText = ""
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvComment(_) => ()
          case EvElemEnd(_, "span") => break
          case EvText(text) => tmpText = text
        }
      }
    }
    val line = new TermLine(tmpText, x, y, w, h)
    line
  }
}

