package com.rahasak.xrpc

import com.rahasak.proto.document._
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object DocumentClientApp extends App {
  val channel =
    ManagedChannelBuilder
      .forAddress("192.168.1.44", 9000)
      .usePlaintext(true)
      .build()
  val blockingStub = DocumentServiceGrpc.blockingStub(channel)
  val asyncStub = DocumentServiceGrpc.stub(channel)

  def createDocument(): Unit = {
    val request = DocumentCreateMessage("1", "blob1")
    println(s"[unary] blocking create document $request")

    val reply = blockingStub.createDocument(request)
    println(s"[unary] create document reply $reply")
  }

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
  getDocuments()

  // wait main thread till futures/streams to complete
  // otherwise main thread will exit before printing the results
  while (true) {}
}
