name := "xrpc"

version := "1.0"

scalaVersion := "2.12.10"

libraryDependencies ++= {

  Seq(
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

// define merge conflict strategy to netty 
assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case "module-info.class" => MergeStrategy.discard
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
