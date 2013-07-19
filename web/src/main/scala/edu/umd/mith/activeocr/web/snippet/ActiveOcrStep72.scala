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
import java.net.URL
import javax.imageio.ImageIO
import net.liftweb.http.{S, SessionVar, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVar72 extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])
object pagesVar72 extends SessionVar[Int](0)

class ActiveOcrStep72 extends StatefulSnippet {
  val source = Source.fromFile("../data/luxmundi07multipage.html")
  val reader = new XMLEventReader(source)
  val pages = OcroReader.parsePage(reader)
  val lastPageNumber = pages.length -1
  val tmpPageNumber = (S.param("page") map { _.toInt } openOr(0))
  val pageNumber = if (tmpPageNumber < 0) 0 else if (tmpPageNumber > lastPageNumber) lastPageNumber else tmpPageNumber
  val pageString = "/activeocr72?page="
  val firstPage = pageString + 0.toString
  val prevPage = pageString + (if (pageNumber > 0) pageNumber - 1 else 0).toString
  val nextPage = pageString + (if (pageNumber < lastPageNumber) pageNumber + 1 else lastPageNumber).toString
  val lastPage = pageString + lastPageNumber.toString
  val oldPageNumber = pagesVar72.is
  if (nodesVar72.is.isEmpty || pageNumber != oldPageNumber) {
    nodesVar72(pages(pageNumber).bbList)
  }
  pagesVar72(pageNumber)
  val nodes = nodesVar72.is
  val lastBboxNumber = nodes.length - 1
  val tmpBboxNumber = (S.param("bbox") map { _.toInt } openOr(0))
  val bboxNumber = if (tmpBboxNumber < 0) 0 else if (tmpBboxNumber > lastBboxNumber) lastBboxNumber else tmpBboxNumber
  val queryString = pageString + pageNumber.toString + "&bbox="
  val firstBbox = queryString + 0.toString
  val prevBbox = queryString + (if (bboxNumber > 0) bboxNumber - 1 else 0).toString
  val nextBbox = queryString + (if (bboxNumber < lastBboxNumber) bboxNumber + 1 else lastBboxNumber).toString
  val lastBbox = queryString + lastBboxNumber.toString
  val img = ImageIO.read(new URL(pages(pageNumber).getUri))
  var ocrText = ""
  nodes(bboxNumber) match {
    case l@TermLine(s, x, y, w, h) =>
      if ((w > 0) && (h > 0)) {
        ocrText = l.s
        var tmpImg = crop(img, x, y, w, h)
        ImageIO.write(tmpImg, "png", new File("./src/main/webapp/images/tmp.png"))
      }
    case _ => ()
  }

  def dispatch = {
    case "renderTop" => renderTop
    case "renderBottom" => renderBottom
  }

  def renderTop(in: NodeSeq): NodeSeq = {
    bind ("prefix", in,
      "firstPage" -> <a href={firstPage}>&lt;&lt; First Page</a>,
      "prevPage" -> <a href={prevPage}>&lt; Previous Page</a>,
      "nextPage" -> <a href={nextPage}>Next Page &gt;</a>,
      "lastPage" -> <a href={lastPage}>Last Page &gt;&gt;</a>,
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
        <image xlink:href={ pages(pageNumber).getUri() }
          width="680" height="1149"/>
        { nodes(this.bboxNumber).toSVG }
      </svg>
    </div>
  }

  def perform(correction: String): Unit = {
    updateAt(this.bboxNumber, correction)
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVar72.is
    val updatedNode = nodes(i) match {
      case l: TermLine => l.copy(s = correction)
    }
    nodesVar72(nodes.updated(i, updatedNode))
  }

  def outputNodes(): Unit = {
    val dirNumber = pageNumber + 1
    val nodes = nodesVar72.is
    var index = 1
    var outputFile = "/dev/null"
    var outputPrinter = new java.io.PrintWriter(outputFile)
    for (node <- nodes) {
      node match {
        case l@TermLine(s, _, _, _, _) => {
          outputFile = "./temp/" + f"$dirNumber%04d" + "/0100" + f"$index%02x" + ".gt.txt"
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
