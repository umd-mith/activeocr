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

/**
  * @author Paul Evans
  *
  */

// Simple class to experiment with elements of the model.

object Main {
  def main(args: Array[String]) {
    val a = new Glyph("a", 10, 10, 10, 10)
    val b = new Glyph("b", 20, 10, 10, 10)
    val c = new Glyph("c", 30, 10, 10, 10)
    val d = new Glyph("d", 40, 10, 10, 10)
    
    val children = IndexedSeq(a, b, c, d)

    val word = new Word(children)

    println(word)
    println(word.x, word.y, word.w, word.h, word.rx, word.ly)
  }
}
