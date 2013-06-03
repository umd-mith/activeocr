/*
 * #%L
 * Active OCR Web Application
 * %%
 * Copyright (C) 2011 - 2013 Maryland Institute for Technology in the Humanities
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

import edu.umd.mith.activeocr.util.model.{OcroReader, TermLine}
import java.io.File
import javax.imageio.ImageIO
import net.liftweb.http.{S, StatefulSnippet}
import org.imgscalr.Scalr.crop
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

class ActiveOcrStep7 extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi07.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.png"
  val img = ImageIO.read(new File(imageFileName))
  val pages = OcroReader.parsePage(reader, new File(imageFileName).toURI)
  val count = (S.param("count") map { _.toInt } openOr(0))
  // enough information to declare and initialize first, prev
  val firstString = "/activeocr7?count=0" // WHY '/'?
  val prevCount = if (count > 0) count - 1 else 0
  val prevString = "activeocr7?count=" + prevCount.toString // WHY NO '/'?
  // not enough information to initialize last, next
  var lastString = ""
  var nextCount = 0
  var nextString = ""
  var ocrText = ""
  for (page <- pages) {
    val nodes = page.bbList
    // enough information to initialize last, next
    val lastCount = nodes.length - 1
    lastString = "activeocr7?count=" + lastCount.toString
    nextCount = if (count < lastCount) count + 1 else lastCount
    nextString = "activeocr7?count=" + nextCount.toString
    val thisCount = if (count < 0) 0 else if (count > lastCount) lastCount else count
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
    case "transform" => xform
  }

  def xform(in: NodeSeq): NodeSeq = {
    <table>
    <tr>
    <td><a href={firstString}>&lt;&lt; First</a></td>
    <td><a href={prevString}>&lt; Prev</a></td>
    <td><a href={nextString}>Next &gt;</a></td>
    <td><a href={lastString}>Last &gt;&gt;</a></td>
    </tr>
    <tr>
    <td colspan="4"><img src="images/tmp.png"/></td>
    </tr>
    <tr>
    <td colspan="4">{ocrText}</td>
    </tr>
    </table>
  }
}

}
}
