package bot

import java.time.{ LocalDate, LocalDateTime }

import io.getquill._
import io.getquill.{ H2JdbcContext, SnakeCase }
import io.getquill.context.jdbc.JdbcContext
import org.scalactic._
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, FunSuite }

class AttendanceServiceTest extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll {

  //implicit lazy val ctx: JdbcContext[H2Dialect, SnakeCase] = new H2JdbcContext[SnakeCase]("ctx")
  implicit lazy val ctx: H2JdbcContext[SnakeCase] = new H2JdbcContext[SnakeCase]("ctx")
  val service: AttendanceService = new AttendanceService

  import ctx._
  import bot.AttendanceDAO._

  override def beforeEach() {
    super.beforeEach()
    import ctx._
    ctx.run {
      query[Attendance].delete
    }
  }

  override def afterEach() = {
    import ctx._
    ctx.run {
      query[Attendance].delete
    }
  }

  override def afterAll(): Unit = {
    super.afterAll()
    ctx.close()
  }

  test("start") {
    val dateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    implicit lazy val dateHelper = new DateHelper {
      override def currentTime: LocalDateTime = dateTime
    }
    val userId = "test_user"
    val userName = "test_user_name"
    service.start(userId, userName)

    val resultRow = ctx.run {
      query[Attendance]
    }

    assert(resultRow.size == 1)
    assert(resultRow.head.userId === userId)
    assert(resultRow.head.userName === userName)
    assert(resultRow.head.attType === AttType.Start)
    assert(resultRow.head.attDate === dateTime.toLocalDate)
    assert(resultRow.head.attDateTime === Some(dateTime))
    assert(resultRow.head.breakTimeMin === None)
    assert(resultRow.head.createdAt === dateTime)
  }

  test("finish") {
    val dateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    implicit lazy val dateHelper = new DateHelper {
      override def currentTime: LocalDateTime = dateTime
    }
    val userId = "test_user"
    val userName = "test_user_name"
    service.finish(userId, userName)

    val resultRow = ctx.run {
      query[Attendance]
    }

    assert(resultRow.size == 1)
    assert(resultRow.head.userId === userId)
    assert(resultRow.head.userName === userName)
    assert(resultRow.head.attType === AttType.Finish)
    assert(resultRow.head.attDate === dateTime.toLocalDate)
    assert(resultRow.head.attDateTime === Some(dateTime))
    assert(resultRow.head.breakTimeMin === None)
    assert(resultRow.head.createdAt === dateTime)
  }

  test("break") {
    val dateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    implicit lazy val dateHelper = new DateHelper {
      override def currentTime: LocalDateTime = dateTime
    }
    val userId = "test_user"
    val userName = "test_user_name"
    service.break(userId, userName, 30)

    val resultRow = ctx.run {
      query[Attendance]
    }

    assert(resultRow.size == 1)
    assert(resultRow.head.userId === userId)
    assert(resultRow.head.userName === userName)
    assert(resultRow.head.attType === AttType.Break)
    assert(resultRow.head.attDate === dateTime.toLocalDate)
    assert(resultRow.head.attDateTime === None)
    assert(resultRow.head.breakTimeMin === Some(30))
    assert(resultRow.head.createdAt === dateTime)
  }

  test("getWorkingTimeMin") {
    val startDateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    val finishDateTime = LocalDateTime.of(2017, 6, 3, 19, 0, 0)
    val userId = "test_user"
    val userName = "test_user_name"

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = startDateTime
      }
      service.start(userId, userName)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = finishDateTime
      }
      service.finish(userId, userName)
    }

    val time = service.getWorkingTimeMin(userId, startDateTime.toLocalDate)
    assert(time === Some(9 * 60))
  }

  test("getWorkingTimeMin：休憩あり") {
    val startDateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    val finishDateTime = LocalDateTime.of(2017, 6, 3, 19, 0, 0)
    val userId = "test_user"
    val userName = "test_user_name"

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = startDateTime
      }
      service.start(userId, userName)
      service.break(userId, userName, 30)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = finishDateTime
      }
      service.finish(userId, userName)
    }

    val time = service.getWorkingTimeMin(userId, startDateTime.toLocalDate)
    assert(time === Some(9 * 60 - 30))
  }

  test("getWorkingTimeMin：start複数") {
    val startDateTime1 = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    val startDateTime2 = LocalDateTime.of(2017, 6, 3, 11, 0, 0)
    val finishDateTime = LocalDateTime.of(2017, 6, 3, 19, 0, 0)
    val userId = "test_user"
    val userName = "test_user_name"

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = startDateTime1
      }
      service.start(userId, userName)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = startDateTime2
      }
      service.start(userId, userName)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = finishDateTime
      }
      service.finish(userId, userName)
    }

    val time = service.getWorkingTimeMin(userId, startDateTime1.toLocalDate)
    assert(time === Some(8 * 60))
  }

  test("getWorkingTimeMin：finish複数") {
    val startDateTime = LocalDateTime.of(2017, 6, 3, 10, 0, 0)
    val finishDateTime1 = LocalDateTime.of(2017, 6, 3, 19, 0, 0)
    val finishDateTime2 = LocalDateTime.of(2017, 6, 3, 20, 0, 0)
    val userId = "test_user"
    val userName = "test_user_name"

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = startDateTime
      }
      service.start(userId, userName)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = finishDateTime1
      }
      service.finish(userId, userName)
    }

    {
      implicit lazy val dateHelper = new DateHelper {
        override def currentTime: LocalDateTime = finishDateTime2
      }
      service.finish(userId, userName)
    }

    val time = service.getWorkingTimeMin(userId, startDateTime.toLocalDate)
    assert(time === Some(10 * 60))
  }
}
