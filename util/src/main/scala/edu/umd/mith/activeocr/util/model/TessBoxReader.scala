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

import java.io.FileInputStream
import java.io.InputStream
import scala.io.Source

class TessBoxReader(in: InputStream, w: Int, h: Int)
  extends FormatReader[Line, Zone] {
  // Default word space threshhold in pixels (a temporary hack).
  def lineThreshhold = 20
  def spaceThreshhold = 30

  private val LinePattern = "^(\\S+) (\\d+) (\\d+) (\\d+) (\\d+) .*".r

  lazy val container = {
    val source = Source.fromInputStream(this.in)

    val zone = source.getLines.foldLeft(Zone(IndexedSeq.empty)) {
      case (zone, LinePattern(c, x1, y1, x2, y2)) =>
        val glyph = Glyph(
          c, x1.toInt,
          this.h - y2.toInt,
          x2.toInt - x1.toInt,
          y2.toInt - y1.toInt
        )

        if (zone.children.isEmpty || glyph.y - zone.children.last.ly > this.lineThreshhold)
          zone.addChild(Line(IndexedSeq(Word(IndexedSeq(glyph)))))
        else
          zone.replaceLast { line =>
            if (glyph.x - line.children.last.rx > this.spaceThreshhold)
              line.addChild(Word(IndexedSeq(glyph)))
            else
              line.replaceLast(_.addChild(glyph))
          }
    }

    source.close()
    zone
  } 
}

object TessBoxReader extends App {
  val image = org.apache.sanselan.Sanselan.getBufferedImage(
    new java.io.File(args(1))
  )

  val reader = new TessBoxReader(new FileInputStream(args(0)), image.getWidth, image.getHeight)
  reader.foreach { line =>
    println(line.children.map(_.children.map(_.c).mkString).mkString(" "))
  }
}

