package bot

import java.time.{ LocalDate, LocalDateTime }

import io.getquill._
import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom

// コンパイルエラーになる
//class AttendanceService[Dialect <: SqlIdiom, Naming <: NamingStrategy](implicit ctx: JdbcContext[Dialect, Naming]) {
class AttendanceService(implicit ctx: H2JdbcContext[SnakeCase]) {
  import ctx._
  import bot.AttendanceDAO._

  def start(userId: String, userName: String)(implicit dateHelper: DateHelper): Option[Attendance] = {
    val item = Attendance(
      id = None,
      attDate = dateHelper.currentTime.toLocalDate,
      attDateTime = Some(dateHelper.currentTime),
      attType = AttType.Start,
      userId = userId,
      userName = userName,
      breakTimeMin = None,
      createdAt = dateHelper.currentTime
    )
    ctx.transaction {
      val oid = ctx.run(AttendanceDAO.insert(item))
      oid.map(id => item.copy(id = Some(id)))
    }
  }

  def finish(userId: String, userName: String)(implicit dateHelper: DateHelper): Option[Attendance] = {
    val item = Attendance(
      id = None,
      attDate = dateHelper.currentTime.toLocalDate,
      attDateTime = Some(dateHelper.currentTime),
      attType = AttType.Finish,
      userId = userId,
      userName = userName,
      breakTimeMin = None,
      createdAt = dateHelper.currentTime
    )
    ctx.transaction {
      val oid = ctx.run(AttendanceDAO.insert(item))
      oid.map(id => item.copy(id = Some(id)))
    }
  }

  def break(userId: String, userName: String, breakTimeMin: Int)(implicit dateHelper: DateHelper): Option[Attendance] = {
    val item = Attendance(
      id = None,
      attDate = dateHelper.currentTime.toLocalDate,
      attDateTime = None,
      attType = AttType.Break,
      userId = userId,
      userName = userName,
      breakTimeMin = Some(breakTimeMin),
      createdAt = dateHelper.currentTime
    )
    ctx.transaction {
      val oid = ctx.run(AttendanceDAO.insert(item))
      oid.map(id => item.copy(id = Some(id)))
    }
  }

  def getWorkingTimeMin(userId: String, data: LocalDate): Option[Long] = {
    val startResults = ctx.run(
      AttendanceDAO.find(userId, data, AttType.Start)
    )
    val finishResults = ctx.run(
      AttendanceDAO.find(userId, data, AttType.Finish)
    )
    val breakResult = ctx.run(
      AttendanceDAO.find(userId, data, AttType.Break)
    )

    for {
      startResult <- startResults.headOption
      finishResult <- finishResults.headOption
      startTime <- startResult.attDateTime
      finishTime <- finishResult.attDateTime
    } yield {
      val between = java.time.Duration.between(startTime, finishTime)
      (between.getSeconds / 60) - breakResult.headOption.fold(0)(x => x.breakTimeMin.getOrElse(0))
    }
  }
}
