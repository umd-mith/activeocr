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
import _root_.java.util.Date
import edu.umd.mith.activeocr.web.lib._
import Helpers._

import java.io.File
import javax.imageio.ImageIO
import org.imgscalr.Scalr._

class ActiveOCR {
  // var img = ImageIO.read(new File("../data/luxmundi.jpeg"))
  // img = resize(img, Method.QUALITY, Mode.FIT_TO_WIDTH, 510)
  // ImageIO.write(img, "jpeg", new File("./src/main/webapp/images/tmp.jpeg"))

  def transform(in: NodeSeq): NodeSeq = {
    <img src={"/cached?url=/static/images/luxmundi.png&rw=510"}/>
    <p>{System.getProperty("user.dir")}</p>
  }
}

}
}
