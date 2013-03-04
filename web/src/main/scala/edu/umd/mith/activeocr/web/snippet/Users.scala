/*
 * #%L
 * Active OCR Web Application
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
package edu.umd.mith.activeocr.web {
package snippet {

import net.liftweb.util._
import net.liftweb.util.Helpers._
import scala.xml.NodeSeq

class Users {
  val items = model.User.findAll()
  def render(in: NodeSeq): NodeSeq = {
    val tableRows = items.map(item => 
      <tr>
        <td>{item.uniqueName}</td>
        <td>{item.nickname}</td>
        <td>{item.firstName}</td>
        <td>{item.lastName}</td>
        <td>{item.email}</td>
        <td>{item.locale}</td>
        <td>{item.timezone}</td>
      </tr>)
    Helpers.bind("prefix", in, "tableRows" -> tableRows)
  }
}

}
}

