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
name|SortedSetDocValuesField
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
name|IndexReader
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
name|index
operator|.
name|TermContext
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
name|BooleanClause
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
name|BooleanQuery
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
name|MatchAllDocsQuery
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
name|search
operator|.
name|TermQuery
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
name|DocValuesIndexFieldData
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
name|PagedBytesIndexFieldData
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|TypeFieldMapper
specifier|public
class|class
name|TypeFieldMapper
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
literal|"_type"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_type"
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
name|TypeFieldMapper
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
name|TypeFieldType
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
annotation|@
name|Override
DECL|method|getDefault
specifier|public
name|MetadataFieldMapper
name|getDefault
parameter_list|(
name|Settings
name|indexSettings
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|String
name|typeName
parameter_list|)
block|{
return|return
operator|new
name|TypeFieldMapper
argument_list|(
name|indexSettings
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
block|}
DECL|class|TypeFieldType
specifier|static
specifier|final
class|class
name|TypeFieldType
extends|extends
name|StringFieldType
block|{
DECL|field|fielddata
specifier|private
name|boolean
name|fielddata
decl_stmt|;
DECL|method|TypeFieldType
specifier|public
name|TypeFieldType
parameter_list|()
block|{
name|this
operator|.
name|fielddata
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|TypeFieldType
specifier|protected
name|TypeFieldType
parameter_list|(
name|TypeFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
name|this
operator|.
name|fielddata
operator|=
name|ref
operator|.
name|fielddata
expr_stmt|;
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
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TypeFieldType
name|that
init|=
operator|(
name|TypeFieldType
operator|)
name|o
decl_stmt|;
return|return
name|fielddata
operator|==
name|that
operator|.
name|fielddata
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
name|super
operator|.
name|hashCode
argument_list|()
argument_list|,
name|fielddata
argument_list|)
return|;
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
name|TypeFieldType
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
DECL|method|fielddata
specifier|public
name|boolean
name|fielddata
parameter_list|()
block|{
return|return
name|fielddata
return|;
block|}
DECL|method|setFielddata
specifier|public
name|void
name|setFielddata
parameter_list|(
name|boolean
name|fielddata
parameter_list|)
block|{
name|checkIfFrozen
argument_list|()
expr_stmt|;
name|this
operator|.
name|fielddata
operator|=
name|fielddata
expr_stmt|;
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
if|if
condition|(
name|hasDocValues
argument_list|()
condition|)
block|{
return|return
operator|new
name|DocValuesIndexFieldData
operator|.
name|Builder
argument_list|()
return|;
block|}
assert|assert
name|indexOptions
argument_list|()
operator|!=
name|IndexOptions
operator|.
name|NONE
assert|;
if|if
condition|(
name|fielddata
condition|)
block|{
return|return
operator|new
name|PagedBytesIndexFieldData
operator|.
name|Builder
argument_list|(
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MIN_FREQUENCY
argument_list|,
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MAX_FREQUENCY
argument_list|,
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MIN_SEGMENT_SIZE
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|fielddataBuilder
argument_list|()
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
if|if
condition|(
name|indexOptions
argument_list|()
operator|==
name|IndexOptions
operator|.
name|NONE
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
return|return
operator|new
name|TypesQuery
argument_list|(
name|indexedValueForSearch
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkCompatibility
specifier|public
name|void
name|checkCompatibility
parameter_list|(
name|MappedFieldType
name|other
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|conflicts
parameter_list|,
name|boolean
name|strict
parameter_list|)
block|{
name|super
operator|.
name|checkCompatibility
argument_list|(
name|other
argument_list|,
name|conflicts
argument_list|,
name|strict
argument_list|)
expr_stmt|;
name|TypeFieldType
name|otherType
init|=
operator|(
name|TypeFieldType
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|strict
condition|)
block|{
if|if
condition|(
name|fielddata
argument_list|()
operator|!=
name|otherType
operator|.
name|fielddata
argument_list|()
condition|)
block|{
name|conflicts
operator|.
name|add
argument_list|(
literal|"mapper ["
operator|+
name|name
argument_list|()
operator|+
literal|"] is used by multiple types. Set update_all_types to true to update [fielddata] "
operator|+
literal|"across all types."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Specialization for a disjunction over many _type      */
DECL|class|TypesQuery
specifier|public
specifier|static
class|class
name|TypesQuery
extends|extends
name|Query
block|{
comment|// Same threshold as TermsQuery
DECL|field|BOOLEAN_REWRITE_TERM_COUNT_THRESHOLD
specifier|private
specifier|static
specifier|final
name|int
name|BOOLEAN_REWRITE_TERM_COUNT_THRESHOLD
init|=
literal|16
decl_stmt|;
DECL|field|types
specifier|private
specifier|final
name|BytesRef
index|[]
name|types
decl_stmt|;
DECL|method|TypesQuery
specifier|public
name|TypesQuery
parameter_list|(
name|BytesRef
modifier|...
name|types
parameter_list|)
block|{
if|if
condition|(
name|types
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"types cannot be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|types
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"types must contains at least one value."
argument_list|)
throw|;
block|}
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|threshold
init|=
name|Math
operator|.
name|min
argument_list|(
name|BOOLEAN_REWRITE_TERM_COUNT_THRESHOLD
argument_list|,
name|BooleanQuery
operator|.
name|getMaxClauseCount
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|types
operator|.
name|length
operator|<=
name|threshold
condition|)
block|{
name|Set
argument_list|<
name|BytesRef
argument_list|>
name|uniqueTypes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|BooleanQuery
operator|.
name|Builder
name|bq
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|int
name|totalDocFreq
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BytesRef
name|type
range|:
name|types
control|)
block|{
if|if
condition|(
name|uniqueTypes
operator|.
name|add
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|Term
name|term
init|=
operator|new
name|Term
argument_list|(
name|CONTENT_TYPE
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|TermContext
name|context
init|=
name|TermContext
operator|.
name|build
argument_list|(
name|reader
operator|.
name|getContext
argument_list|()
argument_list|,
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|docFreq
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// this _type is not present in the reader
continue|continue;
block|}
name|totalDocFreq
operator|+=
name|context
operator|.
name|docFreq
argument_list|()
expr_stmt|;
comment|// strict equality should be enough ?
if|if
condition|(
name|totalDocFreq
operator|>=
name|reader
operator|.
name|maxDoc
argument_list|()
condition|)
block|{
assert|assert
name|totalDocFreq
operator|==
name|reader
operator|.
name|maxDoc
argument_list|()
assert|;
comment|// Matches all docs since _type is a single value field
comment|// Using a match_all query will help Lucene perform some optimizations
comment|// For instance, match_all queries as filter clauses are automatically removed
return|return
operator|new
name|MatchAllDocsQuery
argument_list|()
return|;
block|}
name|bq
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
name|term
argument_list|,
name|context
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ConstantScoreQuery
argument_list|(
name|bq
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|TermsQuery
argument_list|(
name|CONTENT_TYPE
argument_list|,
name|types
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|sameClassAs
argument_list|(
name|obj
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TypesQuery
name|that
init|=
operator|(
name|TypesQuery
operator|)
name|obj
decl_stmt|;
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|types
argument_list|,
name|that
operator|.
name|types
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
literal|31
operator|*
name|classHash
argument_list|()
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|types
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|BytesRef
name|type
range|:
name|types
control|)
block|{
if|if
condition|(
name|builder
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
operator|new
name|Term
argument_list|(
name|CONTENT_TYPE
argument_list|,
name|type
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
DECL|method|TypeFieldMapper
specifier|private
name|TypeFieldMapper
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
name|defaultFieldType
argument_list|(
name|indexSettings
argument_list|)
else|:
name|existing
operator|.
name|clone
argument_list|()
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|TypeFieldMapper
specifier|private
name|TypeFieldMapper
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
name|defaultFieldType
argument_list|(
name|indexSettings
argument_list|)
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|defaultFieldType
specifier|private
specifier|static
name|MappedFieldType
name|defaultFieldType
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|MappedFieldType
name|defaultFieldType
init|=
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|clone
argument_list|()
decl_stmt|;
name|Version
name|indexCreated
init|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexCreated
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_1_0
argument_list|)
condition|)
block|{
comment|// enables fielddata loading, doc values was disabled on _type between 2.0 and 2.1.
operator|(
operator|(
name|TypeFieldType
operator|)
name|defaultFieldType
operator|)
operator|.
name|setFielddata
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|defaultFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|defaultFieldType
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
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
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
block|{     }
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we parse in pre parse
return|return
literal|null
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
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
operator|==
name|IndexOptions
operator|.
name|NONE
operator|&&
operator|!
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
return|return;
block|}
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
name|name
argument_list|()
argument_list|,
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|fieldType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldType
argument_list|()
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
name|SortedSetDocValuesField
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|// do nothing here, no merging, but also no exception
block|}
block|}
end_class

end_unit

