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

import java.io.File
import java.net.URI
import javax.imageio.ImageIO
import scala.util.control.Breaks.{break, breakable}
import scala.util.matching.Regex
import scala.xml.MetaData
import scala.xml.pull.{EvComment, EvElemEnd, EvElemStart, EvText, XMLEventReader}

object OcroReader extends HocrReader {
  def parsePage(reader: XMLEventReader): Seq[Page] = {
    var pages = Seq[Page]()
    while (reader.hasNext) {
      reader.next match {
        case EvElemStart(_, "div", attrs, _) =>
          val clss = attrs.asAttrMap.getOrElse("class", "")
          val title = attrs.asAttrMap.getOrElse("title", "")
          val pattern = new Regex("""file (temp\/\d{4}.bin.png)""", "filename")
          val result = pattern.findFirstMatchIn(title).get
          val filename = result.group("filename")
          val facsimileUri = new URI("http://localhost:8080/static/images/" + filename)
          val imageFileName = "../web/src/main/webapp/static/images/" + filename
          val image = ImageIO.read(new File(imageFileName))
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

  override def makeNewPage(reader: XMLEventReader, attributes: MetaData,
      uri: URI, imageW: Int, imageH: Int): Page = {
    var page = new Page(IndexedSeq[Zone](), uri.toString, imageW, imageH)
    var zone = new Zone(IndexedSeq[Line]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, "br", _, _) => ()
          case EvElemStart(_, "p", _, _) => ()
          case EvElemStart(_, "span", attrs, _) =>
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_line")
              zone = zone.addChild(makeNewLine(reader, attrs, imageH))
          case EvElemEnd(_, "br") => ()
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

  def makeNewLine(reader: XMLEventReader, attributes: MetaData, imageHeight: Int): TermLine = {
    val title = attributes.asAttrMap.getOrElse("title", "")
    val (x, y, w, h) = unpackDimensions(title, imageHeight)
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

  def unpackDimensions(title: String, imageHeight: Int): (Int, Int, Int, Int) = {
    val Re = ".*bbox (\\d+) (\\d+) (\\d+) (\\d+).*".r
    val Re(x0, y0, x1, y1) = title
    val x = x0.toInt; var y = y0.toInt
    val w = x1.toInt - x0.toInt
    val h = y1.toInt - y0.toInt
    y = imageHeight - y - h
    (x, y, w, h)
  }
}

