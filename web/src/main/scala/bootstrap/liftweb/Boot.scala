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
package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}
import _root_.edu.umd.mith.activeocr.web.model._
import edu.umd.mith.util.lift.image._


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // where to search snippet
    LiftRules.addToPackages("edu.umd.mith.activeocr.web")
    Schemifier.schemify(true, Schemifier.infoF _, User, SourceImage, DerivativeImage, ImageSelection)

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu("Home") / "index" >> User.AddUserMenusAfter, // Simple menu form
      Menu("Active OCR Step 7") / "activeocr7" >> User.AddUserMenusAfter,    
      Menu("Active OCR Step 7m") / "activeocr7m" >> User.AddUserMenusAfter,    
      // If you don’t add a page to your SiteMap
      // it will not be displayed by Lift on your web site.
      // Menu items marked Hidden will not be displayed in the menu hierarchy,
      // but the page can be accessed. 
      Menu("User") / "user" >> Hidden,    
      Menu("Users") / "users" >> Hidden,    
      // Menu with special Link
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")))

    LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap()))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    LiftRules.dispatch.append(new ImageCache("cached" :: Nil).matcher)

    LiftRules.dispatch.append(MyVendor.dispatchPF)
    LiftRules.snippets.append(MyVendor.snippetPF)

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(List("user", username),_,_,_),_,_) =>
          RewriteResponse("user" :: Nil, Map("username" -> username))
    }

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
