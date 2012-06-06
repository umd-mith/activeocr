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

// We assume throughout a coordinate space with the origin in the upper left
// corner. The bounding box's (x, y) is its upper left corner.
trait Bbox {
  def x: Int
  def y: Int
  def w: Int
  def h: Int
  def rx = this.x + this.w
  def ly = this.y + this.h
}

trait Container[A <: Bbox, B <: Container[A, B]] extends Bbox {
  def children: IndexedSeq[A]

  // By default the container's box is the minimal bounding box containing all
  // its children. This may be overridden in subclasses: there is no guarantee
  // that all children are properly contained.
  lazy val x = this.children.map(_.x).min
  lazy val y = this.children.map(_.y).min
  lazy val w = this.children.map(_.rx).max - this.x
  lazy val h = this.children.map(_.ly).max - this.y

  def addChild(child: A): B
  def replaceLast(f: A => A): B
}

case class Glyph(c: String, x: Int, y: Int, w: Int, h: Int) extends Bbox

case class Word(children: IndexedSeq[Glyph]) extends Container[Glyph, Word] {
  def addChild(child: Glyph) = this.copy(children = this.children :+ child)

  def replaceLast(f: Glyph => Glyph) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

case class Line(children: IndexedSeq[Word]) extends Container[Word, Line] {
  def addChild(child: Word) = this.copy(children = this.children :+ child)

  def replaceLast(f: Word => Word) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

case class Page(children: IndexedSeq[Line]) extends Container[Line, Page] {
  def addChild(child: Line) = this.copy(children = this.children :+ child)

  def replaceLast(f: Line => Line) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

trait FormatReader[A <: Bbox, B <: Container[A, B]] extends Iterable[A] {
  def container: B
  def iterator = this.container.children.iterator
}

