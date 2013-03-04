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

import edu.umd.mith.activeocr.web.model._
import net.liftweb.http.S
import net.liftweb.mapper.By
import scala.xml.NodeSeq

class User {
  val username = S.param("username").openOr("")
  val items = model.User.findAll(By(User.uniqueName, username))
  val response: NodeSeq = {
    if (items.length == 1) {
      <p>{username} does matches a uniqueName in the database.</p>
      <table>
        <tr>
          <th>uniqueName</th>
          <th>nickname</th>
          <th>firstName</th>
          <th>lastName</th>
          <th>email</th>
          <th>locale</th>
          <th>timezone</th>
        </tr>
        {items.map(item =>
          <tr>
            <td>{item.uniqueName}</td>
            <td>{item.nickname}</td>
            <td>{item.firstName}</td>
            <td>{item.lastName}</td>
            <td>{item.email}</td>
            <td>{item.locale}</td>
            <td>{item.timezone}</td>
          </tr>)
        }
      </table>
    }
    else {
      <p>{username} does not match a uniqueName in the database.<br/><a href="http://localhost:8080/user_mgt/login">Login</a> and Edit User.</p>
    }
  }

  def render(in: NodeSeq): NodeSeq = {
    {response}
  }
}

}
}

