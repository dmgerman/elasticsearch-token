begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|bytes
operator|.
name|BytesReference
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
name|cache
operator|.
name|RemovalNotification
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
name|cache
operator|.
name|request
operator|.
name|ShardRequestCache
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

begin_comment
comment|/**  * Abstract base class for the an {@link IndexShard} level {@linkplain IndicesRequestCache.CacheEntity}.  */
end_comment

begin_class
DECL|class|AbstractIndexShardCacheEntity
specifier|abstract
class|class
name|AbstractIndexShardCacheEntity
implements|implements
name|IndicesRequestCache
operator|.
name|CacheEntity
block|{
comment|/**      * Get the {@linkplain ShardRequestCache} used to track cache statistics.      */
DECL|method|stats
specifier|protected
specifier|abstract
name|ShardRequestCache
name|stats
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|onCached
specifier|public
specifier|final
name|void
name|onCached
parameter_list|(
name|IndicesRequestCache
operator|.
name|Key
name|key
parameter_list|,
name|BytesReference
name|value
parameter_list|)
block|{
name|stats
argument_list|()
operator|.
name|onCached
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onHit
specifier|public
specifier|final
name|void
name|onHit
parameter_list|()
block|{
name|stats
argument_list|()
operator|.
name|onHit
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onMiss
specifier|public
specifier|final
name|void
name|onMiss
parameter_list|()
block|{
name|stats
argument_list|()
operator|.
name|onMiss
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onRemoval
specifier|public
specifier|final
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|IndicesRequestCache
operator|.
name|Key
argument_list|,
name|BytesReference
argument_list|>
name|notification
parameter_list|)
block|{
name|stats
argument_list|()
operator|.
name|onRemoval
argument_list|(
name|notification
operator|.
name|getKey
argument_list|()
argument_list|,
name|notification
operator|.
name|getValue
argument_list|()
argument_list|,
name|notification
operator|.
name|getRemovalReason
argument_list|()
operator|==
name|RemovalNotification
operator|.
name|RemovalReason
operator|.
name|EVICTED
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

