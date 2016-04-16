package com.art4ul.scrafty.client

import com.art4ul.raft.client.TcpClient
import com.art4ul.scrafty.protocol.NewCommandRequest
import com.art4ul.scrafty.utils.UrlUtil
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by artsemsemianenka on 4/16/16.
  */
object Main extends App {
  require(args.length == 2, "Input <host:port> <command>")

  val (host, port) = UrlUtil.parseHostAndPort(args(0))
  val command = args(1)

  val client = new TcpClient(host,port)
  println(s"Send :: [$command] -> ($host:$port)")
  val result = Await.result(client.send(new NewCommandRequest(command)),10 seconds)
  println(s"Result :: $result")
}
