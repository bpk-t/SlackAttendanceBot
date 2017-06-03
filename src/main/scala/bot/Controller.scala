package bot

import akka.actor.ActorSystem
import io.getquill.{ H2JdbcContext, SnakeCase }
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

class Controller {
  implicit val system = ActorSystem("slack")
  import com.typesafe.config.ConfigFactory

  val token = ConfigFactory.load().getString("bot.slack.token")

  implicit lazy val ctx: H2JdbcContext[SnakeCase] = new H2JdbcContext[SnakeCase]("ctx")
  implicit lazy val dateHelper = new DateHelperImpl()

  val service: AttendanceService = new AttendanceService
  var client: Option[SlackRtmClient] = None

  def init(): Unit = {
    client = Option(SlackRtmClient(token))

    client.foreach { c =>
      c.onMessage { message => onMessage(c, message) }
    }
  }

  def close(): Unit = {
    client.foreach { c =>
      c.close()
    }
    ctx.close()
  }

  private def onMessage(client: SlackRtmClient, message: Message): Unit = {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    if (mentionedIds.contains(client.state.self.id)) {
      // 自分宛て
      toMeMessage(client, message)
    }
  }

  private def toMeMessage(client: SlackRtmClient, message: Message): Unit = {
    val userName = client.state.getUserById(message.user).fold("")(_.name)
    val splits = message.text.split(" ")
    if (splits.length >= 2) {
      if (splits(1).startsWith("start")) {
        service.start(message.user, userName) match {
          case Some(r) => client.sendMessage(message.channel, s"start user=${userName}, timestamp=${r.attDateTime}")
          case None => client.sendMessage(message.channel, "error")
        }
      } else if (splits(1).startsWith("finish")) {
        service.finish(message.user, userName) match {
          case Some(r) => client.sendMessage(message.channel, s"finish user=${userName}, timestamp=${r.attDateTime}")
          case None => client.sendMessage(message.channel, "error")
        }
      } else if (splits(1).startsWith("break")) {
        import scalaz._
        import Scalaz._
        if (splits.length >= 3) {
          val ret = for {
            time <- splits(2).parseInt.toOption
            ret <- service.break(message.user, userName, time)
          } yield (ret, time)

          ret match {
            case Some((ret, time)) => client.sendMessage(message.channel, s"break user=${userName}, time=${time}")
            case None => client.sendMessage(message.channel, "error")
          }
        } else {
          client.sendMessage(message.channel, "error")
        }
      } else if (splits(1).startsWith("today")) {
        service.getWorkingTimeMin(message.user, dateHelper.currentTime.toLocalDate) match {
          case Some(time) => client.sendMessage(message.channel, s"working time user=${userName}, work time=${time}min")
          case None => client.sendMessage(message.channel, "startかfinishが無いです")
        }
      } else {
        // error
        client.sendMessage(message.channel, "Command Error!!!")
      }
    } else {
      // error
      client.sendMessage(message.channel, "Command Error!!!")
    }
  }
}
