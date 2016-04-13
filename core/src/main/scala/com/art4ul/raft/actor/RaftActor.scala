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

import java.util.concurrent.TimeUnit

import akka.actor.{FSM, Props}
import com.art4ul.raft.ExecutionConfig
import com.art4ul.raft.state._
import com.art4ul.raft.utils.StateLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class RaftActor(id: String, servers: Map[String, (RaftRequest) => Future[RaftResponse]]) extends FSM[State, Data] with StateLogger {

  val heartBeatTimeout = FiniteDuration(ExecutionConfig.heartBeatTimeoutMs, TimeUnit.MILLISECONDS)
  val electionPeriod = FiniteDuration(ExecutionConfig.electionPeriodMs, TimeUnit.MILLISECONDS)

  startWith(Follower, FollowerData())

  when(Follower, electionTimeout) {
    case Event(msg: AppendEntryRequest, data: FollowerData) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term == data.currentTerm) => stay -> AppendEntryResponse(data.currentTerm, true)
        case term if (term >= data.currentTerm) => stay.using(FollowerData(term, Some(msg.leaderId))) -> AppendEntryResponse(term, true)
        case _ => stay() -> AppendEntryResponse(data.currentTerm, false)
      }

    case Event(msg@StateTimeout, data: FollowerData) =>
      logState(s"message:$msg")
      goto(Candidate) using CandidateData(data.currentTerm + 1, 1)

    case Event(msg: VoteRequest, data: FollowerData) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) || (term == data.currentTerm && data.votedFor.isEmpty) =>
          stay.using(FollowerData(term, votedFor = Some(msg.candidateId))) -> VoteResponse(term, true)

        case _ => stay -> VoteResponse(data.currentTerm, false)
      }
  }

  when(Candidate, stateTimeout = electionPeriod) {
    case Event(msg@StateTimeout, data: CandidateData) =>
      logState(s"message:$msg")
      if (isVotesReached(data.votes)) {
        goto(Leader) using (LeaderData(data.currentTerm))
      } else {
        goto(Follower).forMax(electionTimeout) using FollowerData(data.currentTerm)
      }

    case Event(voteResponse: VoteResponse, data: CandidateData) =>
      logState(s"message:$voteResponse")
      val votes = if (voteResponse.result) data.votes + 1 else data.votes
      if (isVotesReached(votes)) {
        goto(Leader) using (LeaderData(data.currentTerm))
      } else {
        stay using data.copy(votes = votes)
      }
  }

  when(Leader, stateTimeout = heartBeatTimeout) {
    case Event(msg@StateTimeout, data: LeaderData) =>
      logState(s"message:$msg")
      sendHeartBeat(data.currentTerm)
      stay

    case Event(response: AppendEntryResponse, data: LeaderData) =>
      logState(s"message:$response")
      stay
  }

  whenUnhandled {
    case Event(msg: AppendEntryRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) =>
          goto(Follower).using(FollowerData(term, Some(msg.leaderId))) -> AppendEntryResponse(term, true)

        case _ => stay -> AppendEntryResponse(data.currentTerm, true)
      }

    case Event(msg: VoteRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) =>
          goto(Follower).using(FollowerData(term, votedFor = Some(msg.candidateId))) -> VoteResponse(term, true)

        case _ => stay -> VoteResponse(data.currentTerm, false)
      }

    case Event(e, s) =>
      println("Received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  onTransition {
    case Follower -> Candidate =>
      logTransition(Follower, Candidate)
      startElection(nextStateData.currentTerm)

    case Candidate -> Leader =>
      logTransition(Candidate, Leader)
      sendHeartBeat(nextStateData.currentTerm)
  }

  initialize()

  def handleRequest[S, D](request: RaftRequest)(function: PartialFunction[Int, (FSM.State[S, D], RaftResponse)]): FSM.State[S, D] = {
    val (state, response) = function(request.term)
    sender ! response
    state
  }

  def isVotesReached(votes: Int) = votes >= (servers.size + 1) / 2 + 1

  def startElection(term: Int): Unit = {
    val responseHandler: RaftResponse => Unit = response => self ! response
    broadcast(VoteRequest(term, id), responseHandler)
  }

  def sendHeartBeat(term: Int): Unit = {
    val responseHandler: RaftResponse => Unit = response => self ! response
    broadcast(AppendEntryRequest(term, id), responseHandler)
  }

  def broadcast(request: RaftRequest, responseHandler: RaftResponse => Unit): Unit =
    servers.values.foreach { client =>
      client(request).onSuccess { case response => responseHandler(response) }
    }

  def electionTimeout = {
    val randomize = new Random()
    val initTimeout = ExecutionConfig.electionTimeoutMs
    val timeout = initTimeout + randomize.nextInt(initTimeout.toInt)
    println(s"electTimeout:$timeout")
    new FiniteDuration(timeout, TimeUnit.MILLISECONDS)
  }

}

object RaftActor {
  def props(id: String, servers: Map[String, (RaftRequest) => Future[RaftResponse]]) = Props(classOf[RaftActor], id, servers)
}
