name := "zio-constraintless"

version := "0.2.1"

crossScalaVersions := List("2.12.16", "2.13.8", "3.1.2")

scalacOptions ++=
  (if (scalaVersion.value.startsWith("3"))
     Seq("-Ykind-projector")
   else Seq())

lazy val core = project in (new File("core"))

lazy val example = (project in (new File("examples"))).dependsOn(core)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("3")) Seq()
  else
    Seq(
      compilerPlugin(
        "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
      )
    )
)
