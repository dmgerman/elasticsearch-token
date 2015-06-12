begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|metrics
operator|.
name|CounterMetric
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
name|BlockingQueue
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
name|ThreadPoolExecutor
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|EsAbortPolicy
specifier|public
class|class
name|EsAbortPolicy
implements|implements
name|XRejectedExecutionHandler
block|{
DECL|field|rejected
specifier|private
specifier|final
name|CounterMetric
name|rejected
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|field|SHUTTING_DOWN_KEY
specifier|public
specifier|static
specifier|final
name|String
name|SHUTTING_DOWN_KEY
init|=
literal|"(shutting down)"
decl_stmt|;
annotation|@
name|Override
DECL|method|rejectedExecution
specifier|public
name|void
name|rejectedExecution
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|ThreadPoolExecutor
name|executor
parameter_list|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|AbstractRunnable
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|AbstractRunnable
operator|)
name|r
operator|)
operator|.
name|isForceExecution
argument_list|()
condition|)
block|{
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|queue
init|=
name|executor
operator|.
name|getQueue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|queue
operator|instanceof
name|SizeBlockingQueue
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"forced execution, but expected a size queue"
argument_list|)
throw|;
block|}
try|try
block|{
operator|(
operator|(
name|SizeBlockingQueue
operator|)
name|queue
operator|)
operator|.
name|forcePut
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"forced execution, but got interrupted"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return;
block|}
block|}
name|rejected
operator|.
name|inc
argument_list|()
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"rejected execution "
argument_list|)
decl_stmt|;
if|if
condition|(
name|executor
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|SHUTTING_DOWN_KEY
operator|+
literal|" "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|executor
operator|.
name|getQueue
argument_list|()
operator|instanceof
name|SizeBlockingQueue
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"(queue capacity "
argument_list|)
operator|.
name|append
argument_list|(
operator|(
operator|(
name|SizeBlockingQueue
operator|)
name|executor
operator|.
name|getQueue
argument_list|()
operator|)
operator|.
name|capacity
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"on "
argument_list|)
operator|.
name|append
argument_list|(
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|EsRejectedExecutionException
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|rejected
specifier|public
name|long
name|rejected
parameter_list|()
block|{
return|return
name|rejected
operator|.
name|count
argument_list|()
return|;
block|}
block|}
end_class

end_unit
