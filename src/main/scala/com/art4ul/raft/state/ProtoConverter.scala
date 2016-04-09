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


import com.art4ul.raft.protocol._

/**
  * Created by artsemsemianenka on 4/9/16.
  */
object ProtoConverter {

  implicit class RaftResponseConversion(response: RaftResponse) {

    def toProto: Response = response match {
      case v: AppendEntryResponse => Response(v.term).withAppendEntryResult(AppendEntryResult(v.result))
      case v: VoteResponse =>Response(v.term).withRequestVoteResult(RequestVoteResult(v.result))
    }
  }

  implicit class RaftRequestConversion(request: RaftRequest) {
    def toProto: Request = request match {
      case v: AppendEntryRequest => Request(v.term).withAppendEntry(AppendEntry(v.leaderId))
      case v: VoteRequest => Request(v.term).withRequestVote(RequestVote(v.candidateId))
    }
  }

  implicit class ProtoRequestConversion(request: Request) {
    def toRaftMessage: RaftRequest = request.`type` match {
      case Request.Type.AppendEntry(value) => AppendEntryRequest(request.term, value.leaderId)
      case Request.Type.RequestVote(value) => VoteRequest(request.term, value.candidateId)
    }
  }

  implicit class ProtoResponseConversion(response: Response) {
    def toRaftMessage: RaftResponse = response.`type` match {
      case Response.Type.AppendEntryResult(value) => AppendEntryResponse(response.term, value.result)
      case Response.Type.RequestVoteResult(value) => VoteResponse(response.term, value.result)
    }
  }

}
