begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|RecoveryFailedException
specifier|public
class|class
name|RecoveryFailedException
extends|extends
name|ElasticsearchException
block|{
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|StartRecoveryRequest
name|request
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|request
argument_list|,
literal|null
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|StartRecoveryRequest
name|request
parameter_list|,
annotation|@
name|Nullable
name|String
name|extraInfo
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|extraInfo
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|RecoveryState
name|state
parameter_list|,
annotation|@
name|Nullable
name|String
name|extraInfo
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|state
operator|.
name|getShardId
argument_list|()
argument_list|,
name|state
operator|.
name|getSourceNode
argument_list|()
argument_list|,
name|state
operator|.
name|getTargetNode
argument_list|()
argument_list|,
name|extraInfo
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|DiscoveryNode
name|targetNode
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|shardId
argument_list|,
name|sourceNode
argument_list|,
name|targetNode
argument_list|,
literal|null
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|DiscoveryNode
name|targetNode
parameter_list|,
annotation|@
name|Nullable
name|String
name|extraInfo
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
operator|+
literal|": Recovery failed from "
operator|+
name|sourceNode
operator|+
literal|" into "
operator|+
name|targetNode
operator|+
operator|(
name|extraInfo
operator|==
literal|null
condition|?
literal|""
else|:
literal|" ("
operator|+
name|extraInfo
operator|+
literal|")"
operator|)
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryFailedException
specifier|public
name|RecoveryFailedException
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

