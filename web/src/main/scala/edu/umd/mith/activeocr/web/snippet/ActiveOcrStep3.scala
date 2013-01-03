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

import _root_.scala.xml.{NodeSeq, Text}
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import edu.umd.mith.activeocr.web.lib._
import Helpers._

import java.io.File
import javax.imageio.ImageIO
import org.imgscalr.Scalr._

import scala.io.Source
import scala.xml.pull.XMLEventReader
import edu.umd.mith.activeocr.util.model._

class ActiveOcrStep3 extends StatefulSnippet {
  val hocrFileName = "../data/luxmundi302.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.jpeg"
  val pages = TessReader.parsePage(reader, new File(imageFileName).toURI)
  val img = ImageIO.read(new File(imageFileName))
  val count = S.param("count").openOr("0").toInt
  val firstString = "/activeocr3?count=0"
  var lastString = ""
  if (count < 0) S.redirectTo(firstString)
  val nextCount = (count + 1).toString
  val nextString = "activeocr3?count=" + nextCount
  val prevCount = (count - 1).toString
  val prevString = "activeocr3?count=" + prevCount
  for (page <- pages) {
    val nodes = page.bbList
    lastString = "activeocr3?count=" + (nodes.length - 1)
    nodes(count) match {
      case t@TermWord(s, x, y, w, h) =>
        if ((w > 0) && (h > 0)) {
          var tmpImg = crop(img, x, y, w, h)
          ImageIO.write(tmpImg, "jpeg", new File("./src/main/webapp/images/tmp.jpeg"))
        }
      case g@Glyph(c, x, y, w, h) =>
        if ((w > 0) && (h > 0)) {
          var tmpImg = crop(img, x, y, w, h)
          ImageIO.write(tmpImg, "jpeg", new File("./src/main/webapp/images/tmp.jpeg"))
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
    <td><img src="images/tmp.jpeg"/></td>
    <td><a href={nextString}>Next &gt;</a></td>
    <td><a href={lastString}>Last &gt;&gt;</a></td>
    </tr>
    </table>
  }
}

}
}
