package com.rahasak.xrpc

import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import vip.RahasakServiceGrpc.RahasakService
import vip.{HelloRequest, HelloResponse, RahasakServiceGrpc}
import scala.concurrent.ExecutionContext.global

import scala.concurrent.Future

class RahasakServiceImpl extends RahasakService {
  override def sayHello(request: HelloRequest) = {
    println(s"got request $request")
    val resp = HelloResponse("hoooo")
    Future.successful(resp)
  }

  override def clientStream(responseObserver: StreamObserver[HelloResponse]): StreamObserver[HelloRequest] = {
    val requestObserver = new StreamObserver[HelloRequest] {
      override def onNext(value: HelloRequest) = {
        println(s"got stream $value")
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println("end stream")
      }
    }

    requestObserver
  }

  override def serverStream(request: HelloRequest, responseObserver: StreamObserver[HelloResponse]) = {
    println(s"got stream request $request")

    val values: List[String] = List(
      "First Stream packet",
      "Second stream packet",
      "Third Stream packet",
      "Fourth Stream packet"
    )

    values.foreach { a =>
      // send response, handle via actor
      responseObserver.onNext(HelloResponse(s"stream response $a"))
      Thread.sleep(1000)
    }
  }

  override def streamHello(responseObserver: StreamObserver[HelloResponse]): StreamObserver[HelloRequest] = {
    val requestObserver = new StreamObserver[HelloRequest] {
      override def onNext(value: HelloRequest) = {
        println(s"got stream $value")

        // send message to same client
        responseObserver.onNext(HelloResponse("response"))
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println("end stream")
      }
    }

    requestObserver
  }
}

object Rahasak extends App {

  import scala.concurrent.ExecutionContext

  // start server
  val server = ServerBuilder
    .forPort(9000)
    .addService(ProtoReflectionService.newInstance())
    .addService(RahasakServiceGrpc.bindService(new RahasakServiceImpl, ExecutionContext.global))
    .build()
    .start()

  // block until shutdown
  if (server != null) {
    server.awaitTermination()
  }

}
