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

// From "Example project: Lift OpenID integration with openid-selector"
// Written by Tim Williams
// https://www.assembla.com/spaces/liftweb/wiki/OpenID

package edu.umd.mith.activeocr.web {
package model {

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._

import net.liftmodules.openid._

import org.openid4java.discovery.DiscoveryInformation
import org.openid4java.message.AuthRequest

object MyVendor extends SimpleOpenIDVendor {
  def ext(di: DiscoveryInformation, authReq: AuthRequest): Unit = {
    import WellKnownAttributes._
    WellKnownEndpoints.findEndpoint(di) map { ep =>
      ep.makeAttributeExtension(List(Email, FullName, FirstName, LastName)) foreach { ex =>
        authReq.addExtension(ex)
      }
    }
  }
  override def createAConsumer = new OpenIDConsumer[UserType] {
    beforeAuth = Full(ext _)
  }
}

}
}

