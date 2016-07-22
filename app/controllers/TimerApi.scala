package controllers

import java.sql.Timestamp

import com.google.inject.Inject
import dao.{Timer, TimerDAO}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * @author msvens
  * @since 20/07/16
  */
class TimerApi @Inject()(timerDAO: TimerDAO) extends Controller{

  import ApiContainers._

  val dtf: DateTimeFormatter = ISODateTimeFormat.dateTime()

  //Json Converters
  implicit val timestampFormat = new Format[Timestamp] {
    def reads(json: JsValue): JsResult[Timestamp] = {
      val str = json.as[String]
      JsSuccess(new Timestamp((DateTime.parse(str).getMillis)))
    }
    def writes(t: Timestamp): JsValue = {
      JsString(dtf.print(t.getTime))
    }
    //def writes(t: Timestamp): JsValue = Json.toJson(timestampToDateTime(t))
    //def reads(json: JsValue): JsResult[Timestamp] = Json.fromJson[DateTime](json).map(dateTimeToTimestamp)
  }

  implicit val apiResponseFmt = Json.format[ApiResponse]
  implicit val timerResponsFmt = Json.format[Timer]
  implicit val addTimerFmt = Json.format[AddTimer]


  def list = Action.async{ implicit r =>
    val ret = for{
      l <- timerDAO.list
    } yield(ok("list", Some(Json.toJson(l))))
    ret.recover{case e => bad("listTimers",Some(JsError.toJson(JsError(e.getMessage))))}
  }

  def get(id: Int) = Action.async{implicit r =>
   val ret = for{t <- timerDAO.get(id)} yield(t match {
     case Some(timer) => ok("get", Some(Json.toJson(timer)))
     case None => bad("no such timer")
   })
    ret.recover{case e => bad("get", Some(JsError.toJson(JsError(e.getMessage))))}
  }

  def secondsLeft(id: Int) = Action.async{implicit r =>
    for(t <- timerDAO.get(id)) yield (t match {
      case None => bad("no such timer " + id)
      case Some(timer) => {
        val millis = timer.stop.get.getTime - DateTime.now().getMillis
        
      }

    })
    null
  }



  def add = Action.async(parse.json){implicit r =>
    val t = r.body.validate[AddTimer]
    t.fold(
      errors => {
        Future(bad("addTimer", Some(JsError.toJson(errors))))
      },
      addTimer => {
        val start = addTimer.start.getOrElse(DateTime.now())
        val stop = if(addTimer.seconds.isDefined){
          start.plusSeconds(addTimer.seconds.get)
        } else {
          addTimer.stop.getOrElse(start.plusYears(1))
        }
        val t = Timer(None, addTimer.title, Some(new Timestamp(start.getMillis)), Some(new Timestamp(stop.getMillis)),addTimer.desc)
        (for{
          id <- timerDAO.insert(t) if id.isDefined
          tt <- timerDAO.get(id.get)
        } yield(ok("addTimer", Some(Json.toJson(tt.get))))).recover{case e => bad("addTimer", Some(JsError.toJson(JsError(e.getMessage))))}
      }
    )
  }



  private def success(message: String, content: Option[JsValue] = None): JsValue = {
    Json.toJson( ApiResponse(ApiContainers.SUCESS, Some(message), content))
  }

  def error(message: String, content: Option[JsValue] = None): JsValue = {
    Json.toJson( ApiResponse(ApiContainers.ERROR, Some(message), content))
  }

  def succ(message: String, content: Option[JsValue] = None)(f: JsValue => Result): Result = {
    Logger.info(message)
    f(success(message, content))
  }

  def fail(message: String, content: Option[JsValue] = None)(f: JsValue => Result): Result = {
    Logger.info(message)
    f(error(message, content))
  }

  def bad(message: String, content: Option[JsValue] = None): Result =
    fail(message, content)(f => BadRequest(f))

  def ok(message: String, content: Option[JsValue] = None): Result =
    succ(message, content)(f => Ok(Json.prettyPrint(f)).as(JSON))


}

object ApiContainers {


  val SUCESS = "success"
  val ERROR = "error"

  //def timestampToDateTime(t: Timestamp): DateTime = new DateTime(t.getTime)

  //def dateTimeToTimestamp(dt: DateTime): Timestamp = new Timestamp(dt.getMillis)

  case class AddTimer(title: String, start: Option[DateTime] = None, stop: Option[DateTime] = None, seconds: Option[Int] = None, desc: Option[String] = None)

  case class ApiResponse(status: String = SUCESS, message: Option[String] = None, value: Option[JsValue] = None)


}
