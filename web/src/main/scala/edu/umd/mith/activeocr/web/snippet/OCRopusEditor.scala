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
package edu.umd.mith.activeocr.web.snippet

import edu.umd.mith.activeocr.util.model.{Bbox, LocalHostOcroReader, TermLine}
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import net.liftweb.http.{S, SessionVar, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVarOcroEdit extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])
object pageVarOcroEdit extends SessionVar[Int](0)

class OCRopusEditor extends StatefulSnippet {
  val source = Source.fromFile("../data/luxmundi07multipage.html")
  val reader = new XMLEventReader(source)
  val pages = LocalHostOcroReader.parsePage(reader)
  val lastPageNumber = pages.length -1
  val pageNumber = S.param("page").map(_.toInt).map {
    case tmp if tmp < 0 => 0
    case tmp if tmp > lastPageNumber => lastPageNumber
    case tmp => tmp
  }.openOr(0)
  val pageString = "/ocroedit?page="
  val firstPage = pageString + 0.toString
  val prevPage = pageString + (if (pageNumber > 0) pageNumber - 1 else 0).toString
  val nextPage = pageString + (if (pageNumber < lastPageNumber) pageNumber + 1 else lastPageNumber).toString
  val lastPage = pageString + lastPageNumber.toString
  val oldPageNumber = pageVarOcroEdit.is
  if (nodesVarOcroEdit.is.isEmpty || pageNumber != oldPageNumber) {
    nodesVarOcroEdit(pages(pageNumber).bbList)
  }
  pageVarOcroEdit(pageNumber)
  val nodes = nodesVarOcroEdit.is
  val lastBboxNumber = nodes.length - 1
  val bboxNumber = S.param("bbox").map(_.toInt).map {
    case tmp if tmp < 0 => 0
    case tmp if tmp > lastBboxNumber => lastBboxNumber
    case tmp => tmp
  }.openOr(0)
  val queryString = pageString + pageNumber.toString + "&bbox="
  val firstBbox = queryString + 0.toString
  val prevBbox = queryString + (if (bboxNumber > 0) bboxNumber - 1 else 0).toString
  val nextBbox = queryString + (if (bboxNumber < lastBboxNumber) bboxNumber + 1 else lastBboxNumber).toString
  val lastBbox = queryString + lastBboxNumber.toString
  val img = ImageIO.read(new URL(pages(pageNumber).uri))
  var ocrText = nodes(bboxNumber) match {
    case TermLine(s, x, y, w, h) if ((w > 0) && (h > 0)) =>
      val tmpImg = crop(img, x, y, w, h)
      ImageIO.write(tmpImg, "png", new File("./src/main/webapp/images/tmp.png"))
      s
    case _ => sys.error("Unexpected Bbox type.")
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
        viewBox={ "0 0 " + pages(pageNumber).imageW.toString + " " + pages(pageNumber).imageH.toString }>
        <image xlink:href={ pages(pageNumber).uri }
          width={ pages(pageNumber).imageW.toString } height={ pages(pageNumber).imageH.toString }/>
        { nodes(bboxNumber).toSVG }
      </svg>
    </div>
  }

  def perform(correction: String): Unit = {
    updateAt(bboxNumber, correction)
  }

  def updateAt(i: Int, correction: String) = {
    val nodes = nodesVarOcroEdit.is
    val updatedNode = nodes(i) match {
      case l: TermLine => l.copy(s = correction)
    }
    nodesVarOcroEdit(nodes.updated(i, updatedNode))
  }

  def outputNodes(): Unit = nodesVarOcroEdit.is.zipWithIndex.foreach {
    case (TermLine(s, _, _, _, _), index) =>
      val dirNumber = pageNumber + 1
      val outputFile = f"./temp/$dirNumber%04d/0100${index + 1}%02x.gt.txt"
      val outputPrinter = new java.io.PrintWriter(outputFile)
      outputPrinter.println(s)
      outputPrinter.close()
    case _ => sys.error("Unexpected Bbox type.")
  }
}

