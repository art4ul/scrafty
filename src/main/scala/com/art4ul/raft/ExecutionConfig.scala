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

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Created by artsemsemianenka on 3/29/16.
  */
object ExecutionConfig {
  private val config = ConfigFactory.load()
  val servers: java.util.List[String] = Try(config.getStringList("servers")).getOrElse(Nil)
  val host = config.getString("host")
  val port = config.getInt("port")

  val socketTimeout = 5000

  val electionTimeoutMs = 3000

  val electionPeriodMs = 3000

  val heartBeatTimeoutMs = 300

}
