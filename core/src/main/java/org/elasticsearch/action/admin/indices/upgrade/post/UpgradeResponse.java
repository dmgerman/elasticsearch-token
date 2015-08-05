begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.upgrade.post
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|upgrade
operator|.
name|post
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|ShardOperationFailedException
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
name|broadcast
operator|.
name|BroadcastResponse
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
name|collect
operator|.
name|Tuple
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  * A response for optimize action.  *  *  */
end_comment

begin_class
DECL|class|UpgradeResponse
specifier|public
class|class
name|UpgradeResponse
extends|extends
name|BroadcastResponse
block|{
DECL|field|versions
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|String
argument_list|>
argument_list|>
name|versions
decl_stmt|;
DECL|method|UpgradeResponse
name|UpgradeResponse
parameter_list|()
block|{      }
DECL|method|UpgradeResponse
name|UpgradeResponse
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|String
argument_list|>
argument_list|>
name|versions
parameter_list|,
name|int
name|totalShards
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|)
block|{
name|super
argument_list|(
name|totalShards
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
expr_stmt|;
name|this
operator|.
name|versions
operator|=
name|versions
expr_stmt|;
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|versions
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|String
name|index
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|Version
name|upgradeVersion
init|=
name|Version
operator|.
name|readVersion
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|String
name|oldestLuceneSegment
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|versions
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|upgradeVersion
argument_list|,
name|oldestLuceneSegment
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|writeVInt
argument_list|(
name|versions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|versions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|Version
operator|.
name|writeVersion
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|v1
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns the highest upgrade version of the node that performed metadata upgrade and the      * the version of the oldest lucene segment for each index that was upgraded.      */
DECL|method|versions
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|String
argument_list|>
argument_list|>
name|versions
parameter_list|()
block|{
return|return
name|versions
return|;
block|}
block|}
end_class

end_unit

