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
package com.art4ul.raft.actor

import akka.actor.{FSM, Props}
import com.art4ul.raft.client.Client
import com.art4ul.raft.state._

class RaftActor(override val id: String, override val servers: Map[String, Client])
  extends FSM[State, Data] with CommonRaft with FollowerLogic with CandidateLogic with LeaderLogic {

  startWith(Follower, Data())

  onTransition {
    case Follower -> Candidate =>
      logTransition(Follower, Candidate)
      startElection(nextStateData.currentTerm)

    case Candidate -> Leader =>
      logTransition(Candidate, Leader)
      sendHeartBeat(nextStateData.currentTerm)
  }

  initialize()

}

object RaftActor {
  def props(id: String, servers: Map[String, Client]) = Props(classOf[RaftActor], id, servers)
}
