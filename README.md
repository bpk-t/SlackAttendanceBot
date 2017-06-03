# SlackAttendanceBot
勤怠bot

# test

```
sbt
flywayMigrate
test
```

# 準備

## db定義作成

```
sbt
flywayMigrate
```

## Slack botに登録する
https://my.slack.com/services/new/bot

## tokenを設定ファイルに記述

src\main\resources\application.properties

```
bot.slack.token=ここに設定
```

# daemon
## 起動

// TODO 
## 終了

// TODO

# 使い方
// TODO
## start
## finish
## break
