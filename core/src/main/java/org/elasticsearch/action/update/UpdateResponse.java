begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.update
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|update
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
name|get
operator|.
name|GetResult
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
name|seqno
operator|.
name|SequenceNumbersService
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

begin_class
DECL|class|UpdateResponse
specifier|public
class|class
name|UpdateResponse
extends|extends
name|DocWriteResponse
block|{
DECL|field|GET
specifier|private
specifier|static
specifier|final
name|String
name|GET
init|=
literal|"get"
decl_stmt|;
DECL|field|getResult
specifier|private
name|GetResult
name|getResult
decl_stmt|;
DECL|method|UpdateResponse
specifier|public
name|UpdateResponse
parameter_list|()
block|{     }
comment|/**      * Constructor to be used when a update didn't translate in a write.      * For example: update script with operation set to none      */
DECL|method|UpdateResponse
specifier|public
name|UpdateResponse
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
name|version
parameter_list|,
name|Result
name|result
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|ShardInfo
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|,
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
argument_list|,
literal|0
argument_list|,
name|version
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
DECL|method|UpdateResponse
specifier|public
name|UpdateResponse
parameter_list|(
name|ShardInfo
name|shardInfo
parameter_list|,
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
name|Result
name|result
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
name|result
argument_list|)
expr_stmt|;
name|setShardInfo
argument_list|(
name|shardInfo
argument_list|)
expr_stmt|;
block|}
DECL|method|setGetResult
specifier|public
name|void
name|setGetResult
parameter_list|(
name|GetResult
name|getResult
parameter_list|)
block|{
name|this
operator|.
name|getResult
operator|=
name|getResult
expr_stmt|;
block|}
DECL|method|getGetResult
specifier|public
name|GetResult
name|getGetResult
parameter_list|()
block|{
return|return
name|this
operator|.
name|getResult
return|;
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
name|this
operator|.
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|getResult
operator|=
name|GetResult
operator|.
name|readGetResult
argument_list|(
name|in
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
if|if
condition|(
name|getResult
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|getResult
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|getGetResult
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|GET
argument_list|)
expr_stmt|;
name|getGetResult
argument_list|()
operator|.
name|toXContentEmbedded
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
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
literal|"UpdateResponse["
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
literal|",shards="
argument_list|)
operator|.
name|append
argument_list|(
name|getShardInfo
argument_list|()
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
DECL|method|fromXContent
specifier|public
specifier|static
name|UpdateResponse
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
name|GET
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|context
operator|.
name|setGetResult
argument_list|(
name|GetResult
operator|.
name|fromXContentEmbedded
argument_list|(
name|parser
argument_list|)
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
comment|/**      * Builder class for {@link UpdateResponse}. This builder is usually used during xcontent parsing to      * temporarily store the parsed values, then the {@link DocWriteResponse.Builder#build()} method is called to      * instantiate the {@link UpdateResponse}.      */
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
DECL|field|getResult
specifier|private
name|GetResult
name|getResult
init|=
literal|null
decl_stmt|;
DECL|method|setGetResult
specifier|public
name|void
name|setGetResult
parameter_list|(
name|GetResult
name|getResult
parameter_list|)
block|{
name|this
operator|.
name|getResult
operator|=
name|getResult
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|UpdateResponse
name|build
parameter_list|()
block|{
name|UpdateResponse
name|update
decl_stmt|;
if|if
condition|(
name|shardInfo
operator|!=
literal|null
operator|&&
name|seqNo
operator|!=
literal|null
condition|)
block|{
name|update
operator|=
operator|new
name|UpdateResponse
argument_list|(
name|shardInfo
argument_list|,
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
name|result
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|update
operator|=
operator|new
name|UpdateResponse
argument_list|(
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|version
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getResult
operator|!=
literal|null
condition|)
block|{
name|update
operator|.
name|setGetResult
argument_list|(
operator|new
name|GetResult
argument_list|(
name|update
operator|.
name|getIndex
argument_list|()
argument_list|,
name|update
operator|.
name|getType
argument_list|()
argument_list|,
name|update
operator|.
name|getId
argument_list|()
argument_list|,
name|update
operator|.
name|getVersion
argument_list|()
argument_list|,
name|getResult
operator|.
name|isExists
argument_list|()
argument_list|,
name|getResult
operator|.
name|internalSourceRef
argument_list|()
argument_list|,
name|getResult
operator|.
name|getFields
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|update
operator|.
name|setForcedRefresh
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
return|return
name|update
return|;
block|}
block|}
block|}
end_class

end_unit

