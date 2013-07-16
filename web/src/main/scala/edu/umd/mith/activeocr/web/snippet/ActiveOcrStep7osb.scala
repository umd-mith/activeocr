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

import edu.umd.mith.activeocr.util.model.{Bbox,OcroReader}
import java.io.File
import net.liftweb.http.{S,SessionVar,StatefulSnippet}
import net.liftweb.util.Helpers._
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

object nodesVar7osb extends SessionVar[IndexedSeq[Bbox]](IndexedSeq.empty[Bbox])
object pagesVar7osb extends SessionVar[Int](0)

class ActiveOcrStep7osb extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi07multipage.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.png"
  val pages = OcroReader.parsePage(reader, new File(imageFileName).toURI)

  val lineNumber = (S.param("line") map { _.toInt } openOr(0))
  val pageNumber = (S.param("page") map { _.toInt } openOr(0))
  val lastPageNumber = pages.length -1
  val firstPage = "/activeocr7osb?page=0"
  val prevPage = "/activeocr7osb?page=" + (if (pageNumber > 0) pageNumber - 1 else 0).toString 
  val nextPage = "/activeocr7osb?page=" + (if (pageNumber < lastPageNumber) pageNumber + 1 else lastPageNumber).toString
  val lastPage = "/activeocr7osb?page=" + lastPageNumber.toString
  val oldPageNumber = pagesVar7osb.is
  if (nodesVar7osb.is.isEmpty || pageNumber != oldPageNumber) {
    nodesVar7osb(pages(pageNumber).bbList)
  }
  pagesVar7osb(pageNumber)
  val nodes = nodesVar7osb.is

  def dispatch = {
    case "renderTop" => renderTop
    case "renderBottom" => renderBottom
  }

  def renderTop(in: NodeSeq): NodeSeq = {
    bind ("prefix", in,
      "firstPage" -> <a href={firstPage}>&lt;&lt; First Page</a>,
      "prevPage" -> <a href={prevPage}>&lt; Previous Page</a>,
      "nextPage" -> <a href={nextPage}>Next Page &gt;</a>,
      "lastPage" -> <a href={lastPage}>Last Page &gt;&gt;</a>
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
        { nodes(this.lineNumber).toSVG }
      </svg>
    </div>
  }
}

}
}
