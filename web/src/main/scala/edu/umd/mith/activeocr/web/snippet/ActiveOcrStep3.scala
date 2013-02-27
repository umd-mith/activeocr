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

object nodesVar extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])

class ActiveOcrStep3 extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi302.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.jpeg"
  val pages = TessReader.parsePage(reader, new File(imageFileName).toURI)
  val img = ImageIO.read(new File(imageFileName))
  val count = (S.param("count") map { _.toInt } openOr(0))
  // enough information to declare and initialize first, prev
  val firstString = "/activeocr3?count=0"
  val prevCount = if (count > 0) count - 1 else 0
  val prevString = "activeocr3?count=" + prevCount.toString
  // not enough information to initialize last, next
  var lastString = ""
  var nextCount = 0
  var nextString = ""
  var ocrText = ""
  if (nodesVar.is.isEmpty) {
    nodesVar(pages.head.bbList)
  }
  val nodes = nodesVar.is
  for (page <- pages) {
    // enough information to initialize last, next
    val lastCount = nodes.length - 1
    lastString = "activeocr3?count=" + lastCount.toString
    nextCount = if (count < lastCount) count + 1 else lastCount
    nextString = "activeocr3?count=" + nextCount.toString
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
    case "render" => render
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVar.is
    val updatedNode = nodes(i) match {
      case t: TermWord => t.copy(s = correction)
      case g: Glyph => g.copy(c = correction)
    }
    nodesVar(nodes.updated(i, updatedNode))
  }

  def render(in: NodeSeq): NodeSeq = {
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

  def perform(correction: String): Unit = {
    updateAt(this.count, correction)
  }
}

}
}
