package bot

import java.time.LocalDateTime

trait DateHelper {
  def currentTime: LocalDateTime
}

class DateHelperImpl extends DateHelper {
  override def currentTime: LocalDateTime = LocalDateTime.now()
}
