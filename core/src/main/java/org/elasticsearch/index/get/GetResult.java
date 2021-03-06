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
name|mapper
operator|.
name|SourceFieldMapper
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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
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
name|throwUnknownField
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
name|ToXContentObject
block|{
DECL|field|_INDEX
specifier|private
specifier|static
specifier|final
name|String
name|_INDEX
init|=
literal|"_index"
decl_stmt|;
DECL|field|_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|_TYPE
init|=
literal|"_type"
decl_stmt|;
DECL|field|_ID
specifier|private
specifier|static
specifier|final
name|String
name|_ID
init|=
literal|"_id"
decl_stmt|;
DECL|field|_VERSION
specifier|private
specifier|static
specifier|final
name|String
name|_VERSION
init|=
literal|"_version"
decl_stmt|;
DECL|field|FOUND
specifier|private
specifier|static
specifier|final
name|String
name|FOUND
init|=
literal|"found"
decl_stmt|;
DECL|field|FIELDS
specifier|private
specifier|static
specifier|final
name|String
name|FIELDS
init|=
literal|"fields"
decl_stmt|;
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
name|emptyMap
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
name|BytesReference
operator|.
name|toBytes
argument_list|(
name|sourceRef
argument_list|()
argument_list|)
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
name|Collections
operator|.
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
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|GetField
argument_list|>
name|otherFields
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|SourceFieldMapper
operator|.
name|NAME
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
name|field
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
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|_INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|_TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|_ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|isExists
argument_list|()
condition|)
block|{
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
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|FOUND
argument_list|,
literal|false
argument_list|)
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
DECL|method|fromXContentEmbedded
specifier|public
specifier|static
name|GetResult
name|fromXContentEmbedded
parameter_list|(
name|XContentParser
name|parser
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
name|nextToken
argument_list|()
decl_stmt|;
name|ensureExpectedToken
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
argument_list|,
name|token
argument_list|,
name|parser
operator|::
name|getTokenLocation
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|String
name|index
init|=
literal|null
decl_stmt|,
name|type
init|=
literal|null
decl_stmt|,
name|id
init|=
literal|null
decl_stmt|;
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|BytesReference
name|source
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
init|=
operator|new
name|HashMap
argument_list|<>
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|_INDEX
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|index
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|_TYPE
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|type
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|_ID
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|id
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|_VERSION
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|version
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|FOUND
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|found
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|.
name|put
argument_list|(
name|currentFieldName
argument_list|,
operator|new
name|GetField
argument_list|(
name|currentFieldName
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|parser
operator|.
name|objectText
argument_list|()
argument_list|)
argument_list|)
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
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|SourceFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|parser
operator|.
name|contentType
argument_list|()
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
comment|//the original document gets slightly modified: whitespaces or pretty printing are not preserved,
comment|//it all depends on the current builder settings
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|source
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|FIELDS
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
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
name|GetField
name|getField
init|=
name|GetField
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|getField
operator|.
name|getName
argument_list|()
argument_list|,
name|getField
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|throwUnknownField
argument_list|(
name|currentFieldName
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|GetResult
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|version
argument_list|,
name|found
argument_list|,
name|source
argument_list|,
name|fields
argument_list|)
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|GetResult
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
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
name|nextToken
argument_list|()
decl_stmt|;
name|ensureExpectedToken
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|token
argument_list|,
name|parser
operator|::
name|getTokenLocation
argument_list|)
expr_stmt|;
return|return
name|fromXContentEmbedded
argument_list|(
name|parser
argument_list|)
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
name|emptyMap
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|=
operator|new
name|HashMap
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
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|GetResult
name|getResult
init|=
operator|(
name|GetResult
operator|)
name|o
decl_stmt|;
return|return
name|version
operator|==
name|getResult
operator|.
name|version
operator|&&
name|exists
operator|==
name|getResult
operator|.
name|exists
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|index
argument_list|,
name|getResult
operator|.
name|index
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|getResult
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|id
argument_list|,
name|getResult
operator|.
name|id
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|fields
argument_list|,
name|getResult
operator|.
name|fields
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|sourceAsMap
argument_list|()
argument_list|,
name|getResult
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|version
argument_list|,
name|exists
argument_list|,
name|fields
argument_list|,
name|sourceAsMap
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

