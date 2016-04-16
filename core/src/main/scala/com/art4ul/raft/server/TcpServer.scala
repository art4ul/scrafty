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

package com.art4ul.raft.server

import java.io.ByteArrayOutputStream

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString
import com.art4ul.scrafty.protocol.Response
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator

import scala.util.{Failure, Success, Try}

/**
  * Created by artsemsemianenka on 3/29/16.
  */
class ServerActor(handlerProps: Props) extends Actor {

  override def receive = {
    //case b@Bound(localAddress) =>

    case CommandFailed(_: Bind) => context stop self

    case Connected(remote, local) =>
      val handler = context.actorOf(handlerProps)
      val connection = sender()
      connection ! Register(handler)
  }
}

object ServerActor {
  def props(handlerProps: Props) = Props(classOf[ServerActor], handlerProps)
}

import com.art4ul.scrafty.utils.Disposable._

class ConnectionHandlerActor(raftActor: ActorRef) extends Actor {

  var connection: Option[ActorRef] = None
  val instantiator = new ScalaKryoInstantiator().setRegistrationRequired(false)
  val kryo = instantiator.newKryo()

  override def receive = {
    case Received(data) =>
      using(new Input(data.toByteBuffer.array)) { input =>
        Try(kryo.readClassAndObject(input)) match {
          case Success(request) =>
            connection = Some(sender)
            raftActor ! request

          case Failure(ex) =>
            ex.printStackTrace()
            sender ! Close
        }
      }

    case resp: Response => connection.foreach { connectionRef =>
      using(new Output(new ByteArrayOutputStream())) { output =>
        kryo.writeClassAndObject(output, resp)
        connectionRef ! Write(ByteString(output.toBytes))
//        output.flush()
        connectionRef ! Close
      }
    }

    case PeerClosed => context stop self
  }
}

object ConnectionHandlerActor {
  def props(raftActor: ActorRef) = Props(classOf[ConnectionHandlerActor], raftActor)
}

