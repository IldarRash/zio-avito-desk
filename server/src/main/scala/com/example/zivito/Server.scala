package com.example.zivito

import zio.Console.{printLine, readLine}
import zio._

object Server extends zio.ZIOAppDefault{

  val greeting = for {
    _ <- Console.printLine("what your name?")
    name <- Console.readLine
    _ <- Console.printLine(s"My name is ${name}")
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = greeting

}
