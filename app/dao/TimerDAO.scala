package dao

import java.sql.Timestamp
import javax.inject.Inject

import com.google.inject.Singleton
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.Future

/**
  * @author msvens
  * @since 21/07/16
  */
@Singleton
class TimerDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class TimerTable(tag: Tag) extends Table[Timer](tag, "timer"){
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def start = column[Option[Timestamp]]("start")
    def stop = column[Option[Timestamp]]("stop")
    def desc = column[Option[String]]("description")
    def * = (id,title,start,stop,desc) <> (Timer.tupled, Timer.unapply _)
  }

  private val timers = TableQuery[TimerTable]

  def insert(t: Timer): Future[Option[Int]] = db.run(timers returning timers.map(_.id) += t)

  def get(id: Int): Future[Option[Timer]] = {
    val q = timers.filter(_.id === id)
    db.run(q.result.headOption)
  }

  def get(t: String): Future[Option[Timer]] = {
    val q = timers.filter(_.title === t)
    db.run(q.result.headOption)
  }

  def list: Future[Seq[Timer]] = {
    val q = for(t <- timers) yield t
    db.run(q.result)
  }



}
