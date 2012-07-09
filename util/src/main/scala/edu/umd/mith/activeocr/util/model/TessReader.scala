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
import scala.util.control.Breaks._
import scala.xml.MetaData
import scala.xml.pull._

object TessReader {
  def main(args: Array[String]) {
    val filename = "/luxmundi3.html"
    val source = Source.fromInputStream(
      getClass.getResourceAsStream(filename)
    )
    val reader = new XMLEventReader(source)
    while (reader.hasNext) {
      val event = reader.next
      event match {
      // reader.next match {
        case EvElemStart(_, "div", attrs, _) =>
          val clss = attrs.asAttrMap.getOrElse("class", "")
          if (clss == "ocr_page") {
            val page = makeNewPage(
              reader, attrs, "../data/luxmundi.jpeg", 680, 1149
            )
            println(page)
          }
          else assert(false, "Unexpected <div>.")
        case EvElemStart(_, "title", attrs, _) => eatTitle(reader)
        case EvElemStart(_, "body"|"head"|"html"|"meta", attrs, _) => ()
        case EvElemEnd(_, "body"|"head"|"html"|"meta") => ()
        case EvText(text) => assume(text.trim.isEmpty)
        case _ => assert(false, "Unexpected XML event.")
      }
    }
  }

  def makeNewPage(reader: XMLEventReader, attributes: MetaData, uri: String, imageW: Int, imageH: Int): Page = {
    var page = new Page(IndexedSeq[Zone](), uri, imageW, imageH)
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
        // reader.next match {
          case EvElemStart(_, "div", attrs, _) => {
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_carea") {
              page = page.addChild(makeNewZone(reader, attrs))
            }
          }
          case EvElemStart(_, label, attrs, _) => ()
          case EvElemEnd(_, label) => break
          case EvText(text) => assume(text.trim.isEmpty)
        }
      }
    }
    page
  }

  def makeNewZone(reader: XMLEventReader, attributes: MetaData): Zone = {
    var zone = new Zone(IndexedSeq[Line]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
        // reader.next match
          case EvElemEnd(_, label) => ()
          case EvElemStart(_, "span", attrs, _) =>
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_line") {
              zone = zone.addChild(makeNewLine(reader, attrs))
            }
          case EvElemStart(_, label, attrs, _) => ()
          case EvText(text) => assume(text.trim.isEmpty)
        }
      }
    }
    zone
  }

  def makeNewLine(reader: XMLEventReader, attributes: MetaData): Line = {
    var line = new ContLine(IndexedSeq[Word]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
        // reader.next match
          case EvElemEnd(_, label) => ()
          case EvElemStart(_, "span", attrs, _) =>
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_word") {
              line = line.addChild(makeNewWord(reader, attrs))
            }
          case EvElemStart(_, label, attrs, _) => ()
          case EvEntityRef(_) => ()
          case EvText(text) => println(text)
        }
      }
    }
    line
  }

  def makeNewWord(reader: XMLEventReader, attributes: MetaData): Word = {
    var word = new TermWord("", 0, 0, 0, 0)
    word
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
}
