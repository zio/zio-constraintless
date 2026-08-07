import zio.json.ast.Json
import zio.sbt.ZioSbtCiPlugin._
import zio.sbt.githubactions.{Job, Step, Strategy}

val Scala212 = "2.12.21"
val Scala213 = "2.13.18"
val Scala3 = "3.8.2"

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
      ),
      Developer(
        "afsalthaj",
        "Afsal Thaj",
        "https://github.com/afsalthaj",
        url("https://github.com/afsalthaj")
      )
    ),
    versionScheme := Some("early-semver"),
    crossScalaVersions := List(Scala212, Scala213, Scala3),
    scalaVersion := Scala213,
    scalacOptions ++= List(
      "-Xfatal-warnings",
      "-feature",
      "-language:higherKinds"
    ),
    scalacOptions ++= (
      if (scalaVersion.value.startsWith("3"))
        Seq(
          "-Ykind-projector",
          "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
        )
      else Seq()
    ),
    ciEnabledBranches := Seq("master"),
    ciTargetJavaVersions := Seq("17", "21"),
    ciTestJobs := Seq(
      Job(
        id = "test",
        name = "Test",
        strategy = Some(
          Strategy(
            matrix = Map("java" -> ciTargetJavaVersions.value.toList),
            failFast = false
          )
        ),
        steps = Seq(
          SetupLibuv,
          SetupJava("${{ matrix.java }}"),
          SetupSBT,
          CacheDependencies,
          Checkout.value,
          SetupNodeJs,
          Step.SingleStep(
            name = "Test",
            run = Some("sbt +test")
          )
        )
      )
    ),
    // Works around a bug shared with zio-sbt's own release workflow (e.g. zio/zio-sbt#v0.6.3):
    // a `release`-triggered run checks out the tag, so HEAD is detached, and
    // `peter-evans/create-pull-request` then requires an explicit `base` to know which branch to
    // target — the plugin's default step doesn't set one.
    ciUpdateReadmeJobs := updateReadmeJobs.value.map { job =>
      job.copy(steps = job.steps.map {
        case s: Step.SingleStep if s.name == "Create Pull Request" =>
          s.copy(parameters =
            s.parameters + ("base" -> Json.Str(
              "${{ github.event.repository.default_branch }}"
            ))
          )
        case other => other
      })
    }
  )
)

addCommandAlias("fix", "; all scalafmtSbt scalafmtAll")
addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; Test/compile")
addCommandAlias("lint", "check")

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
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("3"))
        Seq(
          "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s",
          "-Wconf:msg=deprecated alias:s"
        )
      else Seq()
    },
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3")) Seq()
      else
        Seq(
          compilerPlugin(
            "org.typelevel" %% "kind-projector" % "0.13.4" cross CrossVersion.full
          )
        )
    )
  )
  .enablePlugins(BuildInfoPlugin)

lazy val examples = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("examples"))
  .settings(
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("3"))
        Seq(
          "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s",
          "-Wconf:msg=deprecated alias:s"
        )
      else Seq()
    },
    name := "zio-constraintless-examples",
    publish / skip := true
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("zio-constraintless-docs"))
  .settings(
    name := "zio-constraintless-docs",
    scalaVersion := Scala213,
    crossScalaVersions := List(Scala213),
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName := "ZIO Constraintless",
    mainModuleName := (core.jvm / moduleName).value,
    projectStage := ProjectStage.Development,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core.jvm)
    // `ciWorkflowName` (zio-sbt-website) intentionally left at its default ("CI") — referencing
    // it here would be ambiguous with zio-sbt-ci's key of the same name until that plugin's
    // `ciWorkflowName` -> `ciWorkflowTitle` rename (zio/zio-sbt#716) is in a published release.
  )
  .dependsOn(core.jvm)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(WebsitePlugin)
