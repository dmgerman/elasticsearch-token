begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.scheduler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|scheduler
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|MergePolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|MergeScheduler
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
name|index
operator|.
name|merge
operator|.
name|MergeStats
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
name|merge
operator|.
name|OnGoingMerge
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
name|settings
operator|.
name|IndexSettings
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
name|AbstractIndexShardComponent
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
name|IndexShardComponent
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
name|ShardId
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
name|Set
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MergeSchedulerProvider
specifier|public
specifier|abstract
class|class
name|MergeSchedulerProvider
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|IndexShardComponent
block|{
DECL|interface|FailureListener
specifier|public
specifier|static
interface|interface
name|FailureListener
block|{
DECL|method|onFailedMerge
name|void
name|onFailedMerge
parameter_list|(
name|MergePolicy
operator|.
name|MergeException
name|e
parameter_list|)
function_decl|;
block|}
comment|/**      * Listener for events before/after single merges. Called on the merge thread.      */
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
comment|/**          * A callback before a merge is going to execute. Note, any logic here will block the merge          * till its done.          */
DECL|method|beforeMerge
name|void
name|beforeMerge
parameter_list|(
name|OnGoingMerge
name|merge
parameter_list|)
function_decl|;
comment|/**          * A callback after a merge is going to execute. Note, any logic here will block the merge          * thread.          */
DECL|method|afterMerge
name|void
name|afterMerge
parameter_list|(
name|OnGoingMerge
name|merge
parameter_list|)
function_decl|;
block|}
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|failureListeners
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|FailureListener
argument_list|>
name|failureListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
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
DECL|field|notifyOnMergeFailure
specifier|private
specifier|final
name|boolean
name|notifyOnMergeFailure
decl_stmt|;
DECL|method|MergeSchedulerProvider
specifier|protected
name|MergeSchedulerProvider
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
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
name|notifyOnMergeFailure
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"notify_on_failure"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|addFailureListener
specifier|public
name|void
name|addFailureListener
parameter_list|(
name|FailureListener
name|listener
parameter_list|)
block|{
name|failureListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|removeFailureListener
specifier|public
name|void
name|removeFailureListener
parameter_list|(
name|FailureListener
name|listener
parameter_list|)
block|{
name|failureListeners
operator|.
name|remove
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
DECL|method|failedMerge
specifier|protected
name|void
name|failedMerge
parameter_list|(
specifier|final
name|MergePolicy
operator|.
name|MergeException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|notifyOnMergeFailure
condition|)
block|{
return|return;
block|}
for|for
control|(
specifier|final
name|FailureListener
name|failureListener
range|:
name|failureListeners
control|)
block|{
name|threadPool
operator|.
name|generic
argument_list|()
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
name|failureListener
operator|.
name|onFailedMerge
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|beforeMerge
specifier|protected
name|void
name|beforeMerge
parameter_list|(
name|OnGoingMerge
name|merge
parameter_list|)
block|{
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|beforeMerge
argument_list|(
name|merge
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|afterMerge
specifier|protected
name|void
name|afterMerge
parameter_list|(
name|OnGoingMerge
name|merge
parameter_list|)
block|{
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|afterMerge
argument_list|(
name|merge
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Maximum number of allowed running merges before index throttling kicks in. */
DECL|method|getMaxMerges
specifier|public
specifier|abstract
name|int
name|getMaxMerges
parameter_list|()
function_decl|;
DECL|method|newMergeScheduler
specifier|public
specifier|abstract
name|MergeScheduler
name|newMergeScheduler
parameter_list|()
function_decl|;
DECL|method|stats
specifier|public
specifier|abstract
name|MergeStats
name|stats
parameter_list|()
function_decl|;
DECL|method|onGoingMerges
specifier|public
specifier|abstract
name|Set
argument_list|<
name|OnGoingMerge
argument_list|>
name|onGoingMerges
parameter_list|()
function_decl|;
DECL|method|close
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
function_decl|;
block|}
end_class

end_unit

