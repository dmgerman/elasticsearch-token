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
name|BinaryDocValuesField
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
name|IndexableField
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
name|settings
operator|.
name|ImmutableSettings
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
name|settings
operator|.
name|Settings
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
name|docvaluesformat
operator|.
name|DocValuesFormatProvider
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
name|docvaluesformat
operator|.
name|DocValuesFormatService
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatService
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
name|fielddata
operator|.
name|FieldDataType
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
name|ParseContext
operator|.
name|Document
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
name|core
operator|.
name|TypeParsers
operator|.
name|parseField
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
DECL|field|INDEX_NAME
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
name|UidFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|FIELD_TYPE
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
DECL|field|NESTED_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|NESTED_FIELD_TYPE
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setIndexed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|FieldInfo
operator|.
name|IndexOptions
operator|.
name|DOCS_ONLY
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|NESTED_FIELD_TYPE
operator|=
operator|new
name|FieldType
argument_list|(
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|NESTED_FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|NESTED_FIELD_TYPE
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
name|AbstractFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|UidFieldMapper
argument_list|>
block|{
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
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|indexName
operator|=
name|Defaults
operator|.
name|INDEX_NAME
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
name|docValues
argument_list|,
name|postingsProvider
argument_list|,
name|docValuesProvider
argument_list|,
name|fieldDataSettings
argument_list|,
name|context
operator|.
name|indexSettings
argument_list|()
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
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
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
name|parseField
argument_list|(
name|builder
argument_list|,
name|builder
operator|.
name|name
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
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
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
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
name|Boolean
name|docValues
parameter_list|,
name|PostingsFormatProvider
name|postingsFormat
parameter_list|,
name|DocValuesFormatProvider
name|docValuesFormat
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|fieldDataSettings
parameter_list|,
name|Settings
name|indexSettings
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
name|FIELD_TYPE
argument_list|)
argument_list|,
name|docValues
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
argument_list|,
name|docValuesFormat
argument_list|,
literal|null
argument_list|,
name|fieldDataSettings
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldType
specifier|public
name|FieldType
name|defaultFieldType
parameter_list|()
block|{
return|return
name|Defaults
operator|.
name|FIELD_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldDataType
specifier|public
name|FieldDataType
name|defaultFieldDataType
parameter_list|()
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"string"
argument_list|)
return|;
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
literal|"default"
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
specifier|final
name|IndexableField
name|uidField
init|=
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
specifier|final
name|Document
name|doc
init|=
name|context
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|doc
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
name|stringValue
argument_list|()
argument_list|,
name|Defaults
operator|.
name|NESTED_FIELD_TYPE
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
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|Field
name|uid
init|=
operator|new
name|Field
argument_list|(
name|NAME
argument_list|,
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
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
name|context
operator|.
name|uid
argument_list|(
name|uid
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|BinaryDocValuesField
argument_list|(
name|NAME
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|uid
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|Uid
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Uid
operator|.
name|createUid
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
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
name|boolean
name|includeDefaults
init|=
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"include_defaults"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// if defaults, don't output
if|if
condition|(
operator|!
name|includeDefaults
operator|&&
name|customFieldDataSettings
operator|==
literal|null
operator|&&
operator|(
name|postingsFormat
operator|==
literal|null
operator|||
name|postingsFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultPostingFormat
argument_list|()
argument_list|)
operator|)
operator|&&
operator|(
name|docValuesFormat
operator|==
literal|null
operator|||
name|docValuesFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultDocValuesFormat
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
name|builder
return|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|postingsFormat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|includeDefaults
operator|||
operator|!
name|postingsFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultPostingFormat
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"postings_format"
argument_list|,
name|postingsFormat
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|String
name|format
init|=
name|defaultPostingFormat
argument_list|()
decl_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|format
operator|=
name|PostingsFormatService
operator|.
name|DEFAULT_FORMAT
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"postings_format"
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|docValuesFormat
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|includeDefaults
operator|||
operator|!
name|docValuesFormat
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|defaultDocValuesFormat
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|DOC_VALUES_FORMAT
argument_list|,
name|docValuesFormat
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|String
name|format
init|=
name|defaultDocValuesFormat
argument_list|()
decl_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|format
operator|=
name|DocValuesFormatService
operator|.
name|DEFAULT_FORMAT
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|DOC_VALUES_FORMAT
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|customFieldDataSettings
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fielddata"
argument_list|,
operator|(
name|Map
operator|)
name|customFieldDataSettings
operator|.
name|getAsMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fielddata"
argument_list|,
operator|(
name|Map
operator|)
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
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
name|AbstractFieldMapper
argument_list|<
name|?
argument_list|>
name|fieldMergeWith
init|=
operator|(
name|AbstractFieldMapper
argument_list|<
name|?
argument_list|>
operator|)
name|mergeWith
decl_stmt|;
comment|// do nothing here, no merging, but also no exception
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
comment|// apply changeable values
if|if
condition|(
name|fieldMergeWith
operator|.
name|postingsFormatProvider
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|postingsFormat
operator|=
name|fieldMergeWith
operator|.
name|postingsFormatProvider
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

