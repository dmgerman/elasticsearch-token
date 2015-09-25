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
name|SortedDocValuesField
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
name|DocValuesType
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
name|queries
operator|.
name|TermsQuery
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
name|MappedFieldType
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
name|MergeResult
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
name|MetadataFieldMapper
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
name|query
operator|.
name|QueryShardContext
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ParentFieldMapper
specifier|public
class|class
name|ParentFieldMapper
extends|extends
name|MetadataFieldMapper
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
name|MappedFieldType
name|FIELD_TYPE
init|=
operator|new
name|ParentFieldType
argument_list|()
decl_stmt|;
DECL|field|JOIN_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|MappedFieldType
name|JOIN_FIELD_TYPE
init|=
operator|new
name|ParentFieldType
argument_list|()
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
name|setIndexAnalyzer
argument_list|(
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setSearchAnalyzer
argument_list|(
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|JOIN_FIELD_TYPE
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|JOIN_FIELD_TYPE
operator|.
name|setDocValuesType
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
expr_stmt|;
name|JOIN_FIELD_TYPE
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
name|MetadataFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|ParentFieldMapper
argument_list|>
block|{
DECL|field|parentType
specifier|private
name|String
name|parentType
decl_stmt|;
DECL|field|indexName
specifier|protected
name|String
name|indexName
decl_stmt|;
DECL|field|documentType
specifier|private
specifier|final
name|String
name|documentType
decl_stmt|;
DECL|field|parentJoinFieldType
specifier|private
specifier|final
name|MappedFieldType
name|parentJoinFieldType
init|=
name|Defaults
operator|.
name|JOIN_FIELD_TYPE
operator|.
name|clone
argument_list|()
decl_stmt|;
DECL|field|childJoinFieldType
specifier|private
specifier|final
name|MappedFieldType
name|childJoinFieldType
init|=
name|Defaults
operator|.
name|JOIN_FIELD_TYPE
operator|.
name|clone
argument_list|()
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|String
name|documentType
parameter_list|)
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
name|this
operator|.
name|indexName
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|documentType
operator|=
name|documentType
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
name|parentType
operator|=
name|type
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|fieldDataSettings
specifier|public
name|Builder
name|fieldDataSettings
parameter_list|(
name|Settings
name|fieldDataSettings
parameter_list|)
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|childJoinFieldType
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|fieldDataSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|childJoinFieldType
operator|.
name|setFieldDataType
argument_list|(
operator|new
name|FieldDataType
argument_list|(
name|childJoinFieldType
operator|.
name|fieldDataType
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|settings
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
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
name|parentType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"[_parent] field mapping must contain the [type] option"
argument_list|)
throw|;
block|}
name|parentJoinFieldType
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|joinField
argument_list|(
name|documentType
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|parentJoinFieldType
operator|.
name|setFieldDataType
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|childJoinFieldType
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|joinField
argument_list|(
name|parentType
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ParentFieldMapper
argument_list|(
name|fieldType
argument_list|,
name|parentJoinFieldType
argument_list|,
name|childJoinFieldType
argument_list|,
name|parentType
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
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|parserContext
operator|.
name|type
argument_list|()
argument_list|)
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
name|V_2_0_0_beta1
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
name|MappedFieldType
operator|.
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
name|MappedFieldType
operator|.
name|Loading
operator|.
name|KEY
argument_list|,
name|fieldDataSettings
operator|.
name|get
argument_list|(
name|MappedFieldType
operator|.
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
DECL|class|ParentFieldType
specifier|static
specifier|final
class|class
name|ParentFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|ParentFieldType
specifier|public
name|ParentFieldType
parameter_list|()
block|{
name|setFieldDataType
argument_list|(
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
name|MappedFieldType
operator|.
name|Loading
operator|.
name|KEY
argument_list|,
name|Loading
operator|.
name|EAGER_VALUE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|ParentFieldType
specifier|protected
name|ParentFieldType
parameter_list|(
name|ParentFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|ParentFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
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
comment|/**          * We don't need to analyzer the text, and we need to convert it to UID...          */
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
name|QueryShardContext
name|context
parameter_list|)
block|{
return|return
name|termsQuery
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|value
argument_list|)
argument_list|,
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termsQuery
specifier|public
name|Query
name|termsQuery
parameter_list|(
name|List
name|values
parameter_list|,
annotation|@
name|Nullable
name|QueryShardContext
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
name|termsQuery
argument_list|(
name|values
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
name|TermsQuery
argument_list|(
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|bValues
argument_list|)
return|;
block|}
block|}
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
comment|// determines the field data settings
DECL|field|childJoinFieldType
specifier|private
name|MappedFieldType
name|childJoinFieldType
decl_stmt|;
comment|// has no impact of field data settings, is just here for creating a join field, the parent field mapper in the child type pointing to this type determines the field data settings for this join field
DECL|field|parentJoinFieldType
specifier|private
specifier|final
name|MappedFieldType
name|parentJoinFieldType
decl_stmt|;
DECL|method|ParentFieldMapper
specifier|protected
name|ParentFieldMapper
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|parentJoinFieldType
parameter_list|,
name|MappedFieldType
name|childJoinFieldType
parameter_list|,
name|String
name|parentType
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|NAME
argument_list|,
name|fieldType
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|parentJoinFieldType
operator|=
name|parentJoinFieldType
expr_stmt|;
name|this
operator|.
name|parentJoinFieldType
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|this
operator|.
name|childJoinFieldType
operator|=
name|childJoinFieldType
expr_stmt|;
if|if
condition|(
name|childJoinFieldType
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|childJoinFieldType
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|ParentFieldMapper
specifier|public
name|ParentFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|,
name|MappedFieldType
name|existing
parameter_list|,
name|String
name|parentType
parameter_list|)
block|{
name|this
argument_list|(
name|existing
operator|==
literal|null
condition|?
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|clone
argument_list|()
else|:
name|existing
operator|.
name|clone
argument_list|()
argument_list|,
name|joinFieldTypeForParentType
argument_list|(
name|parentType
argument_list|,
name|indexSettings
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|joinFieldTypeForParentType
specifier|private
specifier|static
name|MappedFieldType
name|joinFieldTypeForParentType
parameter_list|(
name|String
name|parentType
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|MappedFieldType
name|parentJoinFieldType
init|=
name|Defaults
operator|.
name|JOIN_FIELD_TYPE
operator|.
name|clone
argument_list|()
decl_stmt|;
name|parentJoinFieldType
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|joinField
argument_list|(
name|parentType
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|parentJoinFieldType
operator|.
name|freeze
argument_list|()
expr_stmt|;
return|return
name|parentJoinFieldType
return|;
block|}
DECL|method|getParentJoinFieldType
specifier|public
name|MappedFieldType
name|getParentJoinFieldType
parameter_list|()
block|{
return|return
name|parentJoinFieldType
return|;
block|}
DECL|method|getChildJoinFieldType
specifier|public
name|MappedFieldType
name|getChildJoinFieldType
parameter_list|()
block|{
return|return
name|childJoinFieldType
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|parentType
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
if|if
condition|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|flyweight
argument_list|()
operator|==
literal|false
condition|)
block|{
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
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
name|boolean
name|parent
init|=
name|context
operator|.
name|docMapper
argument_list|()
operator|.
name|isParent
argument_list|(
name|context
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
condition|)
block|{
name|addJoinFieldIfNeeded
argument_list|(
name|fields
argument_list|,
name|parentJoinFieldType
argument_list|,
name|context
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
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
name|parentType
argument_list|,
name|parentId
argument_list|)
argument_list|,
name|fieldType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addJoinFieldIfNeeded
argument_list|(
name|fields
argument_list|,
name|childJoinFieldType
argument_list|,
name|parentId
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
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
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
name|parentType
argument_list|,
name|parentId
argument_list|)
argument_list|,
name|fieldType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|addJoinFieldIfNeeded
argument_list|(
name|fields
argument_list|,
name|childJoinFieldType
argument_list|,
name|parentId
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
name|parentType
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
DECL|method|addJoinFieldIfNeeded
specifier|private
name|void
name|addJoinFieldIfNeeded
parameter_list|(
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|String
name|id
parameter_list|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|SortedDocValuesField
argument_list|(
name|fieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|id
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|joinField
specifier|public
specifier|static
name|String
name|joinField
parameter_list|(
name|String
name|parentType
parameter_list|)
block|{
return|return
name|ParentFieldMapper
operator|.
name|NAME
operator|+
literal|"#"
operator|+
name|parentType
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
DECL|method|joinFieldHasCustomFieldDataSettings
specifier|private
name|boolean
name|joinFieldHasCustomFieldDataSettings
parameter_list|()
block|{
return|return
name|childJoinFieldType
operator|!=
literal|null
operator|&&
name|childJoinFieldType
operator|.
name|fieldDataType
argument_list|()
operator|!=
literal|null
operator|&&
name|childJoinFieldType
operator|.
name|fieldDataType
argument_list|()
operator|.
name|equals
argument_list|(
name|Defaults
operator|.
name|JOIN_FIELD_TYPE
operator|.
name|fieldDataType
argument_list|()
argument_list|)
operator|==
literal|false
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
name|parentType
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
name|joinFieldHasCustomFieldDataSettings
argument_list|()
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
name|childJoinFieldType
operator|.
name|fieldDataType
argument_list|()
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
name|MergeResult
name|mergeResult
parameter_list|)
throws|throws
name|MergeMappingException
block|{
name|super
operator|.
name|merge
argument_list|(
name|mergeWith
argument_list|,
name|mergeResult
argument_list|)
expr_stmt|;
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
name|Objects
operator|.
name|equals
argument_list|(
name|parentType
argument_list|,
name|fieldMergeWith
operator|.
name|parentType
argument_list|)
operator|==
literal|false
condition|)
block|{
name|mergeResult
operator|.
name|addConflict
argument_list|(
literal|"The _parent field's type option can't be changed: ["
operator|+
name|parentType
operator|+
literal|"]->["
operator|+
name|fieldMergeWith
operator|.
name|parentType
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|conflicts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|fieldType
argument_list|()
operator|.
name|checkCompatibility
argument_list|(
name|fieldMergeWith
operator|.
name|fieldType
argument_list|()
argument_list|,
name|conflicts
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// always strict, this cannot change
name|parentJoinFieldType
operator|.
name|checkCompatibility
argument_list|(
name|fieldMergeWith
operator|.
name|parentJoinFieldType
argument_list|,
name|conflicts
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// same here
if|if
condition|(
name|childJoinFieldType
operator|!=
literal|null
condition|)
block|{
comment|// TODO: this can be set to false when the old parent/child impl is removed, we can do eager global ordinals loading per type.
name|childJoinFieldType
operator|.
name|checkCompatibility
argument_list|(
name|fieldMergeWith
operator|.
name|childJoinFieldType
argument_list|,
name|conflicts
argument_list|,
name|mergeResult
operator|.
name|updateAllTypes
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|conflict
range|:
name|conflicts
control|)
block|{
name|mergeResult
operator|.
name|addConflict
argument_list|(
name|conflict
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|active
argument_list|()
operator|&&
name|mergeResult
operator|.
name|simulate
argument_list|()
operator|==
literal|false
operator|&&
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
operator|==
literal|false
condition|)
block|{
name|childJoinFieldType
operator|=
name|fieldMergeWith
operator|.
name|childJoinFieldType
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * @return Whether the _parent field is actually configured.      */
DECL|method|active
specifier|public
name|boolean
name|active
parameter_list|()
block|{
return|return
name|parentType
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit

