begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.analyze
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
name|analyze
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
name|ActionResponse
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|ToXContentObject
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
name|Iterator
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

begin_class
DECL|class|AnalyzeResponse
specifier|public
class|class
name|AnalyzeResponse
extends|extends
name|ActionResponse
implements|implements
name|Iterable
argument_list|<
name|AnalyzeResponse
operator|.
name|AnalyzeToken
argument_list|>
implements|,
name|ToXContentObject
block|{
DECL|class|AnalyzeToken
specifier|public
specifier|static
class|class
name|AnalyzeToken
implements|implements
name|Streamable
implements|,
name|ToXContentObject
block|{
DECL|field|term
specifier|private
name|String
name|term
decl_stmt|;
DECL|field|startOffset
specifier|private
name|int
name|startOffset
decl_stmt|;
DECL|field|endOffset
specifier|private
name|int
name|endOffset
decl_stmt|;
DECL|field|position
specifier|private
name|int
name|position
decl_stmt|;
DECL|field|positionLength
specifier|private
name|int
name|positionLength
init|=
literal|1
decl_stmt|;
DECL|field|attributes
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|attributes
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|method|AnalyzeToken
name|AnalyzeToken
parameter_list|()
block|{         }
DECL|method|AnalyzeToken
specifier|public
name|AnalyzeToken
parameter_list|(
name|String
name|term
parameter_list|,
name|int
name|position
parameter_list|,
name|int
name|startOffset
parameter_list|,
name|int
name|endOffset
parameter_list|,
name|int
name|positionLength
parameter_list|,
name|String
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|attributes
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|startOffset
operator|=
name|startOffset
expr_stmt|;
name|this
operator|.
name|endOffset
operator|=
name|endOffset
expr_stmt|;
name|this
operator|.
name|positionLength
operator|=
name|positionLength
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|attributes
operator|=
name|attributes
expr_stmt|;
block|}
DECL|method|getTerm
specifier|public
name|String
name|getTerm
parameter_list|()
block|{
return|return
name|this
operator|.
name|term
return|;
block|}
DECL|method|getStartOffset
specifier|public
name|int
name|getStartOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|startOffset
return|;
block|}
DECL|method|getEndOffset
specifier|public
name|int
name|getEndOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|endOffset
return|;
block|}
DECL|method|getPosition
specifier|public
name|int
name|getPosition
parameter_list|()
block|{
return|return
name|this
operator|.
name|position
return|;
block|}
DECL|method|getPositionLength
specifier|public
name|int
name|getPositionLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|positionLength
return|;
block|}
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|getAttributes
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getAttributes
parameter_list|()
block|{
return|return
name|this
operator|.
name|attributes
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOKEN
argument_list|,
name|term
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|START_OFFSET
argument_list|,
name|startOffset
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|END_OFFSET
argument_list|,
name|endOffset
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|POSITION
argument_list|,
name|position
argument_list|)
expr_stmt|;
if|if
condition|(
name|positionLength
operator|>
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|POSITION_LENGTH
argument_list|,
name|positionLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|attributes
operator|!=
literal|null
operator|&&
operator|!
name|attributes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entity
range|:
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entity
operator|.
name|getKey
argument_list|()
argument_list|,
name|entity
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|readAnalyzeToken
specifier|public
specifier|static
name|AnalyzeToken
name|readAnalyzeToken
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|AnalyzeToken
name|analyzeToken
init|=
operator|new
name|AnalyzeToken
argument_list|()
decl_stmt|;
name|analyzeToken
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|analyzeToken
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
name|term
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|startOffset
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|endOffset
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|position
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_2_0_UNRELEASED
argument_list|)
condition|)
block|{
name|Integer
name|len
init|=
name|in
operator|.
name|readOptionalVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|!=
literal|null
condition|)
block|{
name|positionLength
operator|=
name|len
expr_stmt|;
block|}
else|else
block|{
name|positionLength
operator|=
literal|1
expr_stmt|;
block|}
block|}
name|type
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|attributes
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
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
name|out
operator|.
name|writeString
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|startOffset
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|endOffset
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|position
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_2_0_UNRELEASED
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalVInt
argument_list|(
name|positionLength
operator|>
literal|1
condition|?
name|positionLength
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeGenericValue
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|field|detail
specifier|private
name|DetailAnalyzeResponse
name|detail
decl_stmt|;
DECL|field|tokens
specifier|private
name|List
argument_list|<
name|AnalyzeToken
argument_list|>
name|tokens
decl_stmt|;
DECL|method|AnalyzeResponse
name|AnalyzeResponse
parameter_list|()
block|{     }
DECL|method|AnalyzeResponse
specifier|public
name|AnalyzeResponse
parameter_list|(
name|List
argument_list|<
name|AnalyzeToken
argument_list|>
name|tokens
parameter_list|,
name|DetailAnalyzeResponse
name|detail
parameter_list|)
block|{
name|this
operator|.
name|tokens
operator|=
name|tokens
expr_stmt|;
name|this
operator|.
name|detail
operator|=
name|detail
expr_stmt|;
block|}
DECL|method|getTokens
specifier|public
name|List
argument_list|<
name|AnalyzeToken
argument_list|>
name|getTokens
parameter_list|()
block|{
return|return
name|this
operator|.
name|tokens
return|;
block|}
DECL|method|detail
specifier|public
name|DetailAnalyzeResponse
name|detail
parameter_list|()
block|{
return|return
name|this
operator|.
name|detail
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|AnalyzeToken
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|tokens
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|tokens
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|TOKENS
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeToken
name|token
range|:
name|tokens
control|)
block|{
name|token
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|detail
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|DETAIL
argument_list|)
expr_stmt|;
name|detail
operator|.
name|toXContent
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|tokens
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
name|tokens
operator|.
name|add
argument_list|(
name|AnalyzeToken
operator|.
name|readAnalyzeToken
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|detail
operator|=
name|in
operator|.
name|readOptionalStreamable
argument_list|(
name|DetailAnalyzeResponse
operator|::
operator|new
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
name|tokens
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|tokens
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeToken
name|token
range|:
name|tokens
control|)
block|{
name|token
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|detail
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|TOKENS
specifier|static
specifier|final
name|String
name|TOKENS
init|=
literal|"tokens"
decl_stmt|;
DECL|field|TOKEN
specifier|static
specifier|final
name|String
name|TOKEN
init|=
literal|"token"
decl_stmt|;
DECL|field|START_OFFSET
specifier|static
specifier|final
name|String
name|START_OFFSET
init|=
literal|"start_offset"
decl_stmt|;
DECL|field|END_OFFSET
specifier|static
specifier|final
name|String
name|END_OFFSET
init|=
literal|"end_offset"
decl_stmt|;
DECL|field|TYPE
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"type"
decl_stmt|;
DECL|field|POSITION
specifier|static
specifier|final
name|String
name|POSITION
init|=
literal|"position"
decl_stmt|;
DECL|field|POSITION_LENGTH
specifier|static
specifier|final
name|String
name|POSITION_LENGTH
init|=
literal|"positionLength"
decl_stmt|;
DECL|field|DETAIL
specifier|static
specifier|final
name|String
name|DETAIL
init|=
literal|"detail"
decl_stmt|;
block|}
block|}
end_class

end_unit

