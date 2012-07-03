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

object HocrReader {
  def main(args: Array[String]) {
    val source = Source.fromInputStream(
      getClass.getResourceAsStream("/luxmundi1.html")
    )
    val reader = new XMLEventReader(source)
    while (reader.hasNext) {
      val event = reader.next
      event match {
        case EvElemStart(_, label, attrs, _) => {
          if (label == "div") {
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_page") {
              val page = makeNewPage(
                reader, attrs, "../data/luxmundi.jpeg", 680, 1149
              )
              // println(page)
              val formatter = new scala.xml.PrettyPrinter(80, 2)
              val printer = new java.io.PrintWriter("luxmundi.svg")
              printer.println(formatter.format(page.toSVG))
              printer.close()
            }
          }
        }
        case EvElemEnd(_, label) => { }
        case EvText(text) => { assume(text.trim.isEmpty) }
      }
    }
    source.close
  }

  def makeNewPage(reader: XMLEventReader, attributes: MetaData, uri: String, imageW: Int, imageH: Int): Page = {
    val id = attributes.asAttrMap.getOrElse("id", "")
    println(id + " Start")
    var page = new Page(IndexedSeq[Zone](), uri, imageW, imageH)
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, "div", attrs, _) => {
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_carea") {
              page = page.addChild(makeNewZone(reader, attrs))
            }
          }
          case EvElemEnd(_, label) => { break }
          case EvText(text) => { assume(text.trim.isEmpty) }
        }
      }
    }
    println(id + " End")
    page
  }

  def makeNewZone(reader: XMLEventReader, attributes: MetaData): Zone = {
    val id = attributes.asAttrMap.getOrElse("id", "")
    println(id + " Start")
    var zone = new Zone(IndexedSeq[Line]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, "p", attrs, _) => { 
            println("<p> Start")
          }
          case EvElemEnd(_, "p") => {
            println("<p> End")
          }
          case EvElemStart(_, "span" , attrs, _) => {
            val clss = attrs.asAttrMap.getOrElse("class", "")
            if (clss == "ocr_line") {
              zone = zone.addChild(makeNewLine(reader, attrs))
            }
          }
          case EvElemEnd(_, "div") => {
            break
          }
          case EvText(text) => {
            assume(text.trim.isEmpty)
          }
        }
      }
    }
    println(id + " End")
    zone
  }

  def makeNewLine(reader: XMLEventReader, attributes: MetaData): Line = {
    val id = attributes.asAttrMap.getOrElse("id", "")
    println(id + " Start")
    var line = new ContLine(IndexedSeq[Word]())
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, label, attrs, _) => {
            if (label == "span") {
              val clss = attrs.asAttrMap.getOrElse("class", "")
              if (clss == "ocr_word") {
                line = line.addChild(makeNewWord(reader, attrs))
              }
            }
          }
          case EvElemEnd(_, label) => { break }
          case EvText(text) => { assume(text.trim.isEmpty) }
        }
      }
    }
    println(id + " End")
    line
  }

  def makeNewWord(reader: XMLEventReader, attributes: MetaData): Word = {
    val title = attributes.asAttrMap.getOrElse("title", "")
    val id = attributes.asAttrMap.getOrElse("id", "")
    val (x, y, w, h) = unpackDimensions(title)
    var tmpWord = ""
    println(id + " Start")
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, label, attrs, _) => {
            if (label == "span") {
              val clss = attrs.asAttrMap.getOrElse("class", "")
              if (clss == "ocrx_word") {
                tmpWord = eatXword(reader, attrs)
                // tmpWord = eatWord(reader)
                println(tmpWord)
              }
            }
          }
          case EvElemEnd(_, label) => { break }
          case EvText(text) => { assume(text.trim.isEmpty) }
        }
      }
    }
    /*
    val glyphs: IndexedSeq[Glyph] =
      tmpWord.map(c => Glyph(c.toString, x, y, w, h)) 
    val word = new ContWord(glyphs)
     */
    val word = new TermWord(tmpWord, x, y, w, h)
    println(id + " End")
    word
  }

  def eatWord(reader: XMLEventReader) = reader.takeWhile {
    case EvElemEnd(_, "span") => false
    case _ => true
  }.flatMap {
    case EvText(text) => Some(text)
    case _ => None
  }.mkString

  def eatXword(reader: XMLEventReader, attributes: MetaData): String = {
    val id = attributes.asAttrMap.getOrElse("id", "")
    println(id + " Start")
    var tmp = ""
    breakable {
      while (reader.hasNext) {
        val event = reader.next
        event match {
          case EvElemStart(_, "em", _, _) => { println("<em> Start") }
          case EvElemEnd(_, "em") => { println("<em> End") }
          case EvElemStart(_, "strong", _, _) => { println("<strong> Start") }
          case EvElemEnd(_, "strong") => { println("<strong> End") }
          case EvElemEnd(_, "span") => break
          case EvText(text) => {
            tmp = text
          }
          case _ => ()
        }
      }
    }
    println(id + " End")
    tmp
  }

  /*
  def unpackAttributes(attrs: String): (String, String, String) = {
    val Re = " title=\"(.+)\" id=\"(\\w+)\" class=\"(\\w+)\"".r
    val Re(attrTitle, attrId, attrClass) = attrs
    (attrTitle, attrId, attrClass)
  }

  def unpackAttributes(attrs: Map[String, String])
    //  : Option[(String, String, String)] = for {
      : (Option[String], Option[String], Option[String]) = for {
    title <- attrs("title")
    id <- attrs("id")
    cl <- attrs("class")
  } yield (title, id, cl)
   */

  def unpackDimensions(title: String): (Int, Int, Int, Int) = {
    val Re = ".*bbox (\\d+) (\\d+) (\\d+) (\\d+)".r
    val Re(x0, y0, x1, y1) = title
    val x = x0.toInt; val y = y0.toInt
    val w = x1.toInt - x0.toInt
    val h = y1.toInt - y0.toInt
    (x, y, w, h)
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

/*
 * The body, em, head, html, strong, and title labels do not have
 * attributes.  The meta label has 3 attributes (content, http-equiv,
 * and name) that for now I don't care about. The p label has a single
 * attribute (class="ocr_par") that adds no information.
 *
 * div: ocr_carea, ocr_page
 * span: ocr_line, ocr_word, ocrx_word
 */
