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

import com.art4ul.scrafty.protocol.{Request, Response}
import com.art4ul.scrafty.utils.Disposable._
import com.twitter.chill.{Input, Output, ScalaKryoInstantiator}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by artsemsemianenka on 3/30/16.
  */
trait Client {
  def send(request: Request): Future[Response]
}

import com.art4ul.raft.client.TcpClient._

class TcpClient(host: String, port: Int, timeOut: Int = DefaultSocketTimeout)
               (implicit executionContext: ExecutionContext) extends Client {

  def send(request: Request): Future[Response] = {
    val instantiator = new ScalaKryoInstantiator().setRegistrationRequired(false)
    val kryo = instantiator.newKryo()
    Future(using(new Socket(InetAddress.getByName(host), port)) {
      socket =>
        socket.setSoTimeout(timeOut)
        val output = new Output(socket.getOutputStream)
        kryo.writeClassAndObject(output, request)
        output.flush()
        val input = new Input(socket.getInputStream)
        val result = kryo.readClassAndObject(input)
        input.close()
        output.close()
        result.asInstanceOf[Response]
    })
  }
}

object TcpClient {
  val DefaultSocketTimeout = 5000
}



