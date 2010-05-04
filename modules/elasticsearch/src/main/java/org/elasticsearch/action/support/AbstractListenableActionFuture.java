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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractListenableActionFuture
specifier|public
specifier|abstract
class|class
name|AbstractListenableActionFuture
parameter_list|<
name|T
parameter_list|,
name|L
parameter_list|>
extends|extends
name|AdapterActionFuture
argument_list|<
name|T
argument_list|,
name|L
argument_list|>
block|{
DECL|field|listenerThreaded
specifier|private
specifier|final
name|boolean
name|listenerThreaded
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|volatile
name|Object
name|listeners
decl_stmt|;
DECL|field|executedListeners
specifier|private
name|boolean
name|executedListeners
init|=
literal|false
decl_stmt|;
DECL|method|AbstractListenableActionFuture
specifier|protected
name|AbstractListenableActionFuture
parameter_list|(
name|boolean
name|listenerThreaded
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|this
operator|.
name|listenerThreaded
operator|=
name|listenerThreaded
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
block|}
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
specifier|final
name|ActionListener
argument_list|<
name|T
argument_list|>
name|listener
parameter_list|)
block|{
name|internalAddListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
specifier|final
name|Runnable
name|listener
parameter_list|)
block|{
name|internalAddListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|internalAddListener
specifier|public
name|void
name|internalAddListener
parameter_list|(
name|Object
name|listener
parameter_list|)
block|{
name|boolean
name|executeImmediate
init|=
literal|false
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|executedListeners
condition|)
block|{
name|executeImmediate
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|Object
name|listeners
init|=
name|this
operator|.
name|listeners
decl_stmt|;
if|if
condition|(
name|listeners
operator|==
literal|null
condition|)
block|{
name|listeners
operator|=
name|listener
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|listeners
operator|instanceof
name|List
condition|)
block|{
operator|(
operator|(
name|List
operator|)
name|this
operator|.
name|listeners
operator|)
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Object
name|orig
init|=
name|listeners
decl_stmt|;
name|listeners
operator|=
name|newArrayListWithExpectedSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
operator|(
operator|(
name|List
operator|)
name|listeners
operator|)
operator|.
name|add
argument_list|(
name|orig
argument_list|)
expr_stmt|;
operator|(
operator|(
name|List
operator|)
name|listeners
operator|)
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|listeners
operator|=
name|listeners
expr_stmt|;
block|}
block|}
if|if
condition|(
name|executeImmediate
condition|)
block|{
name|executeListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|done
annotation|@
name|Override
specifier|protected
name|void
name|done
parameter_list|()
block|{
name|super
operator|.
name|done
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|executedListeners
operator|=
literal|true
expr_stmt|;
block|}
name|Object
name|listeners
init|=
name|this
operator|.
name|listeners
decl_stmt|;
if|if
condition|(
name|listeners
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|listeners
operator|instanceof
name|List
condition|)
block|{
name|List
name|list
init|=
operator|(
name|List
operator|)
name|listeners
decl_stmt|;
for|for
control|(
name|Object
name|listener
range|:
name|list
control|)
block|{
name|executeListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|executeListener
argument_list|(
name|listeners
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|executeListener
specifier|private
name|void
name|executeListener
parameter_list|(
specifier|final
name|Object
name|listener
parameter_list|)
block|{
if|if
condition|(
name|listenerThreaded
condition|)
block|{
if|if
condition|(
name|listener
operator|instanceof
name|Runnable
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|(
name|Runnable
operator|)
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|ActionListener
argument_list|<
name|T
argument_list|>
name|lst
init|=
operator|(
name|ActionListener
argument_list|<
name|T
argument_list|>
operator|)
name|listener
decl_stmt|;
try|try
block|{
name|lst
operator|.
name|onResponse
argument_list|(
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|lst
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|listener
operator|instanceof
name|Runnable
condition|)
block|{
operator|(
operator|(
name|Runnable
operator|)
name|listener
operator|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|ActionListener
argument_list|<
name|T
argument_list|>
name|lst
init|=
operator|(
name|ActionListener
argument_list|<
name|T
argument_list|>
operator|)
name|listener
decl_stmt|;
try|try
block|{
name|lst
operator|.
name|onResponse
argument_list|(
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|lst
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

