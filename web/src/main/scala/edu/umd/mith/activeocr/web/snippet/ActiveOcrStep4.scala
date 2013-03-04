/*
 * #%L
 * Active OCR Web Application
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
package edu.umd.mith.activeocr.web {
package snippet {

import edu.umd.mith.activeocr.util.model.{Bbox, Glyph, TermWord, TessReader}
import java.io.File
import javax.imageio.ImageIO
import net.liftweb.http.{S, SHtml, SessionVar, StatefulSnippet}
import net.liftweb.util.Helpers._
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVar4 extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])

class ActiveOcrStep4 extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi302.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.jpeg"
  val pages = TessReader.parsePage(reader, new File(imageFileName).toURI)
  val img = ImageIO.read(new File(imageFileName))
  val count = (S.param("count") map { _.toInt } openOr(0))
  // enough information to declare and initialize first, prev
  val firstString = "/activeocr4?count=0"
  val prevCount = if (count > 0) count - 1 else 0
  val prevString = "activeocr4?count=" + prevCount.toString
  // not enough information to initialize last, next
  var lastString = ""
  var nextCount = 0
  var nextString = ""
  var ocrText = ""
  if (nodesVar4.is.isEmpty) {
    nodesVar4(pages.head.bbList)
  }
  val nodes = nodesVar4.is
  for (page <- pages) {
    // enough information to initialize last, next
    val lastCount = nodes.length - 1
    lastString = "activeocr4?count=" + lastCount.toString
    nextCount = if (count < lastCount) count + 1 else lastCount
    nextString = "activeocr4?count=" + nextCount.toString
    val thisCount = if (count < 0) 0 else if (count > lastCount) lastCount else count
    nodes(thisCount) match {
      case t@TermWord(s, x, y, w, h) =>
        if ((w > 0) && (h > 0)) {
          ocrText = t.s
          var tmpImg = crop(img, x, y, w, h)
          ImageIO.write(tmpImg, "jpeg", new File("./src/main/webapp/images/tmp.jpeg"))
        }
      case g@Glyph(c, x, y, w, h) =>
        if ((w > 0) && (h > 0)) {
          ocrText = g.c
          var tmpImg = crop(img, x, y, w, h)
          ImageIO.write(tmpImg, "jpeg", new File("./src/main/webapp/images/tmp.jpeg"))
        }
      case _ => () // do nothing
    }
  }

  def dispatch = {
    case "renderRight" => renderRight
    case "renderLeft" => renderLeft
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVar4.is
    val updatedNode = nodes(i) match {
      case t: TermWord => t.copy(s = correction)
      case g: Glyph => g.copy(c = correction)
    }
    nodesVar4(nodes.updated(i, updatedNode))
  }

  def renderRight(in: NodeSeq): NodeSeq = {
    bind ("prefix", in,
      "firstString" -> <a href={firstString}>&lt;&lt; First</a>,
      "prevString" -> <a href={prevString}>&lt; Previous</a>,
      "nextString" -> <a href={nextString}>Next &gt;</a>,
      "lastString" -> <a href={lastString}>Last &gt;&gt;</a>,
      "ocrText" -> ocrText,
      "correction" -> SHtml.text(ocrText, { s: String => ocrText = s }, "size" -> "3"),
      "perform" -> SHtml.submit("Submit", () => perform(ocrText))
    )
  }

  def renderLeft(in: NodeSeq): NodeSeq = {
    <div>
      <svg version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        width="100%" height="100%"
        viewBox="0 0 680 1149">
        <image xlink:href="http://localhost:8080/static/images/luxmundi.jpeg"
          width="680" height="1149"/>
        { nodes(this.count).toSVG }
      </svg>
    </div>
  }

  def perform(correction: String): Unit = {
    updateAt(this.count, correction)
  }
}

}
}
