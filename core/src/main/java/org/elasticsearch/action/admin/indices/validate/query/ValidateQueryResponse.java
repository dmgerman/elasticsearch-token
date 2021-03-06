begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.validate.query
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
name|validate
operator|.
name|query
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
name|ArrayList
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

begin_import
import|import static
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
name|validate
operator|.
name|query
operator|.
name|QueryExplanation
operator|.
name|readQueryExplanation
import|;
end_import

begin_comment
comment|/**  * The response of the validate action.  *  *  */
end_comment

begin_class
DECL|class|ValidateQueryResponse
specifier|public
class|class
name|ValidateQueryResponse
extends|extends
name|BroadcastResponse
block|{
DECL|field|valid
specifier|private
name|boolean
name|valid
decl_stmt|;
DECL|field|queryExplanations
specifier|private
name|List
argument_list|<
name|QueryExplanation
argument_list|>
name|queryExplanations
decl_stmt|;
DECL|method|ValidateQueryResponse
name|ValidateQueryResponse
parameter_list|()
block|{      }
DECL|method|ValidateQueryResponse
name|ValidateQueryResponse
parameter_list|(
name|boolean
name|valid
parameter_list|,
name|List
argument_list|<
name|QueryExplanation
argument_list|>
name|queryExplanations
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
name|valid
operator|=
name|valid
expr_stmt|;
name|this
operator|.
name|queryExplanations
operator|=
name|queryExplanations
expr_stmt|;
if|if
condition|(
name|queryExplanations
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|queryExplanations
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * A boolean denoting whether the query is valid.      */
DECL|method|isValid
specifier|public
name|boolean
name|isValid
parameter_list|()
block|{
return|return
name|valid
return|;
block|}
comment|/**      * The list of query explanations.      */
DECL|method|getQueryExplanation
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|QueryExplanation
argument_list|>
name|getQueryExplanation
parameter_list|()
block|{
if|if
condition|(
name|queryExplanations
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
return|return
name|queryExplanations
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
name|valid
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|queryExplanations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
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
name|queryExplanations
operator|.
name|add
argument_list|(
name|readQueryExplanation
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|writeBoolean
argument_list|(
name|valid
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|queryExplanations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|QueryExplanation
name|exp
range|:
name|queryExplanations
control|)
block|{
name|exp
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

