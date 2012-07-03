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

import scala.util.parsing.combinator._

case class HocrBbox(
  x: Int,
  y: Int,
  w: Int,
  h: Int,
  items: Option[Seq[(Seq[Int], Double)]] = None
) extends Bbox {
  def glyphs: Option[Seq[Glyph]] = this.items.map(_.map {
    // Unfinished: need to average cut.
    case (cuts, _) => null
  })
}

// This is a quick draft of a parser for hOCR bounding box specifications
// that may contain character-level information in the form of cuts and
// negative log probabilities.
object HocrBboxParser extends JavaTokenParsers {
  override val skipWhitespace = false
  val integer = this.wholeNumber ^^ (_.toInt)
  val floating = this.floatingPointNumber ^^ (_.toDouble)

  // Whitespace
  val ws = "\\s+".r

  // A semicolon, possibly with surrounding whitespace.
  val sc = "\\s*;\\s*".r

  val bbox = "bbox" ~> repN(4, this.ws ~> this.integer) ^^ {
    case List(x1, y1, x2, y2) => ((x1, y1), (x2, y2))
    case _ => throw new RuntimeException("Will never happen.")
  }

  val cuts = "cuts" ~ this.ws ~> rep1sep(this.delta, this.ws)
  val delta = rep1sep(this.integer, ",")

  def nlps(cs: Seq[Seq[Int]]) =
    "nlp" ~> repN(cs.size, this.ws ~> this.floating) ^^ (cs.zip(_))

  val line = this.bbox ~ opt((this.sc ~> this.cuts) >> {
    cs => opt(this.sc ~> this.nlps(cs)) ^^ (_.getOrElse(cs.map((_, 0.0))))
  }) ^^ {
    case ((x1, x2), (y1, y2)) ~ items =>
      HocrBbox(x1, y1, x2 - x1, y2 - y1, items)
  }

  def apply(s: String) = this.parseAll(this.line, s.trim)
}

