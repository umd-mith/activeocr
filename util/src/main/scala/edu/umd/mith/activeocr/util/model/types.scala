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
  def color: Option[String] = None
  def rx = this.x + this.w
  def ly = this.y + this.h
  def toSVG: scala.xml.NodeSeq =
    <rect style={"%sstroke-width: 4; fill: none;".format(
      color.map("stroke: %s; ".format(_)).getOrElse("")
    )} x={this.x.toString} y={this.y.toString}
    width={this.w.toString} height={this.h.toString}/>
  
  def listify: Unit = println(this)
  // def bbList: List[Bbox] = List(this)
  def bbList: IndexedSeq[Bbox] = IndexedSeq(this)
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

  override def toSVG = super.toSVG ++ this.children.flatMap(_.toSVG)
  
  override def listify: Unit = this.children.foreach(_.listify)
  // override def bbList: List[Bbox] = this.children.flatMap(_.bbList) (collection.breakOut)
  override def bbList: IndexedSeq[Bbox] = this.children.flatMap(_.bbList)
}

trait Word extends Bbox { override val color = Some("green") }

trait Line extends Bbox { override val color = Some("blue") }

case class Glyph(c: String, x: Int, y: Int, w: Int, h: Int) extends Bbox {
  override val color = Some("orange")
}

case class TermWord(s: String, x: Int, y: Int, w: Int, h: Int) extends Word

case class ContWord(children: IndexedSeq[Glyph]) 
    extends Container[Glyph, ContWord] with Word {
  def addChild(child: Glyph) = this.copy(children = this.children :+ child)

  def replaceLast(f: Glyph => Glyph) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

case class TermLine(s: String, x: Int, y: Int, w: Int, h: Int) extends Line

case class ContLine(children: IndexedSeq[Word]) 
    extends Container[Word, ContLine] with Line {
  def addChild(child: Word) = this.copy(children = this.children :+ child)

  def replaceLast(f: Word => Word) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

case class Zone(children: IndexedSeq[Line]) extends Container[Line, Zone] {
  override val color = Some("red")
  def addChild(child: Line) = this.copy(children = this.children :+ child)

  def replaceLast(f: Line => Line) = 
    this.copy(children = this.children.init :+ f(this.children.last))
}

case class Page(children: IndexedSeq[Zone], uri: String, imageW: Int, imageH: Int) extends Container[Zone, Page] {
  def addChild(child: Zone) = this.copy(children = this.children :+ child)

  def replaceLast(f: Zone => Zone) = 
    this.copy(children = this.children.init :+ f(this.children.last))

  override def toSVG = toSVG(uri)

  def toSVG(uri: String) =
    <svg version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink= "http://www.w3.org/1999/xlink"
      width="100%" height="100%"
      viewBox={"0 0 %d %d".format(imageW, imageH)}>
      <image xlink:href={uri}
        width={imageW.toString} height={imageH.toString}/>
      { this.children.map(_.toSVG) }
    </svg>
}

trait FormatReader[A <: Bbox, B <: Container[A, B]] extends Iterable[A] {
  def container: B
  def iterator = this.container.children.iterator
}

