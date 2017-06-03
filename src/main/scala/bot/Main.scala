package bot

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

object Main extends App {

  val controller = new Controller()
  controller.init()
  Await.ready(Future.never, Duration.Inf)
}
