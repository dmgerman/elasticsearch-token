begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchTimeoutException
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
name|common
operator|.
name|unit
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|BaseFuture
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
name|util
operator|.
name|concurrent
operator|.
name|UncategorizedExecutionException
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
comment|/**  *  */
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
name|BaseFuture
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
DECL|field|rootFailure
specifier|private
name|Throwable
name|rootFailure
decl_stmt|;
annotation|@
name|Override
DECL|method|actionGet
specifier|public
name|T
name|actionGet
parameter_list|()
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
name|IllegalStateException
argument_list|(
literal|"Future got interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
name|rethrowExecutionException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|actionGet
specifier|public
name|T
name|actionGet
parameter_list|(
name|String
name|timeout
parameter_list|)
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
argument_list|,
literal|"AdapterActionFuture.actionGet.timeout"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|actionGet
specifier|public
name|T
name|actionGet
parameter_list|(
name|long
name|timeoutMillis
parameter_list|)
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
annotation|@
name|Override
DECL|method|actionGet
specifier|public
name|T
name|actionGet
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
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
annotation|@
name|Override
DECL|method|actionGet
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
name|ElasticsearchTimeoutException
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
name|IllegalStateException
argument_list|(
literal|"Future got interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
name|rethrowExecutionException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|rethrowExecutionException
specifier|static
name|RuntimeException
name|rethrowExecutionException
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
name|ElasticsearchException
condition|)
block|{
name|ElasticsearchException
name|esEx
init|=
operator|(
name|ElasticsearchException
operator|)
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|Throwable
name|root
init|=
name|esEx
operator|.
name|unwrapCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|root
operator|instanceof
name|ElasticsearchException
condition|)
block|{
return|return
operator|(
name|ElasticsearchException
operator|)
name|root
return|;
block|}
elseif|else
if|if
condition|(
name|root
operator|instanceof
name|RuntimeException
condition|)
block|{
return|return
operator|(
name|RuntimeException
operator|)
name|root
return|;
block|}
return|return
operator|new
name|UncategorizedExecutionException
argument_list|(
literal|"Failed execution"
argument_list|,
name|root
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|RuntimeException
condition|)
block|{
return|return
operator|(
name|RuntimeException
operator|)
name|e
operator|.
name|getCause
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|UncategorizedExecutionException
argument_list|(
literal|"Failed execution"
argument_list|,
name|e
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|onResponse
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
annotation|@
name|Override
DECL|method|onFailure
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
annotation|@
name|Override
DECL|method|getRootFailure
specifier|public
name|Throwable
name|getRootFailure
parameter_list|()
block|{
return|return
name|rootFailure
return|;
block|}
block|}
end_class

end_unit

