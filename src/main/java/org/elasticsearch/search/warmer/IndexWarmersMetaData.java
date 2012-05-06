begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.warmer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|warmer
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|metadata
operator|.
name|IndexMetaData
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
name|BytesHolder
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
name|ToXContent
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|IndexWarmersMetaData
specifier|public
class|class
name|IndexWarmersMetaData
implements|implements
name|IndexMetaData
operator|.
name|Custom
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"warmers"
decl_stmt|;
DECL|field|FACTORY
specifier|public
specifier|static
specifier|final
name|Factory
name|FACTORY
init|=
operator|new
name|Factory
argument_list|()
decl_stmt|;
static|static
block|{
name|IndexMetaData
operator|.
name|registerFactory
argument_list|(
name|TYPE
argument_list|,
name|FACTORY
argument_list|)
expr_stmt|;
block|}
DECL|class|Entry
specifier|public
specifier|static
class|class
name|Entry
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|types
specifier|private
specifier|final
name|String
index|[]
name|types
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|BytesHolder
name|source
decl_stmt|;
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|BytesHolder
name|source
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
operator|==
literal|null
condition|?
name|Strings
operator|.
name|EMPTY_ARRAY
else|:
name|types
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
annotation|@
name|Nullable
DECL|method|source
specifier|public
name|BytesHolder
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
block|}
DECL|field|entries
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|Entry
argument_list|>
name|entries
decl_stmt|;
DECL|method|IndexWarmersMetaData
specifier|public
name|IndexWarmersMetaData
parameter_list|(
name|Entry
modifier|...
name|entries
parameter_list|)
block|{
name|this
operator|.
name|entries
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|entries
argument_list|)
expr_stmt|;
block|}
DECL|method|entries
specifier|public
name|ImmutableList
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
block|{
return|return
name|this
operator|.
name|entries
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
implements|implements
name|IndexMetaData
operator|.
name|Custom
operator|.
name|Factory
argument_list|<
name|IndexWarmersMetaData
argument_list|>
block|{
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|IndexWarmersMetaData
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Entry
index|[]
name|entries
init|=
operator|new
name|Entry
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|entries
index|[
name|i
index|]
operator|=
operator|new
name|Entry
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readStringArray
argument_list|()
argument_list|,
name|in
operator|.
name|readBoolean
argument_list|()
condition|?
name|in
operator|.
name|readBytesHolder
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|IndexWarmersMetaData
argument_list|(
name|entries
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|IndexWarmersMetaData
name|warmers
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|warmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|warmers
operator|.
name|entries
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|entry
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|entry
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|source
argument_list|()
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
name|out
operator|.
name|writeBytesHolder
argument_list|(
name|entry
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|IndexWarmersMetaData
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we get here after we are at warmers token
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|Entry
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
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
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
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
name|String
name|name
init|=
name|currentFieldName
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|types
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|BytesHolder
name|source
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
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
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"types"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|types
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
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
if|if
condition|(
literal|"source"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|map
argument_list|(
name|parser
operator|.
name|mapOrdered
argument_list|()
argument_list|)
decl_stmt|;
name|source
operator|=
operator|new
name|BytesHolder
argument_list|(
name|builder
operator|.
name|underlyingBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|builder
operator|.
name|underlyingBytesLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_EMBEDDED_OBJECT
condition|)
block|{
if|if
condition|(
literal|"source"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|source
operator|=
operator|new
name|BytesHolder
argument_list|(
name|parser
operator|.
name|binaryValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
name|name
argument_list|,
name|types
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|?
name|Strings
operator|.
name|EMPTY_ARRAY
else|:
name|types
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|types
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|IndexWarmersMetaData
argument_list|(
name|entries
operator|.
name|toArray
argument_list|(
operator|new
name|Entry
index|[
name|entries
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|void
name|toXContent
parameter_list|(
name|IndexWarmersMetaData
name|warmers
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
comment|//No need, IndexMetaData already writes it
comment|//builder.startObject(TYPE, XContentBuilder.FieldCaseConversion.NONE);
for|for
control|(
name|Entry
name|entry
range|:
name|warmers
operator|.
name|entries
argument_list|()
control|)
block|{
name|toXContent
argument_list|(
name|entry
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
comment|//No need, IndexMetaData already writes it
comment|//builder.endObject();
block|}
DECL|method|toXContent
specifier|public
name|void
name|toXContent
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|binary
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"binary"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|entry
operator|.
name|name
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"types"
argument_list|,
name|entry
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"source"
argument_list|)
expr_stmt|;
if|if
condition|(
name|binary
condition|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|entry
operator|.
name|source
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|,
name|entry
operator|.
name|source
argument_list|()
operator|.
name|offset
argument_list|()
argument_list|,
name|entry
operator|.
name|source
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|entry
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|entry
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|mapOrderedAndClose
argument_list|()
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

