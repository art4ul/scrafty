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

package com.art4ul.raft.client

import java.net.{InetAddress, Socket}

import com.art4ul.raft.ExecutionConfig
import com.art4ul.raft.protocol.Response
import com.art4ul.raft.state.ProtoConverter._
import com.art4ul.raft.state.{RaftRequest, RaftResponse}
import com.art4ul.raft.utils.Manageable._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by artsemsemianenka on 3/30/16.
  */
object Client {

  def apply(host: String, port: Int = ExecutionConfig.port)(request: RaftRequest): Future[RaftResponse] = {
    Future(using(new Socket(InetAddress.getByName(host), port)) { socket =>
      socket.setSoTimeout(ExecutionConfig.socketTimeout)
      val output = socket.getOutputStream()
      output.write(request.toProto.toByteArray)
      val input = socket.getInputStream()
      val result = Response.parseFrom(input)
      result.toRaftMessage
    })
  }


}