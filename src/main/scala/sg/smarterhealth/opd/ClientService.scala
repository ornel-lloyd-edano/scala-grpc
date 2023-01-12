package sg.smarterhealth.opd

import com.google.protobuf.timestamp.Timestamp
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import sg.smarterhealth.proto.v1.opd.{AppointmentSlot, InitiateOPDRequest, OPDAppointmentServiceGrpc, OPDAppointmentsPaginatedResponse, OPDFilters, ScheduleAppointmentRequest}

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success, Try}

object ClientService extends App {

  val channel =
    ManagedChannelBuilder
      .forAddress("localhost", 9000)
      .usePlaintext(true)
      .build()

  val blockingClient = OPDAppointmentServiceGrpc.blockingStub(channel)
  val asyncClient = OPDAppointmentServiceGrpc.stub(channel)

  def initiateOPDRequest(clientName: String, specialistId: Long, facilityId: Long, specialty: String, country: String): Unit = {
    val request = InitiateOPDRequest(clientName, specialistId, facilityId, specialty, country)

    Try(blockingClient.initiateOPDAppointment(request)) match {
      case Success(response)=>
        println(s"Received response from service: $response")

      case Failure(exception)=>
        println("ooops")
        exception.printStackTrace()
    }
  }

  def scheduleAppointment(refNo: String): Unit = {
    val request = ScheduleAppointmentRequest(refNo,
      openSchedules = List(AppointmentSlot(
        startAt = Some(Timestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))),
        endAt = Some(Timestamp(LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.UTC))),
        expireAt = None,
        priority = 1
      )))

    asyncClient.scheduleAppointment(request).onComplete {
      case Success(_)=>
        println(s"Done scheduling appointment for OPD request $refNo")
      case Failure(exception)=>
        println("ooops")
        exception.printStackTrace()
    }
  }

  def getOPDAppointments(country: String, dateFrom: LocalDate, dateTo: LocalDate): Unit = {
    val observer = new StreamObserver[OPDAppointmentsPaginatedResponse] {
      override def onNext(value: OPDAppointmentsPaginatedResponse): Unit = {
        println(s"fetched from server: ${value.data.mkString("; ")}")
      }

      override def onError(t: Throwable): Unit = ???

      override def onCompleted(): Unit = {
        println(s"completed fetching all data from server")
      }
    }
    val filterRequest = OPDFilters(country,
      Some(Timestamp(dateFrom.atStartOfDay().toEpochSecond(ZoneOffset.UTC))),
      Some(Timestamp(dateTo.atStartOfDay().toEpochSecond(ZoneOffset.UTC)))
    )
    println(s"getOPDAppointments...")
    asyncClient.getOPDAppointments(filterRequest, observer)
  }

  println("Running client...")

  initiateOPDRequest("SH", 100, 10, "Cardiology", "Singapore")

  scheduleAppointment("179c0da0-c371")

  getOPDAppointments("Singapore", LocalDate.now(), LocalDate.now().plusDays(5))
  while (true) {}
}
