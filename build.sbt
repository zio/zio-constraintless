inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.dev/zio-flow/")),
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
    resolvers +=
      "Sonatype OSS Snapshots 01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    crossScalaVersions := List("2.12.16", "2.13.8", "3.1.2"),
    scalacOptions ++= (
      if (scalaVersion.value.startsWith("3"))
        Seq("-Ykind-projector")
      else Seq()
    )
  )
)

lazy val root =
  project
    .in(file("."))
    .settings(publish / skip := true)
    .aggregate(core, example, docs)

lazy val core = project
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

lazy val example = (project in (new File("examples")))
  .settings(
    name := "zio-constraintless-examples",
    publish / skip := true
  )
  .dependsOn(core)

lazy val docs = project
  .in(file("zio-constraintless-docs"))
  .settings(
    name := "zio-constraintless-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName := "ZIO Constraintless",
    mainModuleName := (core / moduleName).value,
    projectStage := ProjectStage.Development,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core),
    docsPublishBranch := "master"
  )
  .dependsOn(core)
  .enablePlugins(WebsitePlugin)
