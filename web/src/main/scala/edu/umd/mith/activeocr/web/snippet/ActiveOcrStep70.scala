/*
 * #%L
 * Active OCR Web Application
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
package edu.umd.mith.activeocr.web {
package snippet {

import edu.umd.mith.activeocr.util.model.{Bbox, OcroReader, TermLine}
import java.io.File
import javax.imageio.ImageIO
import net.liftweb.http.{S, SessionVar, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVar70 extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])

class ActiveOcrStep70 extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi07.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val pages = OcroReader.parsePage(reader)

  val img = ImageIO.read(new File("../data/luxmundi.png"))

  val bboxNumber = (S.param("bbox") map { _.toInt } openOr(0))
  // enough information to declare and initialize first, prev
  val firstBbox = "/activeocr70?bbox=0"
  val prevBbox = "/activeocr70?bbox=" + (if (bboxNumber > 0) bboxNumber - 1 else 0).toString
  // not enough information to initialize last, next
  var lastBbox = ""
  var nextBbox = ""
  var ocrText = ""
  var thisCount = 0
  if (nodesVar70.is.isEmpty) {
    nodesVar70(pages.head.bbList)
  }
  val nodes = nodesVar70.is
  val lastBboxNumber = nodes.length - 1
  for (page <- pages) {
    // enough information to initialize last, next
    lastBbox = "/activeocr70?bbox=" + (nodes.length - 1).toString
    nextBbox = "/activeocr70?bbox=" + (if (bboxNumber < lastBboxNumber) bboxNumber + 1 else lastBboxNumber).toString
    thisCount = if (bboxNumber < 0) 0 else if (bboxNumber > lastBboxNumber) lastBboxNumber else bboxNumber
    nodes(thisCount) match {
      case l@TermLine(s, x, y, w, h) =>
        if ((w > 0) && (h > 0)) {
          ocrText = l.s
          var tmpImg = crop(img, x, y, w, h)
          ImageIO.write(tmpImg, "png", new File("./src/main/webapp/images/tmp.png"))
        }
      case _ => () // do nothing
    }
  }

  def dispatch = {
    case "renderTop" => renderTop
    case "renderBottom" => renderBottom
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVar70.is
    val updatedNode = nodes(i) match {
      case l: TermLine => l.copy(s = correction)
    }
    nodesVar70(nodes.updated(i, updatedNode))
  }

  def renderTop(in: NodeSeq): NodeSeq = {
    bind ("prefix", in,
      "firstBbox" -> <a href={firstBbox}>&lt;&lt; First Bbox</a>,
      "prevBbox" -> <a href={prevBbox}>&lt; Previous Bbox</a>,
      "nextBbox" -> <a href={nextBbox}>Next Bbox &gt;</a>,
      "lastBbox" -> <a href={lastBbox}>Last Bbox &gt;&gt;</a>,
      "ocrText" -> ocrText,
      "correction" -> SHtml.text(ocrText, { s: String => ocrText = s }, "size" -> "80"),
      "perform" -> SHtml.submit("Update", () => perform(ocrText)),
      "outputNodes" -> SHtml.submit("Output", () => outputNodes())
    )
  }

  def renderBottom(in: NodeSeq): NodeSeq = {
    <div>
      <svg version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        width="100%" height="100%"
        viewBox="0 0 680 1149">
        <image xlink:href="http://localhost:8080/static/images/luxmundi.png"
          width="680" height="1149"/>
        { nodes(this.thisCount).toSVG }
      </svg>
    </div>
  }

  def perform(correction: String): Unit = {
    updateAt(this.bboxNumber, correction)
  }

  def outputNodes(): Unit = {
    val nodes = nodesVar70.is
    var index = 1
    var outputFile = "/dev/null"
    var outputPrinter = new java.io.PrintWriter(outputFile)
    for (node <- nodes) {
      node match {
        case l@TermLine(s, _, _, _, _) => {
          outputFile = "./temp/0001/0100" + f"$index%02x" + ".gt.txt"
          index = index + 1
          outputPrinter = new java.io.PrintWriter(outputFile)
          outputPrinter.println(s)
          outputPrinter.close()
        }
        case _ => assert(false, "Unexpected Bbox type.")
      }
    }
  }
}

}
}
