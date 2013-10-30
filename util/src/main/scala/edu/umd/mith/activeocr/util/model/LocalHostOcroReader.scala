/*
 * #%L
 * Active OCR Utilities
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
package edu.umd.mith.activeocr.util.model

import java.net.URI
import scala.util.matching.Regex

object LocalHostOcroReader extends OcroReader {
  def parseTitle(title: String): (URI, String) = {
    val pattern = new Regex("""file (temp\/\d{4}.bin.png)""", "filename")
    val result = pattern.findFirstMatchIn(title).get
    val filename = result.group("filename")
    val facsimileUri = new URI("http://localhost:8080/static/images/" + filename)
    val imageFileName = "../web/src/main/webapp/static/images/" + filename
    (facsimileUri, imageFileName)
  }
}
