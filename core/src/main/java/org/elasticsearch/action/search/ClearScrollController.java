begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodes
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
name|CountDown
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
name|Transport
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
name|TransportResponse
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|TransportSearchHelper
operator|.
name|parseScrollId
import|;
end_import

begin_class
DECL|class|ClearScrollController
specifier|final
class|class
name|ClearScrollController
implements|implements
name|Runnable
block|{
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|searchTransportService
specifier|private
specifier|final
name|SearchTransportService
name|searchTransportService
decl_stmt|;
DECL|field|expectedOps
specifier|private
specifier|final
name|CountDown
name|expectedOps
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|hasFailed
specifier|private
specifier|final
name|AtomicBoolean
name|hasFailed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|field|freedSearchContexts
specifier|private
specifier|final
name|AtomicInteger
name|freedSearchContexts
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|runner
specifier|private
specifier|final
name|Runnable
name|runner
decl_stmt|;
DECL|method|ClearScrollController
name|ClearScrollController
parameter_list|(
name|ClearScrollRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
name|listener
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
name|Logger
name|logger
parameter_list|,
name|SearchTransportService
name|searchTransportService
parameter_list|)
block|{
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|searchTransportService
operator|=
name|searchTransportService
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|scrollIds
init|=
name|request
operator|.
name|getScrollIds
argument_list|()
decl_stmt|;
specifier|final
name|int
name|expectedOps
decl_stmt|;
if|if
condition|(
name|scrollIds
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
literal|"_all"
operator|.
name|equals
argument_list|(
name|scrollIds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
name|expectedOps
operator|=
name|nodes
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|runner
operator|=
name|this
operator|::
name|cleanAllScrolls
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|ScrollIdForNode
argument_list|>
name|parsedScrollIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|parsedScrollId
range|:
name|request
operator|.
name|getScrollIds
argument_list|()
control|)
block|{
name|ScrollIdForNode
index|[]
name|context
init|=
name|parseScrollId
argument_list|(
name|parsedScrollId
argument_list|)
operator|.
name|getContext
argument_list|()
decl_stmt|;
for|for
control|(
name|ScrollIdForNode
name|id
range|:
name|context
control|)
block|{
name|parsedScrollIds
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|parsedScrollIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|expectedOps
operator|=
literal|0
expr_stmt|;
name|runner
operator|=
parameter_list|()
lambda|->
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
literal|true
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|expectedOps
operator|=
name|parsedScrollIds
operator|.
name|size
argument_list|()
expr_stmt|;
name|runner
operator|=
parameter_list|()
lambda|->
name|cleanScrollIds
argument_list|(
name|parsedScrollIds
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|expectedOps
operator|=
operator|new
name|CountDown
argument_list|(
name|expectedOps
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|runner
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
DECL|method|cleanAllScrolls
name|void
name|cleanAllScrolls
parameter_list|()
block|{
for|for
control|(
specifier|final
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
try|try
block|{
name|Transport
operator|.
name|Connection
name|connection
init|=
name|searchTransportService
operator|.
name|getConnection
argument_list|(
literal|null
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|searchTransportService
operator|.
name|sendClearAllScrollContexts
argument_list|(
name|connection
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TransportResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|)
block|{
name|onFreedContext
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|cleanScrollIds
name|void
name|cleanScrollIds
parameter_list|(
name|List
argument_list|<
name|ScrollIdForNode
argument_list|>
name|parsedScrollIds
parameter_list|)
block|{
for|for
control|(
name|ScrollIdForNode
name|target
range|:
name|parsedScrollIds
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|target
operator|.
name|getNode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
name|onFreedContext
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|Transport
operator|.
name|Connection
name|connection
init|=
name|searchTransportService
operator|.
name|getConnection
argument_list|(
literal|null
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|searchTransportService
operator|.
name|sendFreeContext
argument_list|(
name|connection
argument_list|,
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|freed
lambda|->
name|onFreedContext
argument_list|(
name|freed
operator|.
name|isFreed
argument_list|()
argument_list|)
argument_list|,
name|e
lambda|->
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|onFreedContext
specifier|private
name|void
name|onFreedContext
parameter_list|(
name|boolean
name|freed
parameter_list|)
block|{
if|if
condition|(
name|freed
condition|)
block|{
name|freedSearchContexts
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|expectedOps
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|boolean
name|succeeded
init|=
name|hasFailed
operator|.
name|get
argument_list|()
operator|==
literal|false
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
name|succeeded
argument_list|,
name|freedSearchContexts
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onFailedFreedContext
specifier|private
name|void
name|onFailedFreedContext
parameter_list|(
name|Throwable
name|e
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"Clear SC failed on node[{}]"
argument_list|,
name|node
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedOps
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
literal|false
argument_list|,
name|freedSearchContexts
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hasFailed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

