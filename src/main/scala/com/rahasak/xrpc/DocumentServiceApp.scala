package com.rahasak.xrpc

import com.rahasak.proto.document.DocumentServiceGrpc.DocumentService
import com.rahasak.proto.document._
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver

import scala.concurrent.Future

class DocumentServiceImpl extends DocumentService {
  override def createDocument(request: DocumentCreateMessage): Future[StatusReplyMessage] = {
    println(s"[unary] create document $request")

    Future.successful(StatusReplyMessage("201", "created document"))
  }

  override def createDocuments(responseObserver: StreamObserver[StatusReplyMessage]): StreamObserver[DocumentCreateMessage] = {
    val requestObserver = new StreamObserver[DocumentCreateMessage] {
      var count = 0

      override def onNext(value: DocumentCreateMessage) = {
        println(s"[client stream] create document $value")
        count = count + 1
      }

      override def onError(t: Throwable): Unit = {
        t.printStackTrace()
      }

      override def onCompleted(): Unit = {
        println(s"[client stream] end stream")
        responseObserver.onNext(StatusReplyMessage("201", s"created $count document"))
        responseObserver.onCompleted()
      }
    }

    requestObserver
  }

  override def getDocuments(request: DocumentGetMessage, responseObserver: StreamObserver[DocumentReplyMessage]): Unit = {
    println(s"[server stream] get documents $request")

    val documents = List(
      ("1", "blob1"),
      ("2", "blob2"),
      ("3", "blob4"),
      ("4", "blob1")
    )

    documents.foreach { d =>
      responseObserver.onNext(DocumentReplyMessage(d._1, d._2))
      Thread.sleep(1000)
    }
  }

  override def streamDocuments(responseObserver: StreamObserver[DocumentReplyMessage]) = {
    val requestObserver = new StreamObserver[DocumentGetMessage] {
      override def onNext(value: DocumentGetMessage) = {
        println(s"[bi-stream] stream documents $value")

        responseObserver.onNext(DocumentReplyMessage())
      }

      override def onError(t: Throwable) = {
        t.printStackTrace()
      }

      override def onCompleted() = {
        println(s"[bi-stream] end stream")
      }
    }

    requestObserver
  }

}

object DocumentServiceApp extends App {

  import scala.concurrent.ExecutionContext

  // start server
  val server = ServerBuilder
    .forPort(9000)
    .addService(ProtoReflectionService.newInstance())
    .addService(DocumentServiceGrpc.bindService(new DocumentServiceImpl, ExecutionContext.global))
    .build()
    .start()

  // block until shutdown
  if (server != null) {
    server.awaitTermination()
  }

}

