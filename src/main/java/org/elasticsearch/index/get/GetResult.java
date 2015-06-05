begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
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
name|ImmutableMap
import|;
end_import

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
name|Lists
import|;
end_import

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
name|compress
operator|.
name|CompressorFactory
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
name|XContentBuilderString
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
name|XContentHelper
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
name|lookup
operator|.
name|SourceLookup
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
name|Iterators
operator|.
name|emptyIterator
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
name|newHashMapWithExpectedSize
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
operator|.
name|GetField
operator|.
name|readGetField
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|GetResult
specifier|public
class|class
name|GetResult
implements|implements
name|Streamable
implements|,
name|Iterable
argument_list|<
name|GetField
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|version
specifier|private
name|long
name|version
decl_stmt|;
DECL|field|exists
specifier|private
name|boolean
name|exists
decl_stmt|;
DECL|field|fields
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
decl_stmt|;
DECL|field|sourceAsMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|sourceAsBytes
specifier|private
name|byte
index|[]
name|sourceAsBytes
decl_stmt|;
DECL|method|GetResult
name|GetResult
parameter_list|()
block|{     }
DECL|method|GetResult
specifier|public
name|GetResult
parameter_list|(
name|String
name|index
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
name|boolean
name|exists
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|exists
operator|=
name|exists
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fields
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Does the document exists.      */
DECL|method|isExists
specifier|public
name|boolean
name|isExists
parameter_list|()
block|{
return|return
name|exists
return|;
block|}
comment|/**      * The index the document was fetched from.      */
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**      * The type of the document.      */
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * The id of the document.      */
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * The version of the doc.      */
DECL|method|getVersion
specifier|public
name|long
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
comment|/**      * The source of the document if exists.      */
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|sourceAsBytes
operator|!=
literal|null
condition|)
block|{
return|return
name|sourceAsBytes
return|;
block|}
name|this
operator|.
name|sourceAsBytes
operator|=
name|sourceRef
argument_list|()
operator|.
name|toBytes
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|sourceAsBytes
return|;
block|}
comment|/**      * Returns bytes reference, also un compress the source if needed.      */
DECL|method|sourceRef
specifier|public
name|BytesReference
name|sourceRef
parameter_list|()
block|{
try|try
block|{
name|this
operator|.
name|source
operator|=
name|CompressorFactory
operator|.
name|uncompressIfNeeded
argument_list|(
name|this
operator|.
name|source
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|source
return|;
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
literal|"failed to decompress source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Internal source representation, might be compressed....      */
DECL|method|internalSourceRef
specifier|public
name|BytesReference
name|internalSourceRef
parameter_list|()
block|{
return|return
name|source
return|;
block|}
comment|/**      * Is the source empty (not available) or not.      */
DECL|method|isSourceEmpty
specifier|public
name|boolean
name|isSourceEmpty
parameter_list|()
block|{
return|return
name|source
operator|==
literal|null
return|;
block|}
comment|/**      * The source of the document (as a string).      */
DECL|method|sourceAsString
specifier|public
name|String
name|sourceAsString
parameter_list|()
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|BytesReference
name|source
init|=
name|sourceRef
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|source
argument_list|,
literal|false
argument_list|)
return|;
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
literal|"failed to convert source to a json string"
argument_list|)
throw|;
block|}
block|}
comment|/**      * The source of the document (As a map).      */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|sourceAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|()
throws|throws
name|ElasticsearchParseException
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|sourceAsMap
operator|!=
literal|null
condition|)
block|{
return|return
name|sourceAsMap
return|;
block|}
name|sourceAsMap
operator|=
name|SourceLookup
operator|.
name|sourceAsMap
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|sourceAsMap
return|;
block|}
DECL|method|getSource
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSource
parameter_list|()
block|{
return|return
name|sourceAsMap
argument_list|()
return|;
block|}
DECL|method|getFields
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|getFields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|field
specifier|public
name|GetField
name|field
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|fields
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|GetField
argument_list|>
name|iterator
parameter_list|()
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
return|return
name|emptyIterator
argument_list|()
return|;
block|}
return|return
name|fields
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|_INDEX
specifier|static
specifier|final
name|XContentBuilderString
name|_INDEX
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_index"
argument_list|)
decl_stmt|;
DECL|field|_TYPE
specifier|static
specifier|final
name|XContentBuilderString
name|_TYPE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_type"
argument_list|)
decl_stmt|;
DECL|field|_ID
specifier|static
specifier|final
name|XContentBuilderString
name|_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
DECL|field|_VERSION
specifier|static
specifier|final
name|XContentBuilderString
name|_VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_version"
argument_list|)
decl_stmt|;
DECL|field|FOUND
specifier|static
specifier|final
name|XContentBuilderString
name|FOUND
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"found"
argument_list|)
decl_stmt|;
DECL|field|FIELDS
specifier|static
specifier|final
name|XContentBuilderString
name|FIELDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
block|}
DECL|method|toXContentEmbedded
specifier|public
name|XContentBuilder
name|toXContentEmbedded
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
name|List
argument_list|<
name|GetField
argument_list|>
name|metaFields
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|GetField
argument_list|>
name|otherFields
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
operator|&&
operator|!
name|fields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|GetField
name|field
range|:
name|fields
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|field
operator|.
name|getValues
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|field
operator|.
name|isMetadataField
argument_list|()
condition|)
block|{
name|metaFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|otherFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|GetField
name|field
range|:
name|metaFields
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|,
name|field
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FOUND
argument_list|,
name|exists
argument_list|)
expr_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|XContentHelper
operator|.
name|writeRawField
argument_list|(
literal|"_source"
argument_list|,
name|source
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|otherFields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|FIELDS
argument_list|)
expr_stmt|;
for|for
control|(
name|GetField
name|field
range|:
name|otherFields
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|field
operator|.
name|getValues
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
operator|!
name|isExists
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
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
name|_ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FOUND
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
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
name|_ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VERSION
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
name|toXContentEmbedded
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|readGetResult
specifier|public
specifier|static
name|GetResult
name|readGetResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|GetResult
name|result
init|=
operator|new
name|GetResult
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
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
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|exists
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
if|if
condition|(
name|source
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|source
operator|=
literal|null
expr_stmt|;
block|}
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
operator|==
literal|0
condition|)
block|{
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|=
name|newHashMapWithExpectedSize
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
name|GetField
name|field
init|=
name|readGetField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|exists
argument_list|)
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|GetField
name|field
range|:
name|fields
operator|.
name|values
argument_list|()
control|)
block|{
name|field
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

