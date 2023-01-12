package sg.smarterhealth.opd

import com.google.protobuf.timestamp.Timestamp
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import sg.smarterhealth.proto.v1.opd.OPDAppointmentServiceGrpc
import sg.smarterhealth.proto.v1.opd._

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.util.Random

object OPDAppointmentServiceImpl extends OPDAppointmentServiceGrpc.OPDAppointmentService with App {

  override def initiateOPDAppointment(request: InitiateOPDRequest): Future[InitiateOPDResponse] = {
    println(s"initiated OPD appointment for client [${request.clientName}] and specialist [${request.specialistId}]...")
    val resp = InitiateOPDResponse(id = Random.nextLong(), referenceNo = UUID.randomUUID().toString, unitId = Random.nextLong())

    Future.successful(resp)
  }

  override def scheduleAppointment(request: ScheduleAppointmentRequest): Future[com.google.protobuf.empty.Empty] = {
    println(s"scheduled appointment refNo [${request.refNo}]...")
    Future.successful(com.google.protobuf.empty.Empty())
  }

  override def getOPDAppointments(request: OPDFilters,
                                  responseObserver: StreamObserver[OPDAppointmentsPaginatedResponse]): Unit = {

    println(s"streaming OPD Appointments fetched by filter: $request")

    val mockData = Seq(
      OPDAppointmentsPaginatedResponse(
        data = Seq(
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond))),
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond)))
        ),
        size = 2,
        page = 1
      ),
      OPDAppointmentsPaginatedResponse(
        data = Seq(
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond))),
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond)))
        ),
        size = 2,
        page = 2
      ),
      OPDAppointmentsPaginatedResponse(
        data = Seq(
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond))),
          OPDAppointment(id = Random.nextInt(),
            refNo = UUID.randomUUID().toString.take(6),
            patientId = Random.nextInt(), specialistId = Random.nextInt(),
            createdAt = Some(Timestamp(Instant.now.getEpochSecond)))
        ),
        size = 2,
        page = 3
      )
    )

    mockData.foreach(data => {
      Thread.sleep(2000)
      println(s"streaming page ${data.page}...")
      responseObserver.onNext(data)
    })

    Thread.sleep(3000)
    responseObserver.onCompleted()
  }

  import scala.concurrent.ExecutionContext

  // start server
  val server = ServerBuilder
    .forPort(9000)
    .addService(ProtoReflectionService.newInstance())
    .addService(OPDAppointmentServiceGrpc.bindService(OPDAppointmentServiceImpl, ExecutionContext.global))
    .build()
    .start()

  println("OPDAppointmentServiceImpl started...")
  // block until shutdown
  if (server != null) {
    server.awaitTermination()
  }
}
