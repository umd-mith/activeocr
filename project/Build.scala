import sbt._
import Keys._

object ActiveOCR extends Build {
  lazy val root: Project = Project(
    id = "activeocr",
    base = file("."),
    settings = commonSettings
  ).aggregate(core, web)

  lazy val core: Project = Project(
    id = "activeocr-core",
    base = file("util"),
    settings = commonSettings ++ Seq(
      unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
        Seq(base / "data")
      }
    )
  )

  lazy val web: Project = Project(
    id = "activeocr-web",
    base = file("web"),
    dependencies = Seq(core),
    settings = commonSettings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings ++ Seq(
      libraryDependencies ++= Seq(
        "edu.umd.mith" % "lift-util" % "0.2.0-SNAPSHOT",
        "com.h2database" % "h2" % "1.2.138" % "runtime",
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
        "net.liftmodules" % "openid_2.10" % "2.5-RC1-1.2"
      )
    )
  )

  def commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "edu.umd.mith.activeocr",
    version := "0.1.0-SNAPSHOT",
    resolvers ++= Seq(
      "Sonatype snapshots"
        at "http://oss.sonatype.org/content/repositories/snapshots",
      "SpringSource Enterprise Bundle Repository - External Bundle Releases"
        at "http://repository.springsource.com/maven/bundles/external",
      "MITH snapshots"
        at "http://umd-mith.github.com/maven-repository/snapshots"
    ),
    scalaVersion := "2.10.2",
    scalaBinaryVersion := "2.10",
    scalacOptions := Seq(
      "-feature",
      "-language:implicitConversions",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies <++= scalaVersion(sv => Seq(
      "org.slf4j" % "slf4j-simple" % "1.6.4",
      "edu.umd.mith" % "proto" % "0.2.0-SNAPSHOT",
      "javax.media.jai" % "com.springsource.javax.media.jai.core" % "1.1.3"
    ))
  )
}

