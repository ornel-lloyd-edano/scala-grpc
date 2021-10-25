package com.rahasak.xrpc

import com.rahasak.proto.document._
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object DocumentClientApp extends App {
  // build gRPC channel
  val channel =
    ManagedChannelBuilder
      .forAddress("192.168.1.44", 9000)
      .usePlaintext(true)
      .build()

  // blocking and async stubs
  val blockingStub = DocumentServiceGrpc.blockingStub(channel)
  val asyncStub = DocumentServiceGrpc.stub(channel)

  /*
    create document with synchronous call
    consume createDocument unary api in the grpc service
   */
  def createDocument(): Unit = {
    val request = DocumentCreateMessage("1", "blob1")
    println(s"[unary] blocking create document $request")

    val reply = blockingStub.createDocument(request)
    println(s"[unary] create document reply $reply")
  }

  /**
    * create document with asynchronous call
    * consume createDocument unary api in the grpc service
    */
  def createDocumentAsync() = {
    val request = DocumentCreateMessage("1", "blob1")
    val reply = asyncStub.createDocument(request)
    println(s"[unary] async create document $request")

    reply.onComplete {
      case Success(reply) =>
        println(s"[unary] async create document reply $reply")
      case Failure(e) =>
        e.printStackTrace()
    }
  }

  /**
    * create documents with asynchronous call
    * consume createDocuments client stream api in the grpc service
    */
  def createDocuments() = {
    val responseObserver = new StreamObserver[StatusReplyMessage] {
      override def onNext(value: StatusReplyMessage) = {
        println(s"[client stream] create documents reply $value")
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println(s"[client stream] end stream")
      }
    }

    val requestObserver = asyncStub.createDocuments(responseObserver)
    val documents = List(
      ("1", "blob1"),
      ("2", "blob2"),
      ("3", "blob4"),
      ("4", "blob1")
    )
    documents.foreach { d =>
      val request = DocumentCreateMessage(d._1, d._2)
      requestObserver.onNext(request)
      println(s"[client stream] create documents $request")

      Thread.sleep(1000)
    }
  }

  /**
    * get documents with asynchronous call
    * consume getDocuments server stream api in the grpc service
    */
  def getDocuments() = {
    val responseObserver = new StreamObserver[DocumentReplyMessage] {
      override def onNext(value: DocumentReplyMessage) = {
        println(s"[server stream] get documents $value")
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println(s"[server stream] end stream")
      }
    }

    val request = DocumentGetMessage("1, 2, 3")
    asyncStub.getDocuments(request, responseObserver)
    println(s"[server stream] get documents $request")
  }

  /**
    * stream documents with asynchronous call
    * consume streamDocuments bi-directional stream api in the grpc service
    */
  def streamDocuments() = {
    val responseObserver = new StreamObserver[DocumentReplyMessage] {
      override def onNext(value: DocumentReplyMessage) = {
        println(s"[bi-stream] get documents $value")
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println(s"[bi-stream] end stream")
      }
    }

    val requestObserver = asyncStub.streamDocuments(responseObserver)
    val documents = List(
      ("1", "blob1"),
      ("2", "blob2"),
      ("3", "blob4"),
      ("4", "blob1")
    )
    documents.foreach { d =>
      val request = DocumentGetMessage(d._1)
      requestObserver.onNext(request)
      println(s"[bi-stream] get document $request")

      Thread.sleep(1000)
    }
  }

  //createDocument()
  //createDocumentAsync()
  //createDocuments()
  //getDocuments()
  //streamDocuments()

  // wait main thread till futures/streams to complete
  // otherwise main thread will exit before printing the results
  while (true) {}
}
