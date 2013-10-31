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

class OcroReaderTest extends SpecificationWithJUnit with OcroExample {
  "the hOCR output" should {
    "have 1 page" in {
      this.pages.size must_== 1
    }

    "have 1 zone in" in {
      this.pages.head.children.size must_== 1
    }

    "have 32 lines in its first zone" in {
      this.pages.head.children(0).children.size must_== 32
    }
  }
}

trait OcroExample extends Scope {
  val filename = "/luxmundi07.html"

  val source = Source.fromInputStream(
    this.getClass.getResourceAsStream(filename)
  )

  val reader = new XMLEventReader(source)
  val pages = LocalHostOcroReader.parsePage(reader)

  source.close()
}

