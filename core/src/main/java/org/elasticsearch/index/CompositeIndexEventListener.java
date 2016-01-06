begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
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
name|Nullable
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
name|Loggers
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
name|shard
operator|.
name|IndexEventListener
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
name|index
operator|.
name|shard
operator|.
name|IndexShardState
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_comment
comment|/**  * A composite {@link IndexEventListener} that forwards all callbacks to an immutable list of IndexEventListener  */
end_comment

begin_class
DECL|class|CompositeIndexEventListener
specifier|final
class|class
name|CompositeIndexEventListener
implements|implements
name|IndexEventListener
block|{
DECL|field|listeners
specifier|private
specifier|final
name|List
argument_list|<
name|IndexEventListener
argument_list|>
name|listeners
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|CompositeIndexEventListener
name|CompositeIndexEventListener
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Collection
argument_list|<
name|IndexEventListener
argument_list|>
name|listeners
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
if|if
condition|(
name|listener
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"listeners must be non-null"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|listeners
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|listeners
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|indexSettings
operator|.
name|getSettings
argument_list|()
argument_list|,
name|indexSettings
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|shardRoutingChanged
specifier|public
name|void
name|shardRoutingChanged
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
annotation|@
name|Nullable
name|ShardRouting
name|oldRouting
parameter_list|,
name|ShardRouting
name|newRouting
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|shardRoutingChanged
argument_list|(
name|indexShard
argument_list|,
name|oldRouting
argument_list|,
name|newRouting
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke shard touring changed callback"
argument_list|,
name|t
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexShardCreated
specifier|public
name|void
name|afterIndexShardCreated
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexShardCreated
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke after shard created callback"
argument_list|,
name|t
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexShardStarted
specifier|public
name|void
name|afterIndexShardStarted
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexShardStarted
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke after shard started callback"
argument_list|,
name|t
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexShardClosed
specifier|public
name|void
name|beforeIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexShardClosed
argument_list|(
name|shardId
argument_list|,
name|indexShard
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke before shard closed callback"
argument_list|,
name|t
argument_list|,
name|shardId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexShardClosed
specifier|public
name|void
name|afterIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexShardClosed
argument_list|(
name|shardId
argument_list|,
name|indexShard
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke after shard closed callback"
argument_list|,
name|t
argument_list|,
name|shardId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onShardInactive
specifier|public
name|void
name|onShardInactive
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|onShardInactive
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke on shard inactive callback"
argument_list|,
name|t
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|indexShardStateChanged
specifier|public
name|void
name|indexShardStateChanged
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
annotation|@
name|Nullable
name|IndexShardState
name|previousState
parameter_list|,
name|IndexShardState
name|currentState
parameter_list|,
annotation|@
name|Nullable
name|String
name|reason
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|indexShardStateChanged
argument_list|(
name|indexShard
argument_list|,
name|previousState
argument_list|,
name|indexShard
operator|.
name|state
argument_list|()
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke index shard state changed callback"
argument_list|,
name|t
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexCreated
specifier|public
name|void
name|beforeIndexCreated
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexCreated
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke before index created callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexCreated
specifier|public
name|void
name|afterIndexCreated
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexCreated
argument_list|(
name|indexService
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke after index created callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexShardCreated
specifier|public
name|void
name|beforeIndexShardCreated
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexShardCreated
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke before shard created callback"
argument_list|,
name|t
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexClosed
specifier|public
name|void
name|beforeIndexClosed
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexClosed
argument_list|(
name|indexService
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke before index closed callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexDeleted
specifier|public
name|void
name|beforeIndexDeleted
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexDeleted
argument_list|(
name|indexService
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke before index deleted callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexDeleted
specifier|public
name|void
name|afterIndexDeleted
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexDeleted
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke after index deleted callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexClosed
specifier|public
name|void
name|afterIndexClosed
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexClosed
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke after index closed callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexShardDeleted
specifier|public
name|void
name|beforeIndexShardDeleted
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexShardDeleted
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke before shard deleted callback"
argument_list|,
name|t
argument_list|,
name|shardId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexShardDeleted
specifier|public
name|void
name|afterIndexShardDeleted
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|afterIndexShardDeleted
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to invoke after shard deleted callback"
argument_list|,
name|t
argument_list|,
name|shardId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeIndexAddedToCluster
specifier|public
name|void
name|beforeIndexAddedToCluster
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
for|for
control|(
name|IndexEventListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|beforeIndexAddedToCluster
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to invoke before index added to cluster callback"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

