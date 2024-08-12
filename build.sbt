val Scala212 = "2.12.19"
val Scala213 = "2.13.13"
val Scala3 = "3.3.3"

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
    crossScalaVersions := List(Scala212, Scala213, Scala3),
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
            "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
          )
        )
    )
  )
  .enablePlugins(BuildInfoPlugin)

lazy val examples = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("examples"))
  .settings(
    name := "zio-constraintless-examples",
    publish / skip := true
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("zio-constraintless-docs"))
  .settings(
    name := "zio-constraintless-docs",
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
