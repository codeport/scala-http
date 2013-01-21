import sbt._
import Keys._

object ScalaHttpServerBuild extends Build {
  import Dependencies._

  lazy val root = Project(
    id = "scala-http-server",
    base = file("."),
    settings = BuildSetting.settings ++ Seq(libraryDependencies ++= coreDeps)
  )
}

object Resolvers {
  val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val all      = Seq(typesafe)
}

object Dependencies {
  
  val coreDeps = Seq(
    "com.typesafe.akka" % "akka-actor_2.10" % "2.1.0",
    "org.apache.tika" % "tika-core" % "1.2"
  )
}

object BuildSetting {
  val org          = "laScala Korea"
  val ver          = "1.0.0-SNAPSHOT"
  val scalaVer     = "2.10.0"
  val settings     = Defaults.defaultSettings ++ Seq(
        organization := org,
        version      := ver,
        scalaVersion := scalaVer,
        shellPrompt  := SBTPrompt.setting,
        resolvers    := Resolvers.all)
}

// SBT Prompt 를 튜닝하기 위한 세팅.
object SBTPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}
    def error(s: => String) {}
    def buffer[T](f: => T): T = f
  }

  def gitBranches = ("git branch --no-color" lines_! devnull mkString)

  val current = """\*\s+([\w-/]+)""".r
  val setting = {
    (state: State) => {
      val currBranch =
        current findFirstMatchIn gitBranches map (_ group (1)) getOrElse "-"
      val currProject = Project.extract(state).currentProject.id
      "%s:%s:%s> ".format(
        currProject, currBranch, BuildSetting.ver
      )
    }
  }
}

