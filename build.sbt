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
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
)

name := "zio-constraintless"

version := "0.2.1"

crossScalaVersions := List("2.12.16", "2.13.8", "3.1.2")

scalacOptions ++=
  (if (scalaVersion.value.startsWith("3"))
     Seq("-Ykind-projector")
   else Seq())

lazy val core = project in (new File("core"))

lazy val example = (project in (new File("examples")))
  .settings(publish / skip := true)
  .dependsOn(core)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("3")) Seq()
  else
    Seq(
      compilerPlugin(
        "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
      )
    )
)

lazy val docs = project
  .in(file("zio-constraintless-docs"))
  .settings(
    publish / skip := true,
    moduleName := "zio-constraintless-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings"
  )
  .dependsOn(core)
  .enablePlugins(WebsitePlugin)
