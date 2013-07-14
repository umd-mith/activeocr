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

import edu.umd.mith.activeocr.util.model.OcroReader
import java.io.File
import net.liftweb.http.{S,SessionVar}
import net.liftweb.util.Helpers._
import scala.io.Source
import scala.xml.NodeSeq
import scala.xml.pull.XMLEventReader

class ActiveOcrStep7osb {
  val hocrFileName = "../data/luxmundi07multipage.html"
  val source = Source.fromFile(hocrFileName)
  val reader = new XMLEventReader(source)
  val imageFileName = "../data/luxmundi.png"
  val pages = OcroReader.parsePage(reader, new File(imageFileName).toURI)
  val pageNumber = (S.param("pageno") map { _.toInt } openOr(0))

  def transform(in: NodeSeq): NodeSeq = {
    <div>
      {pages(pageNumber).toSVG}
    </div>
  }
}

}
}
