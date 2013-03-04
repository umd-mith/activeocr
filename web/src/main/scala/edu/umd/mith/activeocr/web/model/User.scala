/*
 * #%L
 * Active OCR Web Application
 * %%
 * Copyright (C) 2011 - 2013 Maryland Institute for Technology in the Humanities
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

// Adapted from "Example project: Lift OpenID integration with openid-selector"
// Written by Tim Williams
// https://www.assembla.com/spaces/liftweb/wiki/OpenID

package edu.umd.mith.activeocr.web {
package model {

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._

import net.liftweb.http._
import net.liftmodules.openid._

object User extends User with MetaOpenIDProtoUser[User] with LongKeyedMetaMapper[User] { 
  def openIDVendor = MyVendor
  override def screenWrap = Full(<lift:surround with="default" at="content"><lift:bind /></lift:surround>) 
  override def dbTableName = "users"
  override def fieldOrder: List[FieldPointerType] = List(uniqueName, nickname, firstName, lastName, email, locale, timezone, password)
  override def editFields: List[FieldPointerType] = List(uniqueName, nickname, firstName, lastName, email, locale, timezone, password)
  override def loginXhtml =
    <form method="post" action={S.uri}>
      <table>
        <tr>
          <td colspan="2">{S.?("log.in")}</td>
        </tr>
        <tr>
          <td>OpenID</td><td><input type="text" value="https://www.google.com/accounts/o8/id" name={openIDVendor.PostParamName}/></td>
        </tr>
        <tr>
          <td>&nbsp;</td><td><user:submit /></td>
        </tr>
      </table>
    </form>
} 

class User extends LongKeyedMapper[User] with OpenIDProtoUser[User] { 
  def getSingleton = User 
  private def findByUniqueName(str: String): List[User] = model.User.findAll(By(uniqueName, str))
  object ValidUniqueName {
   val is = """^[a-z][a-z0-9_]*$""".r
   def apply(in: String): Boolean = is.findFirstIn(in).isDefined
  }
  object uniqueName extends MappedString(this, 32) {
    override def dbIndexed_? = true
    def deDupUnderscore(in: String): String = in.indexOf("__") match {
      case -1 => in
      case pos => deDupUnderscore(in.substring(0, pos)+in.substring(pos + 1))
    }
    override def setFilter = notNull _ :: toLower _ :: trim _ :: deDupUnderscore _ :: super.setFilter
    private def validateUniqueName(str: String): List[FieldError] = {
      val others = getSingleton.findByUniqueName(str).
      filter(_.id.is != fieldOwner.id.is)
      others.map(u => FieldError(this, <xml:group>Duplicate uniqueName: {str}</xml:group>))
    }
    private def validText(str: String): List[FieldError] =
    if (ValidUniqueName(str)) Nil
    else List(FieldError(this,
                         <xml:group>Invalid uniqueName.  Must start with
                          a letter and contain only letters,
                          numbers or "_"</xml:group>))
      override def validations = validText _ :: validateUniqueName _ :: super.validations
    }
  override def niceName: String = uniqueName.get
}

}
}

