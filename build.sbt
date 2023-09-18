val Scala211 = "2.11.12"
val Scala212 = "2.12.17"
val Scala213 = "2.13.10"
val Scala3 = "3.3.1"

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.dev/zio-constraintless/")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "jdegoes",
        "John De Goes",
        "john@degoes.net",
        url("http://degoes.net")
      )
    ),
    crossScalaVersions := List(Scala211, Scala212, Scala213, Scala3),
    scalaVersion := Scala213,
    scalacOptions ++= List(
      "-Xfatal-warnings",
      "-feature",
      "-language:higherKinds"
    ),
    scalacOptions ++= (
      if (scalaVersion.value.startsWith("3"))
        Seq("-Ykind-projector")
      else Seq()
    )
  )
)

addCommandAlias("fix", "; all scalafmtSbt scalafmtAll")
addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; Test/compile")

lazy val root =
  project
    .in(file("."))
    .settings(
      publish / skip := true,
      crossScalaVersions := List() // override because we set it in `inThisBuild`
    )
    .aggregate(
      core.js,
      core.jvm,
      core.native,
      docs,
      examples.js,
      examples.jvm,
      examples.native
    )

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    name := "zio-constraintless",
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3")) Seq()
      else
        Seq(
          compilerPlugin(
            "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
          )
        )
    )
  )
  .enablePlugins(BuildInfoPlugin)

lazy val examples = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("examples"))
  .settings(
    name := "zio-constraintless-examples",
    publish / skip := true,
    crossScalaVersions -= Scala211
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("zio-constraintless-docs"))
  .settings(
    name := "zio-constraintless-docs",
    crossScalaVersions -= Scala211,
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName := "ZIO Constraintless",
    mainModuleName := (core.jvm / moduleName).value,
    projectStage := ProjectStage.Development,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core.jvm),
    docsPublishBranch := "master",
    ciWorkflowName := "Website"
  )
  .dependsOn(core.jvm)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(WebsitePlugin)
