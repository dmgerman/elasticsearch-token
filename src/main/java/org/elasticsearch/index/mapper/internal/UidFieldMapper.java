begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|FieldType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|FieldInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
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
name|lucene
operator|.
name|Lucene
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
name|lucene
operator|.
name|uid
operator|.
name|UidField
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
name|index
operator|.
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|*
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
name|core
operator|.
name|AbstractFieldMapper
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
name|Map
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
name|mapper
operator|.
name|MapperBuilders
operator|.
name|uid
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|UidFieldMapper
specifier|public
class|class
name|UidFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|Uid
argument_list|>
implements|implements
name|InternalMapper
implements|,
name|RootMapper
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_uid"
operator|.
name|intern
argument_list|()
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_uid"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|AbstractFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|UidFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|UID_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|UID_FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|(
name|AbstractFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
static|static
block|{
name|UID_FIELD_TYPE
operator|.
name|setIndexed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UID_FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|UID_FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UID_FIELD_TYPE
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UID_FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|FieldInfo
operator|.
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|)
expr_stmt|;
comment|// we store payload (otherwise, we really need just docs)
name|UID_FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|Mapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|UidFieldMapper
argument_list|>
block|{
DECL|field|indexName
specifier|protected
name|String
name|indexName
decl_stmt|;
DECL|field|postingsFormat
specifier|protected
name|PostingsFormatProvider
name|postingsFormat
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexName
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|UidFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|UidFieldMapper
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|postingsFormat
argument_list|)
return|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|Mapper
operator|.
name|TypeParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
operator|.
name|Builder
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Builder
name|builder
init|=
name|uid
argument_list|()
decl_stmt|;
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
name|entry
range|:
name|node
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"postings_format"
argument_list|)
condition|)
block|{
name|String
name|postingFormatName
init|=
name|fieldNode
operator|.
name|toString
argument_list|()
decl_stmt|;
name|builder
operator|.
name|postingsFormat
operator|=
name|parserContext
operator|.
name|postingFormatService
argument_list|()
operator|.
name|get
argument_list|(
name|postingFormatName
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|fieldCache
specifier|private
name|ThreadLocal
argument_list|<
name|UidField
argument_list|>
name|fieldCache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|UidField
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|UidField
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|UidField
argument_list|(
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|UidFieldMapper
specifier|public
name|UidFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|UidFieldMapper
specifier|protected
name|UidFieldMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|name
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|UidFieldMapper
specifier|protected
name|UidFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|PostingsFormatProvider
name|postingsFormat
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|indexName
argument_list|,
name|name
argument_list|)
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
operator|new
name|FieldType
argument_list|(
name|Defaults
operator|.
name|UID_FIELD_TYPE
argument_list|)
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|postingsFormat
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|defaultPostingFormat
specifier|protected
name|String
name|defaultPostingFormat
parameter_list|()
block|{
return|return
literal|"bloom_default"
return|;
block|}
annotation|@
name|Override
DECL|method|preParse
specifier|public
name|void
name|preParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if we have the id provided, fill it, and parse now
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|id
argument_list|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|postParse
specifier|public
name|void
name|postParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|id
argument_list|()
operator|==
literal|null
operator|&&
operator|!
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|flyweight
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No id found while parsing the content source"
argument_list|)
throw|;
block|}
comment|// if we did not have the id as part of the sourceToParse, then we need to parse it here
comment|// it would have been filled in the _id parse phase
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
operator|==
literal|null
condition|)
block|{
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
comment|// since we did not have the uid in the pre phase, we did not add it automatically to the nested docs
comment|// as they were created we need to make sure we add it to all the nested docs...
if|if
condition|(
name|context
operator|.
name|docs
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|UidField
name|uidField
init|=
operator|(
name|UidField
operator|)
name|context
operator|.
name|rootDoc
argument_list|()
operator|.
name|getField
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
assert|assert
name|uidField
operator|!=
literal|null
assert|;
comment|// we need to go over the docs and add it...
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|context
operator|.
name|docs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// we don't need to add it as a full uid field in nested docs, since we don't need versioning
name|context
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|uidField
operator|.
name|uid
argument_list|()
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|,
name|Field
operator|.
name|Index
operator|.
name|NOT_ANALYZED
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do here, we either do it in post parse, or in pre parse.
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|void
name|validate
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|MapperParsingException
block|{     }
annotation|@
name|Override
DECL|method|includeInObject
specifier|public
name|boolean
name|includeInObject
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|Field
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// so, caching uid stream and field is fine
comment|// since we don't do any mapping parsing without immediate indexing
comment|// and, when percolating, we don't index the uid
name|UidField
name|field
init|=
name|fieldCache
operator|.
name|get
argument_list|()
decl_stmt|;
name|field
operator|.
name|setUid
argument_list|(
name|Uid
operator|.
name|createUid
argument_list|(
name|context
operator|.
name|stringBuilder
argument_list|()
argument_list|,
name|context
operator|.
name|type
argument_list|()
argument_list|,
name|context
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|context
operator|.
name|uid
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|field
return|;
comment|// version get updated by the engine
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|Uid
name|value
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
return|return
name|Uid
operator|.
name|createUid
argument_list|(
name|field
operator|.
name|stringValue
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|valueFromString
specifier|public
name|Uid
name|valueFromString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|Uid
operator|.
name|createUid
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|valueAsString
specifier|public
name|String
name|valueAsString
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
return|return
name|field
operator|.
name|stringValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indexedValue
specifier|public
name|String
name|indexedValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
DECL|method|term
specifier|public
name|Term
name|term
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|term
argument_list|(
name|Uid
operator|.
name|createUid
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
argument_list|)
return|;
block|}
DECL|method|term
specifier|public
name|Term
name|term
parameter_list|(
name|String
name|uid
parameter_list|)
block|{
return|return
name|names
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|uid
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|fieldCache
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
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
comment|// for now, don't output it at all
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
comment|// do nothing here, no merging, but also no exception
block|}
block|}
end_class

end_unit

