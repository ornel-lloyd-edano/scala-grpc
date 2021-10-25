//package com.rahasak.xrpc
//
//import io.grpc.ManagedChannelBuilder
//import io.grpc.stub.StreamObserver
//import vip.{HelloRequest, HelloResponse, RahasakServiceGrpc}
//import scala.concurrent.ExecutionContext.global
//
//import scala.util.{Failure, Success}
//
//object RahasakClient extends App {
//  val channel =
//    ManagedChannelBuilder
//      .forAddress("192.168.1.44", 9000)
//      .usePlaintext(true)
//      .build()
//  val blockingStub = RahasakServiceGrpc.blockingStub(channel)
//  val asyncStub = RahasakServiceGrpc.stub(channel)
//
//  def unary() = {
//    // blocking unary
//    val request = HelloRequest("keriyooo")
//    val reply = blockingStub.sayHello(request)
//    println(reply)
//  }
//
//  def unaryAsync() = {
//    // async unary
//    val request = HelloRequest("keriyooo")
//    val f = asyncStub.sayHello(request)
//    f.onComplete {
//      case Success(value) =>
//        println(s"got response $value")
//      case Failure(e) =>
//        e.printStackTrace()
//    }
//  }
//
//  def serverStream() = {
//    val responseObserver = new StreamObserver[HelloResponse] {
//      override def onNext(value: HelloResponse) = {
//        println(s"got server stream $value")
//      }
//
//      override def onError(t: Throwable) = {
//        t.printStackTrace()
//      }
//
//      override def onCompleted() = {
//        println("end server stream")
//      }
//    }
//    asyncStub.serverStream(HelloRequest("lets stream"), responseObserver)
//  }
//
//  def clientStream() = {
//    val responseObserver = new StreamObserver[HelloResponse] {
//      override def onNext(value: HelloResponse) = {
//        println(s"got client stream $value")
//      }
//
//      override def onError(t: Throwable) = {
//        t.printStackTrace()
//      }
//
//      override def onCompleted() = {
//        println("end client stream")
//      }
//    }
//    val requestObserver = asyncStub.clientStream(responseObserver)
//
//    val values: List[String] = List(
//      "First Stream packet",
//      "Second stream packet",
//      "Third Stream packet",
//      "Fourth Stream packet"
//    )
//    values.foreach { a =>
//      // send response, handle via actor
//      requestObserver.onNext(HelloRequest(s"stream response $a"))
//      Thread.sleep(1000)
//    }
//  }
//
//  def biStream() = {
//    val responseObserver = new StreamObserver[HelloResponse] {
//      override def onNext(value: HelloResponse) = {
//        println(s"got bi-stream $value")
//      }
//
//      override def onError(t: Throwable) = {
//        t.printStackTrace()
//      }
//
//      override def onCompleted() = {
//        println("end bi-stream")
//      }
//    }
//    val requestObserver = asyncStub.streamHello(responseObserver)
//    val values: List[String] = List(
//      "First Stream packet",
//      "Second stream packet",
//      "Third Stream packet",
//      "Fourth Stream packet"
//    )
//    values.foreach { a =>
//      // send response, handle via actor
//      requestObserver.onNext(HelloRequest(s"stream response $a"))
//      Thread.sleep(1000)
//    }
//  }
//
//  biStream()
//
//  while (true) {}
//}
