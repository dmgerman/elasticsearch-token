begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|action
operator|.
name|DocWriteResponse
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
name|Strings
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
name|XContentBuilder
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
name|rest
operator|.
name|RestStatus
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParserUtils
operator|.
name|ensureExpectedToken
import|;
end_import

begin_comment
comment|/**  * A response of an index operation,  *  * @see org.elasticsearch.action.index.IndexRequest  * @see org.elasticsearch.client.Client#index(IndexRequest)  */
end_comment

begin_class
DECL|class|IndexResponse
specifier|public
class|class
name|IndexResponse
extends|extends
name|DocWriteResponse
block|{
DECL|field|CREATED
specifier|private
specifier|static
specifier|final
name|String
name|CREATED
init|=
literal|"created"
decl_stmt|;
DECL|method|IndexResponse
specifier|public
name|IndexResponse
parameter_list|()
block|{     }
DECL|method|IndexResponse
specifier|public
name|IndexResponse
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|long
name|seqNo
parameter_list|,
name|long
name|primaryTerm
parameter_list|,
name|long
name|version
parameter_list|,
name|boolean
name|created
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|seqNo
argument_list|,
name|primaryTerm
argument_list|,
name|version
argument_list|,
name|created
condition|?
name|Result
operator|.
name|CREATED
else|:
name|Result
operator|.
name|UPDATED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|result
operator|==
name|Result
operator|.
name|CREATED
condition|?
name|RestStatus
operator|.
name|CREATED
else|:
name|super
operator|.
name|status
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"IndexResponse["
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"index="
argument_list|)
operator|.
name|append
argument_list|(
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",type="
argument_list|)
operator|.
name|append
argument_list|(
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",id="
argument_list|)
operator|.
name|append
argument_list|(
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",version="
argument_list|)
operator|.
name|append
argument_list|(
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",result="
argument_list|)
operator|.
name|append
argument_list|(
name|getResult
argument_list|()
operator|.
name|getLowercase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",seqNo="
argument_list|)
operator|.
name|append
argument_list|(
name|getSeqNo
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",primaryTerm="
argument_list|)
operator|.
name|append
argument_list|(
name|getPrimaryTerm
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",shards="
argument_list|)
operator|.
name|append
argument_list|(
name|Strings
operator|.
name|toString
argument_list|(
name|getShardInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|public
name|XContentBuilder
name|innerToXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CREATED
argument_list|,
name|result
operator|==
name|Result
operator|.
name|CREATED
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|IndexResponse
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureExpectedToken
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|,
name|parser
operator|::
name|getTokenLocation
argument_list|)
expr_stmt|;
name|Builder
name|context
init|=
operator|new
name|Builder
argument_list|()
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|parseXContentFields
argument_list|(
name|parser
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|context
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * Parse the current token and update the parsing context appropriately.      */
DECL|method|parseXContentFields
specifier|public
specifier|static
name|void
name|parseXContentFields
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|Builder
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
if|if
condition|(
name|CREATED
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|context
operator|.
name|setCreated
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|DocWriteResponse
operator|.
name|parseInnerToXContent
argument_list|(
name|parser
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Builder class for {@link IndexResponse}. This builder is usually used during xcontent parsing to      * temporarily store the parsed values, then the {@link Builder#build()} method is called to      * instantiate the {@link IndexResponse}.      */
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|DocWriteResponse
operator|.
name|Builder
block|{
DECL|field|created
specifier|private
name|boolean
name|created
init|=
literal|false
decl_stmt|;
DECL|method|setCreated
specifier|public
name|void
name|setCreated
parameter_list|(
name|boolean
name|created
parameter_list|)
block|{
name|this
operator|.
name|created
operator|=
name|created
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|IndexResponse
name|build
parameter_list|()
block|{
name|IndexResponse
name|indexResponse
init|=
operator|new
name|IndexResponse
argument_list|(
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|seqNo
argument_list|,
name|primaryTerm
argument_list|,
name|version
argument_list|,
name|created
argument_list|)
decl_stmt|;
name|indexResponse
operator|.
name|setForcedRefresh
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardInfo
operator|!=
literal|null
condition|)
block|{
name|indexResponse
operator|.
name|setShardInfo
argument_list|(
name|shardInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|indexResponse
return|;
block|}
block|}
block|}
end_class

end_unit

