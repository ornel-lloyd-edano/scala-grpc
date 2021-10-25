name := "xrpc"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {

  Seq(
    "org.slf4j"                       % "slf4j-api"                       % "1.7.5",
    "ch.qos.logback"                  % "logback-classic"                 % "1.0.9",
    "org.scalatest"                   % "scalatest_2.11"                  % "2.2.1"               % "test",
    "io.grpc"                         % "grpc-netty"                      % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc"                         % "grpc-services"                   % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
    "com.trueaccord.scalapb"          %% "scalapb-runtime"                % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf",
    "com.trueaccord.scalapb"          %% "scalapb-runtime-grpc"           % com.trueaccord.scalapb.compiler.Version.scalapbVersion
  )
}

// compiles protobuf to scala code
PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
