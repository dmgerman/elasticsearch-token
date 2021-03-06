begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|IndicesRequest
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
name|OriginalIndices
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
name|search
operator|.
name|SearchRequest
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
name|search
operator|.
name|SearchTask
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
name|search
operator|.
name|SearchType
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
name|support
operator|.
name|IndicesOptions
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|query
operator|.
name|QueryBuilder
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
name|query
operator|.
name|QueryShardContext
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
name|search
operator|.
name|Scroll
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|TaskId
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
name|TransportRequest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Shard level search request that represents an actual search sent from the coordinating node to the nodes holding  * the shards where the query needs to be executed. Holds the same info as {@link org.elasticsearch.search.internal.ShardSearchLocalRequest}  * but gets sent over the transport and holds also the indices coming from the original request that generated it, plus its headers and context.  */
end_comment

begin_class
DECL|class|ShardSearchTransportRequest
specifier|public
class|class
name|ShardSearchTransportRequest
extends|extends
name|TransportRequest
implements|implements
name|ShardSearchRequest
implements|,
name|IndicesRequest
block|{
DECL|field|originalIndices
specifier|private
name|OriginalIndices
name|originalIndices
decl_stmt|;
DECL|field|shardSearchLocalRequest
specifier|private
name|ShardSearchLocalRequest
name|shardSearchLocalRequest
decl_stmt|;
DECL|method|ShardSearchTransportRequest
specifier|public
name|ShardSearchTransportRequest
parameter_list|()
block|{     }
DECL|method|ShardSearchTransportRequest
specifier|public
name|ShardSearchTransportRequest
parameter_list|(
name|OriginalIndices
name|originalIndices
parameter_list|,
name|SearchRequest
name|searchRequest
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|AliasFilter
name|aliasFilter
parameter_list|,
name|float
name|indexBoost
parameter_list|,
name|long
name|nowInMillis
parameter_list|)
block|{
name|this
operator|.
name|shardSearchLocalRequest
operator|=
operator|new
name|ShardSearchLocalRequest
argument_list|(
name|searchRequest
argument_list|,
name|shardId
argument_list|,
name|numberOfShards
argument_list|,
name|aliasFilter
argument_list|,
name|indexBoost
argument_list|,
name|nowInMillis
argument_list|)
expr_stmt|;
name|this
operator|.
name|originalIndices
operator|=
name|originalIndices
expr_stmt|;
block|}
DECL|method|searchType
specifier|public
name|void
name|searchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
name|shardSearchLocalRequest
operator|.
name|setSearchType
argument_list|(
name|searchType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
if|if
condition|(
name|originalIndices
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|originalIndices
operator|.
name|indices
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
if|if
condition|(
name|originalIndices
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|originalIndices
operator|.
name|indicesOptions
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|shardId
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|types
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|SearchSourceBuilder
name|source
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|source
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|void
name|source
parameter_list|(
name|SearchSourceBuilder
name|source
parameter_list|)
block|{
name|shardSearchLocalRequest
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|numberOfShards
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|searchType
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|filteringAliases
specifier|public
name|QueryBuilder
name|filteringAliases
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|filteringAliases
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indexBoost
specifier|public
name|float
name|indexBoost
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|indexBoost
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nowInMillis
specifier|public
name|long
name|nowInMillis
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|nowInMillis
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|requestCache
specifier|public
name|Boolean
name|requestCache
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|requestCache
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|scroll
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardSearchLocalRequest
operator|=
operator|new
name|ShardSearchLocalRequest
argument_list|()
expr_stmt|;
name|shardSearchLocalRequest
operator|.
name|innerReadFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|originalIndices
operator|=
name|OriginalIndices
operator|.
name|readOriginalIndices
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|shardSearchLocalRequest
operator|.
name|innerWriteTo
argument_list|(
name|out
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|OriginalIndices
operator|.
name|writeOriginalIndices
argument_list|(
name|originalIndices
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|cacheKey
specifier|public
name|BytesReference
name|cacheKey
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|cacheKey
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|setProfile
specifier|public
name|void
name|setProfile
parameter_list|(
name|boolean
name|profile
parameter_list|)
block|{
name|shardSearchLocalRequest
operator|.
name|setProfile
argument_list|(
name|profile
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isProfile
specifier|public
name|boolean
name|isProfile
parameter_list|()
block|{
return|return
name|shardSearchLocalRequest
operator|.
name|isProfile
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|void
name|rewrite
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|shardSearchLocalRequest
operator|.
name|rewrite
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTask
specifier|public
name|Task
name|createTask
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|TaskId
name|parentTaskId
parameter_list|)
block|{
return|return
operator|new
name|SearchTask
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|getDescription
argument_list|()
argument_list|,
name|parentTaskId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDescription
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
comment|// Shard id is enough here, the request itself can be found by looking at the parent task description
return|return
literal|"shardId["
operator|+
name|shardSearchLocalRequest
operator|.
name|shardId
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

