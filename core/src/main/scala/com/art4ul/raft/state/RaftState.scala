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

package com.art4ul.raft.state

import akka.actor.ActorRef

/**
  * Created by artsemsemianenka on 4/9/16.
  */
// State
trait State

case object Leader extends State

case object Follower extends State

case object Candidate extends State


// Data
case class Data(currentTerm: Int = 1,
                log: IndexedSeq[CommandModel] = IndexedSeq(),
                votedFor: Option[String] = None,
                commitIndex: Int = 0,
                lastApplied: Int = 0,
                votes: Int = 0)


//case class FollowerData(override val currentTerm: Int = 1,
//                        leader: Option[String] = None,
//                        votedFor: Option[String] = None) extends Data
//
//case class CandidateData(override val currentTerm: Int,
//                         votes: Int) extends Data


case class CommandModel(term: Int, command: String, connectionRef: ActorRef, commited: Boolean = false)

//case class LeaderData(override val currentTerm: Int,
//                      log: IndexedSeq[CommandModel] = IndexedSeq(),
//                      commitIndex: Int = 0,
//                      lastApplied: Int = 0) extends Data
