/*
 * #%L
 * MITH Data Mining Utilities
 * %%
 * Copyright (C) 2011 Maryland Institute for Technology in the Humanities
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

import scala.io.Source
import scala.xml.pull.XMLEventReader

import org.specs2.mutable._
import org.specs2.specification.Scope

class TessReaderTest extends SpecificationWithJUnit with TessExample {
  "the HOCR output" should {
    "have 1 page" in {
      this.pages.size must_== 1
    }

    "have 3 zones in" in {
      this.pages.head.children.size must_== 3
    }

    "have 29 lines in its second zone" in {
      this.pages.head.children(1).children.size must_== 29
    }

    "have a correctly size and positioned 'g' as the first glyph of the " +
    "seventh word of the eighth line of the second zone" in {
      val glyph = pages.head.children(1).children(9) match {
        case ContLine(words) => words(6) match {
          case ContWord(glyphs) => Some(glyphs(0))
          case _ => None
        }
        case _ => None
      }
      glyph must beSome(Glyph("g", 318, 575, 15, 21))
    }
  }
}

trait TessExample extends Scope {
  val filename = "/luxmundi302.html"

  val source = Source.fromInputStream(
    this.getClass.getResourceAsStream(filename)
  )

  val reader = new XMLEventReader(source)
  val pages = TessReader.parsePage(
    reader,
    this.getClass.getResource("/luxmundi.jpeg").toURI
  )

  source.close()
}

