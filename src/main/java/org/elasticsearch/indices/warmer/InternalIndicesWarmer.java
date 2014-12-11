begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.warmer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|warmer
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|ClusterService
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
name|metadata
operator|.
name|IndexMetaData
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
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
name|index
operator|.
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|IndexShard
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|InternalIndicesWarmer
specifier|public
class|class
name|InternalIndicesWarmer
extends|extends
name|AbstractComponent
implements|implements
name|IndicesWarmer
block|{
DECL|field|INDEX_WARMER_ENABLED
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_WARMER_ENABLED
init|=
literal|"index.warmer.enabled"
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|InternalIndicesWarmer
specifier|public
name|InternalIndicesWarmer
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|removeListener
specifier|public
name|void
name|removeListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|warmNewReaders
specifier|public
name|void
name|warmNewReaders
parameter_list|(
specifier|final
name|WarmerContext
name|context
parameter_list|)
block|{
name|warmInternal
argument_list|(
name|context
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|warmTopReader
specifier|public
name|void
name|warmTopReader
parameter_list|(
name|WarmerContext
name|context
parameter_list|)
block|{
name|warmInternal
argument_list|(
name|context
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|warmInternal
specifier|private
name|void
name|warmInternal
parameter_list|(
specifier|final
name|WarmerContext
name|context
parameter_list|,
name|boolean
name|topReader
parameter_list|)
block|{
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_WARMER_ENABLED
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_WARMER_ENABLED
argument_list|,
literal|true
argument_list|)
argument_list|)
condition|)
block|{
return|return;
block|}
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|topReader
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] top warming [{}]"
argument_list|,
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] warming [{}]"
argument_list|,
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|context
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
name|indexShard
operator|.
name|warmerService
argument_list|()
operator|.
name|onPreWarm
argument_list|()
expr_stmt|;
name|long
name|time
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|IndicesWarmer
operator|.
name|Listener
operator|.
name|TerminationHandle
argument_list|>
name|terminationHandles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// get a handle on pending tasks
for|for
control|(
specifier|final
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
if|if
condition|(
name|topReader
condition|)
block|{
name|terminationHandles
operator|.
name|add
argument_list|(
name|listener
operator|.
name|warmTopReader
argument_list|(
name|indexShard
argument_list|,
name|indexMetaData
argument_list|,
name|context
argument_list|,
name|threadPool
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|terminationHandles
operator|.
name|add
argument_list|(
name|listener
operator|.
name|warmNewReaders
argument_list|(
name|indexShard
argument_list|,
name|indexMetaData
argument_list|,
name|context
argument_list|,
name|threadPool
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// wait for termination
for|for
control|(
name|IndicesWarmer
operator|.
name|Listener
operator|.
name|TerminationHandle
name|terminationHandle
range|:
name|terminationHandles
control|)
block|{
try|try
block|{
name|terminationHandle
operator|.
name|awaitTermination
argument_list|()
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
if|if
condition|(
name|topReader
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"top warming has been interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"warming has been interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
name|long
name|took
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|time
decl_stmt|;
name|indexShard
operator|.
name|warmerService
argument_list|()
operator|.
name|onPostWarm
argument_list|(
name|took
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexShard
operator|.
name|warmerService
argument_list|()
operator|.
name|logger
argument_list|()
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|topReader
condition|)
block|{
name|indexShard
operator|.
name|warmerService
argument_list|()
operator|.
name|logger
argument_list|()
operator|.
name|trace
argument_list|(
literal|"top warming took [{}]"
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|took
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexShard
operator|.
name|warmerService
argument_list|()
operator|.
name|logger
argument_list|()
operator|.
name|trace
argument_list|(
literal|"warming took [{}]"
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|took
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

