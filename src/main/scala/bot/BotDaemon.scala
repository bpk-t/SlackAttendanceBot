package bot

import org.apache.commons.daemon.{ Daemon, DaemonContext }

class BotDaemon extends Daemon {
  var controller: Option[Controller] = None

  override def init(context: DaemonContext): Unit = {
    controller = Some(new Controller())
    controller.foreach(_.init())
  }

  override def start(): Unit = {

  }

  override def stop(): Unit = {
    controller.foreach(_.close())
  }

  override def destroy(): Unit = {

  }
}