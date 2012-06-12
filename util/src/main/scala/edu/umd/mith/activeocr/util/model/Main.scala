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

// Simple class to experiment with elements of the model.

object Main {
  def main(args: Array[String]) {
    val a = new Glyph("a", 10, 10, 10, 10)
    val b = new Glyph("b", 20, 10, 10, 10)
    val c = new Glyph("c", 30, 10, 10, 10)
    val d = new Glyph("d", 40, 10, 10, 10)
    val children = IndexedSeq(a, b, c, d)
    val word1 = new Word(children)
    println(word1)
    println(word1.x, word1.y, word1.w, word1.h, word1.rx, word1.ly)

    val glyphs: IndexedSeq[Glyph] = "Testudo".map(
      c => Glyph(c.toString, -1, -1, -1, -1)
    )
    println(glyphs)
    val word2 = new Word(glyphs)
    println(word2)
    println(word2.x, word2.y, word2.w, word2.h, word2.rx, word2.ly)
  }
}
