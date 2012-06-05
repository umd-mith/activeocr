/*
 * #%L
 * Active OCR Utilities
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
package edu.umd.mith.activeocr.util.model

import scala.io._

/**
  * @author Paul Evans <decretist@gmail.com>
  *
  */

object ReadFile {
  def main(args: Array[String]) {
    /*
     * Correct way to read resources. Depends on data directory having been 
     * added to util/pom.xml.
     */
    val s = Source.fromInputStream(
      getClass.getResourceAsStream("/monumentagermani00geseuoft_djvu.txt")
    )
    s.getLines.foreach( (line) => {
      println(line)
    })
  }
}
