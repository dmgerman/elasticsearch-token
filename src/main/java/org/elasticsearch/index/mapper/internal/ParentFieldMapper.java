begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
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
name|IndexOptions
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
name|queries
operator|.
name|TermFilter
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
name|queries
operator|.
name|TermsFilter
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
name|search
operator|.
name|ConstantScoreQuery
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
name|search
operator|.
name|Filter
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
name|search
operator|.
name|Query
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
name|Version
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
name|lucene
operator|.
name|BytesRefs
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
name|search
operator|.
name|Queries
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
name|settings
operator|.
name|loader
operator|.
name|SettingsLoader
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
name|DocumentMapper
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
name|InternalMapper
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
name|Mapper
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
name|MapperParsingException
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
name|MergeContext
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
name|MergeMappingException
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
name|RootMapper
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
name|Uid
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryParseContext
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|builder
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeMapValue
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
name|parent
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ParentFieldMapper
specifier|public
class|class
name|ParentFieldMapper
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
literal|"_parent"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_parent"
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
name|ParentFieldMapper
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
static|static
block|{
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
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
name|ParentFieldMapper
argument_list|>
block|{
DECL|field|indexName
specifier|protected
name|String
name|indexName
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|fieldDataSettings
specifier|protected
name|Settings
name|fieldDataSettings
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
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|Builder
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|fieldDataSettings
specifier|public
name|Builder
name|fieldDataSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|fieldDataSettings
operator|=
name|settings
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|ParentFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Parent mapping must contain the parent type"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ParentFieldMapper
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|type
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
name|ParentFieldMapper
operator|.
name|Builder
name|builder
init|=
name|parent
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
init|=
name|node
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
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
literal|"type"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|type
argument_list|(
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"postings_format"
argument_list|)
operator|&&
name|parserContext
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
condition|)
block|{
comment|// ignore before 2.0, reject on and after 2.0
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"fielddata"
argument_list|)
condition|)
block|{
comment|// Only take over `loading`, since that is the only option now that is configurable:
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|fieldDataSettings
init|=
name|SettingsLoader
operator|.
name|Helper
operator|.
name|loadNestedFromMap
argument_list|(
name|nodeMapValue
argument_list|(
name|fieldNode
argument_list|,
literal|"fielddata"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldDataSettings
operator|.
name|containsKey
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|)
condition|)
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|,
name|fieldDataSettings
operator|.
name|get
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|builder
operator|.
name|fieldDataSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|typeAsBytes
specifier|private
specifier|final
name|BytesRef
name|typeAsBytes
decl_stmt|;
DECL|method|ParentFieldMapper
specifier|protected
name|ParentFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|String
name|type
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
literal|null
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|fieldDataSettings
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|typeAsBytes
operator|=
name|type
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BytesRef
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
DECL|method|ParentFieldMapper
specifier|public
name|ParentFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|NAME
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldDataType
operator|=
operator|new
name|FieldDataType
argument_list|(
literal|"_parent"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|,
name|Loading
operator|.
name|LAZY_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
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
literal|"_parent"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Loading
operator|.
name|KEY
argument_list|,
name|Loading
operator|.
name|EAGER_VALUE
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hasDocValues
specifier|public
name|boolean
name|hasDocValues
parameter_list|()
block|{
return|return
literal|false
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
block|{     }
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
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|includeInObject
specifier|public
name|boolean
name|includeInObject
parameter_list|()
block|{
return|return
literal|true
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
if|if
condition|(
operator|!
name|active
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentName
argument_list|()
operator|!=
literal|null
operator|&&
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentName
argument_list|()
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
condition|)
block|{
comment|// we are in the parsing of _parent phase
name|String
name|parentId
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|text
argument_list|()
decl_stmt|;
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|parent
argument_list|(
name|parentId
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
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
name|type
argument_list|,
name|parentId
argument_list|)
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise, we are running it post processing of the xcontent
name|String
name|parsedParentId
init|=
name|context
operator|.
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|parent
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|String
name|parentId
init|=
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|parent
argument_list|()
decl_stmt|;
if|if
condition|(
name|parsedParentId
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parentId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No parent id provided, not within the document, and not externally"
argument_list|)
throw|;
block|}
comment|// we did not add it in the parsing phase, add it now
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
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
name|type
argument_list|,
name|parentId
argument_list|)
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parentId
operator|!=
literal|null
operator|&&
operator|!
name|parsedParentId
operator|.
name|equals
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
name|type
argument_list|,
name|parentId
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Parent id mismatch, document value is ["
operator|+
name|Uid
operator|.
name|createUid
argument_list|(
name|parsedParentId
argument_list|)
operator|.
name|id
argument_list|()
operator|+
literal|"], while external value is ["
operator|+
name|parentId
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|// we have parent mapping, yet no value was set, ignore it...
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
annotation|@
name|Override
DECL|method|valueForSearch
specifier|public
name|Object
name|valueForSearch
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
name|String
name|sValue
init|=
name|value
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|sValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|index
init|=
name|sValue
operator|.
name|indexOf
argument_list|(
name|Uid
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|sValue
return|;
block|}
return|return
name|sValue
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|indexedValueForSearch
specifier|public
name|BytesRef
name|indexedValueForSearch
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
name|BytesRef
name|bytesRef
init|=
operator|(
name|BytesRef
operator|)
name|value
decl_stmt|;
if|if
condition|(
name|Uid
operator|.
name|hasDelimiter
argument_list|(
name|bytesRef
argument_list|)
condition|)
block|{
return|return
name|bytesRef
return|;
block|}
return|return
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|typeAsBytes
argument_list|,
name|bytesRef
argument_list|)
return|;
block|}
name|String
name|sValue
init|=
name|value
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|sValue
operator|.
name|indexOf
argument_list|(
name|Uid
operator|.
name|DELIMITER
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|type
argument_list|,
name|sValue
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|indexedValueForSearch
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termQuery
specifier|public
name|Query
name|termQuery
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termQuery
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|termFilter
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termFilter
specifier|public
name|Filter
name|termFilter
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termFilter
argument_list|(
name|value
argument_list|,
name|context
argument_list|)
return|;
block|}
name|BytesRef
name|bValue
init|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|Uid
operator|.
name|hasDelimiter
argument_list|(
name|bValue
argument_list|)
condition|)
block|{
return|return
operator|new
name|TermFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|bValue
argument_list|)
argument_list|)
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|types
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|types
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|DocumentMapper
name|documentMapper
range|:
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|false
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|documentMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|active
argument_list|()
condition|)
block|{
name|types
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|types
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Queries
operator|.
name|MATCH_NO_FILTER
return|;
block|}
elseif|else
if|if
condition|(
name|types
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
operator|new
name|TermFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|types
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|bValue
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
comment|// we use all non child types, cause we don't know if its exact or not...
name|List
argument_list|<
name|BytesRef
argument_list|>
name|typesValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|types
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|type
range|:
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|types
argument_list|()
control|)
block|{
name|typesValues
operator|.
name|add
argument_list|(
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|type
argument_list|,
name|bValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TermsFilter
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|typesValues
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|termsFilter
specifier|public
name|Filter
name|termsFilter
parameter_list|(
name|List
name|values
parameter_list|,
annotation|@
name|Nullable
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|termsFilter
argument_list|(
name|values
argument_list|,
name|context
argument_list|)
return|;
block|}
comment|// This will not be invoked if values is empty, so don't check for empty
if|if
condition|(
name|values
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|termFilter
argument_list|(
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|context
argument_list|)
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|types
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|types
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|DocumentMapper
name|documentMapper
range|:
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|false
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|documentMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|active
argument_list|()
condition|)
block|{
name|types
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|BytesRef
argument_list|>
name|bValues
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|values
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
name|BytesRef
name|bValue
init|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|Uid
operator|.
name|hasDelimiter
argument_list|(
name|bValue
argument_list|)
condition|)
block|{
name|bValues
operator|.
name|add
argument_list|(
name|bValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we use all non child types, cause we don't know if its exact or not...
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|bValues
operator|.
name|add
argument_list|(
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|type
argument_list|,
name|bValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|TermsFilter
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|bValues
argument_list|)
return|;
block|}
comment|/**      * We don't need to analyzer the text, and we need to convert it to UID...      */
annotation|@
name|Override
DECL|method|useTermQueryWithQueryString
specifier|public
name|boolean
name|useTermQueryWithQueryString
parameter_list|()
block|{
return|return
literal|true
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
if|if
condition|(
operator|!
name|active
argument_list|()
condition|)
block|{
return|return
name|builder
return|;
block|}
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
name|builder
operator|.
name|startObject
argument_list|(
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|type
argument_list|)
expr_stmt|;
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
name|ParentFieldMapper
name|other
init|=
operator|(
name|ParentFieldMapper
operator|)
name|mergeWith
decl_stmt|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equal
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
condition|)
block|{
name|mergeContext
operator|.
name|addConflict
argument_list|(
literal|"The _parent field's type option can't be changed"
argument_list|)
expr_stmt|;
block|}
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
name|ParentFieldMapper
name|fieldMergeWith
init|=
operator|(
name|ParentFieldMapper
operator|)
name|mergeWith
decl_stmt|;
if|if
condition|(
name|fieldMergeWith
operator|.
name|customFieldDataSettings
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|Objects
operator|.
name|equal
argument_list|(
name|fieldMergeWith
operator|.
name|customFieldDataSettings
argument_list|,
name|this
operator|.
name|customFieldDataSettings
argument_list|)
condition|)
block|{
name|this
operator|.
name|customFieldDataSettings
operator|=
name|fieldMergeWith
operator|.
name|customFieldDataSettings
expr_stmt|;
name|this
operator|.
name|fieldDataType
operator|=
operator|new
name|FieldDataType
argument_list|(
name|defaultFieldDataType
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|defaultFieldDataType
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|this
operator|.
name|customFieldDataSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * @return Whether the _parent field is actually used.      */
DECL|method|active
specifier|public
name|boolean
name|active
parameter_list|()
block|{
return|return
name|type
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit

