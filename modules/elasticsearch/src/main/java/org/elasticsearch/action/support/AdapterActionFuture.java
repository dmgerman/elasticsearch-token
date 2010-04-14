begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|ElasticSearchTimeoutException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionFuture
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportException
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
name|TimeValue
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
name|concurrent
operator|.
name|AbstractFuture
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AdapterActionFuture
specifier|public
specifier|abstract
class|class
name|AdapterActionFuture
parameter_list|<
name|T
parameter_list|,
name|L
parameter_list|>
extends|extends
name|AbstractFuture
argument_list|<
name|T
argument_list|>
implements|implements
name|ActionFuture
argument_list|<
name|T
argument_list|>
implements|,
name|ActionListener
argument_list|<
name|L
argument_list|>
block|{
DECL|method|actionGet
annotation|@
name|Override
specifier|public
name|T
name|actionGet
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
DECL|method|actionGet
annotation|@
name|Override
specifier|public
name|T
name|actionGet
parameter_list|(
name|String
name|timeout
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|actionGet
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
DECL|method|actionGet
annotation|@
name|Override
specifier|public
name|T
name|actionGet
parameter_list|(
name|long
name|timeoutMillis
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|actionGet
argument_list|(
name|timeoutMillis
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
DECL|method|actionGet
annotation|@
name|Override
specifier|public
name|T
name|actionGet
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|actionGet
argument_list|(
name|timeout
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
DECL|method|actionGet
annotation|@
name|Override
specifier|public
name|T
name|actionGet
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|ElasticSearchException
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
name|TimeoutException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchTimeoutException
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
name|ElasticSearchException
argument_list|(
literal|"Failed execution"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|onResponse
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|L
name|result
parameter_list|)
block|{
name|set
argument_list|(
name|convert
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|onFailure
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
DECL|method|convert
specifier|protected
specifier|abstract
name|T
name|convert
parameter_list|(
name|L
name|listenerResponse
parameter_list|)
function_decl|;
block|}
end_class

end_unit

