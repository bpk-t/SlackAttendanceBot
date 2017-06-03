package bot

import java.time.{ LocalDate, LocalDateTime }

import io.getquill.context.jdbc.JdbcContext
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.{ MappedEncoding, NamingStrategy }

object AttendanceDAO {

  implicit lazy val encoderAttType = MappedEncoding[AttType.AttType, String] {
    case AttType.Start => "start"
    case AttType.Finish => "finish"
    case AttType.Break => "break"
  }

  implicit lazy val decoderAttType = MappedEncoding[String, AttType.AttType] {
    case "start" => AttType.Start
    case "finish" => AttType.Finish
    case "break" => AttType.Break
  }

  def insert[Dialect <: SqlIdiom, Naming <: NamingStrategy](param: Attendance)(implicit ctx: JdbcContext[Dialect, Naming]) = {
    import ctx._
    quote {
      query[Attendance].insert(lift(param)).returning(x => x.id)
    }
  }

  def findById[Dialect <: SqlIdiom, Naming <: NamingStrategy](id: Int)(implicit ctx: JdbcContext[Dialect, Naming]) = {
    import ctx._
    quote {
      query[Attendance].filter(a => a.id == lift(Some(id)))
    }
  }

  def find[Dialect <: SqlIdiom, Naming <: NamingStrategy](userId: String, attDate: LocalDate, attType: AttType.AttType)(implicit ctx: JdbcContext[Dialect, Naming]) = {
    import ctx._
    quote {
      query[Attendance]
        .filter(a => a.userId == lift(userId) && a.attDate == lift(attDate) && a.attType == lift(attType: AttType.AttType))
        .sortBy(a => a.id)(Ord.desc)
        .take(1)
    }
  }
}

object AttType {
  sealed trait AttType
  case object Start extends AttType
  case object Finish extends AttType
  case object Break extends AttType
}

case class Attendance(
  id: Option[Int],
  attDate: LocalDate,
  attDateTime: Option[LocalDateTime],
  attType: AttType.AttType,
  userId: String,
  userName: String,
  breakTimeMin: Option[Int],
  createdAt: LocalDateTime
)

