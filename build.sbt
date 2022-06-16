name := "constraintless"

version := "0.1.3"

crossScalaVersions := List("2.12.16", "2.13.8", "3.1.2")

scalacOptions ++=
  (if (scalaVersion.value.startsWith("3"))
     Seq("-Ykind-projector")
   else Seq())

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("3")) Seq()
  else
    Seq(
      compilerPlugin(
        "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
      )
    )
)

inThisBuild(
  List(
    organization := "io.github.afsalthaj",
    homepage := Some(url("https://github.com/sbt/sbt-ci-release")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "Afsal",
        "Afsal Thaj",
        "afsal.taj06@gmail.com",
        url("https://afsalthaj.github.io/myblog/")
      )
    )
  )
)
