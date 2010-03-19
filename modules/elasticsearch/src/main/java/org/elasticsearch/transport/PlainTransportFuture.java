begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchInterruptedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|stream
operator|.
name|Streamable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|PlainTransportFuture
specifier|public
class|class
name|PlainTransportFuture
parameter_list|<
name|V
extends|extends
name|Streamable
parameter_list|>
implements|implements
name|TransportFuture
argument_list|<
name|V
argument_list|>
implements|,
name|TransportResponseHandler
argument_list|<
name|V
argument_list|>
block|{
DECL|field|latch
specifier|private
specifier|final
name|CountDownLatch
name|latch
decl_stmt|;
DECL|field|handler
specifier|private
specifier|final
name|TransportResponseHandler
argument_list|<
name|V
argument_list|>
name|handler
decl_stmt|;
DECL|field|done
specifier|private
specifier|volatile
name|boolean
name|done
decl_stmt|;
DECL|field|canceled
specifier|private
specifier|volatile
name|boolean
name|canceled
decl_stmt|;
DECL|field|result
specifier|private
specifier|volatile
name|V
name|result
decl_stmt|;
DECL|field|exp
specifier|private
specifier|volatile
name|Exception
name|exp
decl_stmt|;
DECL|method|PlainTransportFuture
specifier|public
name|PlainTransportFuture
parameter_list|(
name|TransportResponseHandler
argument_list|<
name|V
argument_list|>
name|handler
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|latch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|cancel
annotation|@
name|Override
specifier|public
name|boolean
name|cancel
parameter_list|(
name|boolean
name|mayInterruptIfRunning
parameter_list|)
block|{
if|if
condition|(
name|done
condition|)
return|return
literal|true
return|;
name|canceled
operator|=
literal|true
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|isCancelled
annotation|@
name|Override
specifier|public
name|boolean
name|isCancelled
parameter_list|()
block|{
return|return
name|canceled
return|;
block|}
DECL|method|isDone
annotation|@
name|Override
specifier|public
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|done
return|;
block|}
DECL|method|get
annotation|@
name|Override
specifier|public
name|V
name|get
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|done
operator|||
name|canceled
condition|)
block|{
throw|throw
operator|new
name|InterruptedException
argument_list|(
literal|"future was interrupted"
argument_list|)
throw|;
block|}
if|if
condition|(
name|exp
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ExecutionException
argument_list|(
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
name|exp
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|result
return|;
block|}
DECL|method|get
annotation|@
name|Override
specifier|public
name|V
name|get
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
name|latch
operator|.
name|await
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|done
operator|||
name|canceled
condition|)
block|{
throw|throw
operator|new
name|TimeoutException
argument_list|(
literal|"response did not arrive"
argument_list|)
throw|;
block|}
if|if
condition|(
name|exp
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ExecutionException
argument_list|(
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
name|exp
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|result
return|;
block|}
DECL|method|txGet
annotation|@
name|Override
specifier|public
name|V
name|txGet
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
try|try
block|{
return|return
name|get
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchInterruptedException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ElasticSearchException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|TransportException
argument_list|(
literal|"Failed execution"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|txGet
annotation|@
name|Override
specifier|public
name|V
name|txGet
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|ElasticSearchException
throws|,
name|TimeoutException
block|{
try|try
block|{
return|return
name|get
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchInterruptedException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ElasticSearchException
condition|)
block|{
throw|throw
operator|(
name|ElasticSearchException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|TransportException
argument_list|(
literal|"Failed execution"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|V
name|newInstance
parameter_list|()
block|{
return|return
name|handler
operator|.
name|newInstance
argument_list|()
return|;
block|}
DECL|method|handleResponse
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|V
name|response
parameter_list|)
block|{
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|response
expr_stmt|;
if|if
condition|(
name|canceled
condition|)
return|return;
name|handler
operator|.
name|handleResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
DECL|method|handleException
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|exp
operator|=
name|exp
expr_stmt|;
if|if
condition|(
name|canceled
condition|)
return|return;
name|handler
operator|.
name|handleException
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
DECL|method|spawn
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
return|return
name|handler
operator|.
name|spawn
argument_list|()
return|;
block|}
block|}
end_class

end_unit

