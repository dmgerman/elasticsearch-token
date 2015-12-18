begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.http.netty.pipelining
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|pipelining
package|;
end_package

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_comment
comment|// this file is from netty-http-pipelining, under apache 2.0 license
end_comment

begin_comment
comment|// see github.com/typesafehub/netty-http-pipelining
end_comment

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|DefaultHttpRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpRequest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Implements HTTP pipelining ordering, ensuring that responses are completely served in the same order as their  * corresponding requests. NOTE: A side effect of using this handler is that upstream HttpRequest objects will  * cause the original message event to be effectively transformed into an OrderedUpstreamMessageEvent. Conversely  * OrderedDownstreamChannelEvent objects are expected to be received for the correlating response objects.  *  * @author Christopher Hunt  */
end_comment

begin_class
DECL|class|HttpPipeliningHandler
specifier|public
class|class
name|HttpPipeliningHandler
extends|extends
name|SimpleChannelHandler
block|{
DECL|field|INITIAL_EVENTS_HELD
specifier|public
specifier|static
specifier|final
name|int
name|INITIAL_EVENTS_HELD
init|=
literal|3
decl_stmt|;
DECL|field|maxEventsHeld
specifier|private
specifier|final
name|int
name|maxEventsHeld
decl_stmt|;
DECL|field|sequence
specifier|private
name|int
name|sequence
decl_stmt|;
DECL|field|nextRequiredSequence
specifier|private
name|int
name|nextRequiredSequence
decl_stmt|;
DECL|field|nextRequiredSubsequence
specifier|private
name|int
name|nextRequiredSubsequence
decl_stmt|;
DECL|field|holdingQueue
specifier|private
specifier|final
name|Queue
argument_list|<
name|OrderedDownstreamChannelEvent
argument_list|>
name|holdingQueue
decl_stmt|;
comment|/**      * @param maxEventsHeld the maximum number of channel events that will be retained prior to aborting the channel      *                      connection. This is required as events cannot queue up indefinitely; we would run out of      *                      memory if this was the case.      */
DECL|method|HttpPipeliningHandler
specifier|public
name|HttpPipeliningHandler
parameter_list|(
specifier|final
name|int
name|maxEventsHeld
parameter_list|)
block|{
name|this
operator|.
name|maxEventsHeld
operator|=
name|maxEventsHeld
expr_stmt|;
name|holdingQueue
operator|=
operator|new
name|PriorityQueue
argument_list|<>
argument_list|(
name|INITIAL_EVENTS_HELD
argument_list|,
operator|new
name|Comparator
argument_list|<
name|OrderedDownstreamChannelEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|OrderedDownstreamChannelEvent
name|o1
parameter_list|,
name|OrderedDownstreamChannelEvent
name|o2
parameter_list|)
block|{
specifier|final
name|int
name|delta
init|=
name|o1
operator|.
name|getOrderedUpstreamMessageEvent
argument_list|()
operator|.
name|getSequence
argument_list|()
operator|-
name|o2
operator|.
name|getOrderedUpstreamMessageEvent
argument_list|()
operator|.
name|getSequence
argument_list|()
decl_stmt|;
if|if
condition|(
name|delta
operator|==
literal|0
condition|)
block|{
return|return
name|o1
operator|.
name|getSubsequence
argument_list|()
operator|-
name|o2
operator|.
name|getSubsequence
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|delta
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|getMaxEventsHeld
specifier|public
name|int
name|getMaxEventsHeld
parameter_list|()
block|{
return|return
name|maxEventsHeld
return|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|ChannelHandlerContext
name|ctx
parameter_list|,
specifier|final
name|MessageEvent
name|e
parameter_list|)
block|{
specifier|final
name|Object
name|msg
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|msg
operator|instanceof
name|HttpRequest
condition|)
block|{
name|ctx
operator|.
name|sendUpstream
argument_list|(
operator|new
name|OrderedUpstreamMessageEvent
argument_list|(
name|sequence
operator|++
argument_list|,
name|e
operator|.
name|getChannel
argument_list|()
argument_list|,
name|msg
argument_list|,
name|e
operator|.
name|getRemoteAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ctx
operator|.
name|sendUpstream
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|handleDownstream
specifier|public
name|void
name|handleDownstream
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ChannelEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|e
operator|instanceof
name|OrderedDownstreamChannelEvent
condition|)
block|{
name|boolean
name|channelShouldClose
init|=
literal|false
decl_stmt|;
synchronized|synchronized
init|(
name|holdingQueue
init|)
block|{
if|if
condition|(
name|holdingQueue
operator|.
name|size
argument_list|()
operator|<
name|maxEventsHeld
condition|)
block|{
specifier|final
name|OrderedDownstreamChannelEvent
name|currentEvent
init|=
operator|(
name|OrderedDownstreamChannelEvent
operator|)
name|e
decl_stmt|;
name|holdingQueue
operator|.
name|add
argument_list|(
name|currentEvent
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|holdingQueue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|OrderedDownstreamChannelEvent
name|nextEvent
init|=
name|holdingQueue
operator|.
name|peek
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextEvent
operator|.
name|getOrderedUpstreamMessageEvent
argument_list|()
operator|.
name|getSequence
argument_list|()
operator|!=
name|nextRequiredSequence
operator||
name|nextEvent
operator|.
name|getSubsequence
argument_list|()
operator|!=
name|nextRequiredSubsequence
condition|)
block|{
break|break;
block|}
name|holdingQueue
operator|.
name|remove
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|sendDownstream
argument_list|(
name|nextEvent
operator|.
name|getChannelEvent
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|nextEvent
operator|.
name|isLast
argument_list|()
condition|)
block|{
operator|++
name|nextRequiredSequence
expr_stmt|;
name|nextRequiredSubsequence
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
operator|++
name|nextRequiredSubsequence
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|channelShouldClose
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|channelShouldClose
condition|)
block|{
name|Channels
operator|.
name|close
argument_list|(
name|e
operator|.
name|getChannel
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|super
operator|.
name|handleDownstream
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

