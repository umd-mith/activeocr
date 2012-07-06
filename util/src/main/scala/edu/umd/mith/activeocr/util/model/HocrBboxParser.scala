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
  items: Option[List[(List[Int], Double)]] = None
) extends Bbox {
  def glyphs(cs: Seq[String]): Option[Seq[Glyph]] = this.items.flatMap { is =>
    if (is.size + 1 == cs.size) Some {
      // For now we take the average x values.
      val averaged: List[Int] = is.map {
        case ((x :: ds), _) => x + math.round((0 :: ds).grouped(2).map {
          case dx :: dy :: Nil => dx * (dy.toDouble / h)
          case _ => throw new RuntimeException("Will never happen.")
        }.sum).toInt
        case _ => throw new RuntimeException("Will never happen.")
      }
      
      (0 :: averaged).zip(averaged :+ this.rx).zip(cs).map {
        case ((x1, x2), c) => Glyph(c, x1, y, x2 - x1, h)
      }
    } else None
  }
  def glyphs(cs: String): Option[Seq[Glyph]] = this.glyphs(cs.map(_.toString))
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

  val delta = rep1sep(this.integer, ",")

  val cuts = "cuts" ~ this.ws ~> rep1sep(this.delta, this.ws) ^? (
    { case cs if cs.forall(_.size % 2 == 1) => cs.scanLeft(0 :: Nil) {
        case (c :: _, x :: xs) => x + c :: xs
        case _ => throw new RuntimeException("Will never happen.")
      }.tail
    },
    "A cut has an even number of components: %s".format(_)
  )

  def nlps(cs: List[List[Int]]) =
    "nlp" ~> repN(cs.size, this.ws ~> this.floating) ^^ (cs.zip(_))

  val line = this.bbox ~ opt((this.sc ~> this.cuts) >> {
    cs => opt(this.sc ~> this.nlps(cs)) ^^ (_ getOrElse cs.map(_ -> 0.0))
  }) ^^ {
    case ((x1, y1), (x2, y2)) ~ items =>
      HocrBbox(x1, y1, x2 - x1, y2 - y1, items.map(_.map {
        case (cs, nlp) =>
          val left = y2 - y1 - cs.grouped(2).map(_.lift(1).getOrElse(0)).sum
          (cs :+ left, nlp)
      }))
  }

  def apply(s: String) = this.parseAll(this.line, s.trim)
}

