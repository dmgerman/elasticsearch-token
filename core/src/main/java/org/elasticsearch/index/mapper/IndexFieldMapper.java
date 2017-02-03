begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|IndexFieldData
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
name|plain
operator|.
name|IndexIndexFieldData
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
DECL|class|IndexFieldMapper
specifier|public
class|class
name|IndexFieldMapper
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
literal|"_index"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_index"
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
name|IndexFieldMapper
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
name|IndexFieldType
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
name|NONE
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
literal|false
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
name|setName
argument_list|(
name|NAME
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
name|MetadataFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|IndexFieldMapper
argument_list|>
block|{
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|MappedFieldType
name|existing
parameter_list|)
block|{
name|super
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|existing
operator|==
literal|null
condition|?
name|Defaults
operator|.
name|FIELD_TYPE
else|:
name|existing
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|IndexFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
operator|new
name|IndexFieldMapper
argument_list|(
name|fieldType
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
name|MetadataFieldMapper
operator|.
name|TypeParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|MetadataFieldMapper
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
if|if
condition|(
name|parserContext
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_0_0_alpha3
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
name|NAME
operator|+
literal|" is not configurable"
argument_list|)
throw|;
block|}
return|return
operator|new
name|Builder
argument_list|(
name|parserContext
operator|.
name|mapperService
argument_list|()
operator|.
name|fullName
argument_list|(
name|NAME
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDefault
specifier|public
name|MetadataFieldMapper
name|getDefault
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|,
name|ParserContext
name|context
parameter_list|)
block|{
specifier|final
name|Settings
name|indexSettings
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
decl_stmt|;
return|return
operator|new
name|IndexFieldMapper
argument_list|(
name|indexSettings
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
block|}
DECL|class|IndexFieldType
specifier|static
specifier|final
class|class
name|IndexFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|IndexFieldType
name|IndexFieldType
parameter_list|()
block|{}
DECL|method|IndexFieldType
specifier|protected
name|IndexFieldType
parameter_list|(
name|IndexFieldType
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
name|IndexFieldType
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
DECL|method|isSearchable
specifier|public
name|boolean
name|isSearchable
parameter_list|()
block|{
comment|// The _index field is always searchable.
return|return
literal|true
return|;
block|}
comment|/**          * This termQuery impl looks at the context to determine the index that          * is being queried and then returns a MATCH_ALL_QUERY or MATCH_NO_QUERY          * if the value matches this index. This can be useful if aliases or          * wildcards are used but the aim is to restrict the query to specific          * indices          */
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
if|if
condition|(
name|isSameIndex
argument_list|(
name|value
argument_list|,
name|context
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|(
literal|"Index didn't match. Index queried: "
operator|+
name|context
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" vs. "
operator|+
name|value
argument_list|)
return|;
block|}
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
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|isSameIndex
argument_list|(
name|value
argument_list|,
name|context
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// No need to OR these clauses - we can only logically be
comment|// running in the context of just one of these index names.
return|return
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
return|;
block|}
block|}
comment|// None of the listed index names are this one
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|(
literal|"Index didn't match. Index queried: "
operator|+
name|context
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" vs. "
operator|+
name|values
argument_list|)
return|;
block|}
DECL|method|isSameIndex
specifier|private
name|boolean
name|isSameIndex
parameter_list|(
name|Object
name|value
parameter_list|,
name|String
name|indexName
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
name|indexNameRef
init|=
operator|new
name|BytesRef
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
return|return
operator|(
name|indexNameRef
operator|.
name|bytesEquals
argument_list|(
operator|(
name|BytesRef
operator|)
name|value
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
name|indexName
operator|.
name|equals
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|fielddataBuilder
specifier|public
name|IndexFieldData
operator|.
name|Builder
name|fielddataBuilder
parameter_list|()
block|{
return|return
operator|new
name|IndexIndexFieldData
operator|.
name|Builder
argument_list|()
return|;
block|}
block|}
DECL|method|IndexFieldMapper
specifier|private
name|IndexFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|,
name|MappedFieldType
name|existing
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
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|IndexFieldMapper
specifier|private
name|IndexFieldMapper
parameter_list|(
name|MappedFieldType
name|fieldType
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
block|{}
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
block|{}
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
name|IndexableField
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{}
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
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doMerge
specifier|protected
name|void
name|doMerge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
comment|// nothing to do
block|}
block|}
end_class

end_unit

