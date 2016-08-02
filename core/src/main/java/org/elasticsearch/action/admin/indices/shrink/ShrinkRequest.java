begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shrink
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
name|shrink
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ActionRequestValidationException
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexRequest
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
name|ActiveShardCount
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
name|action
operator|.
name|support
operator|.
name|master
operator|.
name|AcknowledgedRequest
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
name|ParseField
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
name|ParseFieldMatcher
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
name|ParseFieldMatcherSupplier
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
name|common
operator|.
name|xcontent
operator|.
name|ObjectParser
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|XContentType
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
name|Objects
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
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * Request class to shrink an index into a single shard  */
end_comment

begin_class
DECL|class|ShrinkRequest
specifier|public
class|class
name|ShrinkRequest
extends|extends
name|AcknowledgedRequest
argument_list|<
name|ShrinkRequest
argument_list|>
implements|implements
name|IndicesRequest
block|{
DECL|field|PARSER
specifier|public
specifier|static
name|ObjectParser
argument_list|<
name|ShrinkRequest
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"shrink_request"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|request
parameter_list|,
name|parseFieldMatcherSupplier
parameter_list|)
lambda|->
name|request
operator|.
name|getShrinkIndexRequest
argument_list|()
operator|.
name|settings
argument_list|(
name|parser
operator|.
name|map
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"settings"
argument_list|)
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|request
parameter_list|,
name|parseFieldMatcherSupplier
parameter_list|)
lambda|->
name|request
operator|.
name|getShrinkIndexRequest
argument_list|()
operator|.
name|aliases
argument_list|(
name|parser
operator|.
name|map
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"aliases"
argument_list|)
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
block|}
DECL|field|shrinkIndexRequest
specifier|private
name|CreateIndexRequest
name|shrinkIndexRequest
decl_stmt|;
DECL|field|sourceIndex
specifier|private
name|String
name|sourceIndex
decl_stmt|;
DECL|method|ShrinkRequest
name|ShrinkRequest
parameter_list|()
block|{}
DECL|method|ShrinkRequest
specifier|public
name|ShrinkRequest
parameter_list|(
name|String
name|targetIndex
parameter_list|,
name|String
name|sourceindex
parameter_list|)
block|{
name|this
operator|.
name|shrinkIndexRequest
operator|=
operator|new
name|CreateIndexRequest
argument_list|(
name|targetIndex
argument_list|)
expr_stmt|;
name|this
operator|.
name|sourceIndex
operator|=
name|sourceindex
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|shrinkIndexRequest
operator|==
literal|null
condition|?
literal|null
else|:
name|shrinkIndexRequest
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceIndex
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"source index is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shrinkIndexRequest
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"shrink index request is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
DECL|method|setSourceIndex
specifier|public
name|void
name|setSourceIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|sourceIndex
operator|=
name|index
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
name|shrinkIndexRequest
operator|=
operator|new
name|CreateIndexRequest
argument_list|()
expr_stmt|;
name|shrinkIndexRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|sourceIndex
operator|=
name|in
operator|.
name|readString
argument_list|()
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
name|shrinkIndexRequest
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|sourceIndex
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
return|return
operator|new
name|String
index|[]
block|{
name|sourceIndex
block|}
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
return|return
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
return|;
block|}
DECL|method|setShrinkIndex
specifier|public
name|void
name|setShrinkIndex
parameter_list|(
name|CreateIndexRequest
name|shrinkIndexRequest
parameter_list|)
block|{
name|this
operator|.
name|shrinkIndexRequest
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|shrinkIndexRequest
argument_list|,
literal|"shrink index request must not be null"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the {@link CreateIndexRequest} for the shrink index      */
DECL|method|getShrinkIndexRequest
specifier|public
name|CreateIndexRequest
name|getShrinkIndexRequest
parameter_list|()
block|{
return|return
name|shrinkIndexRequest
return|;
block|}
comment|/**      * Returns the source index name      */
DECL|method|getSourceIndex
specifier|public
name|String
name|getSourceIndex
parameter_list|()
block|{
return|return
name|sourceIndex
return|;
block|}
comment|/**      * Sets the number of shard copies that should be active for creation of the      * new shrunken index to return. Defaults to {@link ActiveShardCount#DEFAULT}, which will      * wait for one shard copy (the primary) to become active. Set this value to      * {@link ActiveShardCount#ALL} to wait for all shards (primary and all replicas) to be active      * before returning. Otherwise, use {@link ActiveShardCount#from(int)} to set this value to any      * non-negative integer, up to the number of copies per shard (number of replicas + 1),      * to wait for the desired amount of shard copies to become active before returning.      * Index creation will only wait up until the timeout value for the number of shard copies      * to be active before returning.  Check {@link ShrinkResponse#isShardsAcked()} to      * determine if the requisite shard copies were all started before returning or timing out.      *      * @param waitForActiveShards number of active shard copies to wait on      */
DECL|method|setWaitForActiveShards
specifier|public
name|void
name|setWaitForActiveShards
parameter_list|(
name|ActiveShardCount
name|waitForActiveShards
parameter_list|)
block|{
name|this
operator|.
name|getShrinkIndexRequest
argument_list|()
operator|.
name|waitForActiveShards
argument_list|(
name|waitForActiveShards
argument_list|)
expr_stmt|;
block|}
comment|/**      * A shortcut for {@link #setWaitForActiveShards(ActiveShardCount)} where the numerical      * shard count is passed in, instead of having to first call {@link ActiveShardCount#from(int)}      * to get the ActiveShardCount.      */
DECL|method|setWaitForActiveShards
specifier|public
name|void
name|setWaitForActiveShards
parameter_list|(
specifier|final
name|int
name|waitForActiveShards
parameter_list|)
block|{
name|setWaitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|waitForActiveShards
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|source
specifier|public
name|void
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|XContentType
name|xContentType
init|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|source
argument_list|)
decl_stmt|;
if|if
condition|(
name|xContentType
operator|!=
literal|null
condition|)
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|xContentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
init|)
block|{
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|this
argument_list|,
parameter_list|()
lambda|->
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse source for shrink index"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse content type for shrink index source"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

