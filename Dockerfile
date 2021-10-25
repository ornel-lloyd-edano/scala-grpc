FROM ubuntu:16.04

MAINTAINER Eranga Bandara (erangaeb@gmail.com)

# install required packages
RUN apt-get update -y
RUN apt-get install -y python-software-properties
RUN apt-get install -y software-properties-common

# install java
RUN apt-get install -y openjdk-8-jdk
RUN rm -rf /var/lib/apt/lists/*
RUN rm -rf /var/cache/openjdk-8-jdk

# set JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# working directory
WORKDIR /app

# copy file
ADD target/scala-2.11/xrpc-assembly-1.0.jar xrpc.jar
#ADD *.jar aplos.jar

# logs volume
RUN mkdir .logs
VOLUME ["/app/.logs"]

# keys volume
VOLUME ["/app/.keys"]

# command
ENTRYPOINT [ "java", "-jar", "/app/xrpc.jar" ]
