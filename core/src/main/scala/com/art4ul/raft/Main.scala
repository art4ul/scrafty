/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.art4ul.raft

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.Tcp.Bind
import akka.io.{IO, Tcp}
import com.art4ul.raft.actor._
import com.art4ul.raft.client.{Client, TcpClient}
import com.art4ul.raft.server.{ConnectionHandlerActor, ServerActor}
import com.art4ul.scrafty.utils.UrlUtil
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by artsemsemianenka on 3/29/16.
  */
object Main extends App {
  val serverId = s"${ExecutionConfig.host}:${ExecutionConfig.port}"
  println(s"Start $serverId")
  implicit val actorSystem = ActorSystem("raft-actor-system")

  val servers: Map[String, Client] = ExecutionConfig.servers.map { line =>
    val (host, port) = UrlUtil.parseHostAndPort(line)
    line -> new TcpClient(host, port)
  }.toMap

  val raftActor = actorSystem.actorOf(RaftActor.props(serverId, servers))
  val connectionHandler = ConnectionHandlerActor.props(raftActor)
  val serverRef = actorSystem.actorOf(ServerActor.props(connectionHandler))

  IO(Tcp) ! Bind(serverRef, new InetSocketAddress("localhost", ExecutionConfig.port))
}
