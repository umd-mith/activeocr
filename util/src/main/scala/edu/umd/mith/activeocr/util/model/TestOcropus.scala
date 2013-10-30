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

import java.io.PrintWriter
import scala.io.Source
import scala.xml.PrettyPrinter
import scala.xml.pull.XMLEventReader

object TestOcropus {
  def main(args: Array[String]) {
    val filename = "/luxmundi07multipage.html"
    val source = Source.fromInputStream(getClass.getResourceAsStream(filename))
    val reader = new XMLEventReader(source)
    val formatter = new PrettyPrinter(80, 2)
    val pages = LocalFileOcroReader.parsePage(reader)
    var index = 0
    var outFileName = "/dev/null"
    var printer = new PrintWriter(outFileName)
    for (page <- pages) {
      index = index + 1
      outFileName = "luxmundi" + f"$index%03d" + ".svg"
      printer = new PrintWriter(outFileName)
      printer.println(formatter.format(page.toSVG))
      printer.close()
    }
    source.close()
  }
}

