begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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
name|LeafReaderContext
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
name|BinaryDocValues
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
name|Scorer
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
name|SortField
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
name|join
operator|.
name|BitDocIdSetFilter
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRefBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|fielddata
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
name|fielddata
operator|.
name|IndexFieldData
operator|.
name|XFieldComparatorSource
operator|.
name|Nested
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
name|fieldcomparator
operator|.
name|BytesRefFieldComparatorSource
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
name|fieldcomparator
operator|.
name|DoubleValuesComparatorSource
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
name|ObjectMappers
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
name|object
operator|.
name|ObjectMapper
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
name|ParsedFilter
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
name|search
operator|.
name|nested
operator|.
name|NonNestedDocsFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|MultiValueMode
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
name|SearchParseException
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
name|internal
operator|.
name|SearchContext
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
comment|/**  *  */
end_comment

begin_class
DECL|class|ScriptSortParser
specifier|public
class|class
name|ScriptSortParser
implements|implements
name|SortParser
block|{
DECL|field|STRING_SORT_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|STRING_SORT_TYPE
init|=
literal|"string"
decl_stmt|;
DECL|field|NUMBER_SORT_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|NUMBER_SORT_TYPE
init|=
literal|"number"
decl_stmt|;
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"_script"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|SortField
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|script
init|=
literal|null
decl_stmt|;
name|String
name|scriptLang
init|=
literal|null
decl_stmt|;
name|String
name|type
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
name|boolean
name|reverse
init|=
literal|false
decl_stmt|;
name|MultiValueMode
name|sortMode
init|=
literal|null
decl_stmt|;
name|String
name|nestedPath
init|=
literal|null
decl_stmt|;
name|Filter
name|nestedFilter
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|ScriptService
operator|.
name|ScriptType
name|scriptType
init|=
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
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
name|currentName
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
if|if
condition|(
literal|"params"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|params
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"nested_filter"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
operator|||
literal|"nestedFilter"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|ParsedFilter
name|parsedFilter
init|=
name|context
operator|.
name|queryParserService
argument_list|()
operator|.
name|parseInnerFilter
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|nestedFilter
operator|=
name|parsedFilter
operator|==
literal|null
condition|?
literal|null
else|:
name|parsedFilter
operator|.
name|filter
argument_list|()
expr_stmt|;
block|}
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
literal|"reverse"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|reverse
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"order"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|reverse
operator|=
literal|"desc"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ScriptService
operator|.
name|SCRIPT_INLINE
operator|.
name|match
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|script
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
name|scriptType
operator|=
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ScriptService
operator|.
name|SCRIPT_ID
operator|.
name|match
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|script
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
name|scriptType
operator|=
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ScriptService
operator|.
name|SCRIPT_FILE
operator|.
name|match
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|script
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
name|scriptType
operator|=
name|ScriptService
operator|.
name|ScriptType
operator|.
name|FILE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ScriptService
operator|.
name|SCRIPT_LANG
operator|.
name|match
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|scriptLang
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
literal|"type"
operator|.
name|equals
argument_list|(
name|currentName
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
literal|"mode"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|sortMode
operator|=
name|MultiValueMode
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"nested_path"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
operator|||
literal|"nestedPath"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|nestedPath
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"_script sorting requires setting the script to sort by"
argument_list|)
throw|;
block|}
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"_script sorting requires setting the type of the script"
argument_list|)
throw|;
block|}
specifier|final
name|SearchScript
name|searchScript
init|=
name|context
operator|.
name|scriptService
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|scriptType
argument_list|,
name|params
argument_list|)
decl_stmt|;
if|if
condition|(
name|STRING_SORT_TYPE
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|&&
operator|(
name|sortMode
operator|==
name|MultiValueMode
operator|.
name|SUM
operator|||
name|sortMode
operator|==
name|MultiValueMode
operator|.
name|AVG
operator|)
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"type [string] doesn't support mode ["
operator|+
name|sortMode
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|sortMode
operator|==
literal|null
condition|)
block|{
name|sortMode
operator|=
name|reverse
condition|?
name|MultiValueMode
operator|.
name|MAX
else|:
name|MultiValueMode
operator|.
name|MIN
expr_stmt|;
block|}
comment|// If nested_path is specified, then wrap the `fieldComparatorSource` in a `NestedFieldComparatorSource`
name|ObjectMapper
name|objectMapper
decl_stmt|;
specifier|final
name|Nested
name|nested
decl_stmt|;
if|if
condition|(
name|nestedPath
operator|!=
literal|null
condition|)
block|{
name|ObjectMappers
name|objectMappers
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|objectMapper
argument_list|(
name|nestedPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|objectMappers
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find nested object mapping for explicit nested path ["
operator|+
name|nestedPath
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|objectMapper
operator|=
name|objectMappers
operator|.
name|mapper
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|objectMapper
operator|.
name|nested
argument_list|()
operator|.
name|isNested
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"mapping for explicit nested path is not mapped as nested: ["
operator|+
name|nestedPath
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|BitDocIdSetFilter
name|rootDocumentsFilter
init|=
name|context
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|NonNestedDocsFilter
operator|.
name|INSTANCE
argument_list|)
decl_stmt|;
name|BitDocIdSetFilter
name|innerDocumentsFilter
decl_stmt|;
if|if
condition|(
name|nestedFilter
operator|!=
literal|null
condition|)
block|{
name|innerDocumentsFilter
operator|=
name|context
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|nestedFilter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|innerDocumentsFilter
operator|=
name|context
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|nested
operator|=
operator|new
name|Nested
argument_list|(
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nested
operator|=
literal|null
expr_stmt|;
block|}
specifier|final
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|fieldComparatorSource
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING_SORT_TYPE
case|:
name|fieldComparatorSource
operator|=
operator|new
name|BytesRefFieldComparatorSource
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|sortMode
argument_list|,
name|nested
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|SortedBinaryDocValues
name|getValues
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
name|searchScript
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
specifier|final
name|BinaryDocValues
name|values
init|=
operator|new
name|BinaryDocValues
argument_list|()
block|{
specifier|final
name|BytesRefBuilder
name|spare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|BytesRef
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
name|searchScript
operator|.
name|setNextDocId
argument_list|(
name|docID
argument_list|)
expr_stmt|;
name|spare
operator|.
name|copyChars
argument_list|(
name|searchScript
operator|.
name|run
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|spare
operator|.
name|get
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|FieldData
operator|.
name|singleton
argument_list|(
name|values
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|searchScript
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
break|break;
case|case
name|NUMBER_SORT_TYPE
case|:
comment|// TODO: should we rather sort missing values last?
name|fieldComparatorSource
operator|=
operator|new
name|DoubleValuesComparatorSource
argument_list|(
literal|null
argument_list|,
name|Double
operator|.
name|MAX_VALUE
argument_list|,
name|sortMode
argument_list|,
name|nested
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|SortedNumericDoubleValues
name|getValues
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
name|searchScript
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
specifier|final
name|NumericDoubleValues
name|values
init|=
operator|new
name|NumericDoubleValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|double
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
name|searchScript
operator|.
name|setNextDocId
argument_list|(
name|docID
argument_list|)
expr_stmt|;
return|return
name|searchScript
operator|.
name|runAsDouble
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|FieldData
operator|.
name|singleton
argument_list|(
name|values
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|searchScript
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"custom script sort type ["
operator|+
name|type
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
return|return
operator|new
name|SortField
argument_list|(
literal|"_script"
argument_list|,
name|fieldComparatorSource
argument_list|,
name|reverse
argument_list|)
return|;
block|}
block|}
end_class

end_unit

