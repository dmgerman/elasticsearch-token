begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|LeafReader
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
name|ReaderUtil
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
name|Terms
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
name|BulkScorer
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
name|CollectionTerminatedException
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
name|IndexSearcher
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
name|Weight
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
name|suggest
operator|.
name|Lookup
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
name|suggest
operator|.
name|document
operator|.
name|CompletionQuery
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
name|suggest
operator|.
name|document
operator|.
name|TopSuggestDocs
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
name|suggest
operator|.
name|document
operator|.
name|TopSuggestDocsCollector
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
name|CharsRefBuilder
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
name|CollectionUtil
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
name|PriorityQueue
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
name|BytesArray
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
name|text
operator|.
name|Text
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
name|AtomicFieldData
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
name|ScriptDocValues
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
name|MapperService
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
name|CompletionFieldMapper
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
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
name|suggest
operator|.
name|Suggester
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
name|suggest
operator|.
name|SuggestionBuilder
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
name|suggest
operator|.
name|completion2x
operator|.
name|Completion090PostingsFormat
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
name|Comparator
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
name|LinkedHashMap
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
name|Set
import|;
end_import

begin_class
DECL|class|CompletionSuggester
specifier|public
class|class
name|CompletionSuggester
extends|extends
name|Suggester
argument_list|<
name|CompletionSuggestionContext
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|CompletionSuggester
name|INSTANCE
init|=
operator|new
name|CompletionSuggester
argument_list|()
decl_stmt|;
DECL|method|CompletionSuggester
specifier|private
name|CompletionSuggester
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|innerExecute
specifier|protected
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
argument_list|>
name|innerExecute
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|CompletionSuggestionContext
name|suggestionContext
parameter_list|,
specifier|final
name|IndexSearcher
name|searcher
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|suggestionContext
operator|.
name|getFieldType
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
init|=
name|suggestionContext
operator|.
name|getFieldType
argument_list|()
decl_stmt|;
name|CompletionSuggestion
name|completionSuggestion
init|=
operator|new
name|CompletionSuggestion
argument_list|(
name|name
argument_list|,
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|suggestionContext
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|CompletionSuggestion
operator|.
name|Entry
name|completionSuggestEntry
init|=
operator|new
name|CompletionSuggestion
operator|.
name|Entry
argument_list|(
operator|new
name|Text
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|,
name|spare
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|completionSuggestion
operator|.
name|addTerm
argument_list|(
name|completionSuggestEntry
argument_list|)
expr_stmt|;
name|TopSuggestDocsCollector
name|collector
init|=
operator|new
name|TopDocumentsCollector
argument_list|(
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|suggest
argument_list|(
name|searcher
argument_list|,
name|suggestionContext
operator|.
name|toQuery
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|int
name|numResult
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
decl_stmt|;
for|for
control|(
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
name|suggestScoreDoc
range|:
name|collector
operator|.
name|get
argument_list|()
operator|.
name|scoreLookupDocs
argument_list|()
control|)
block|{
name|TopDocumentsCollector
operator|.
name|SuggestDoc
name|suggestDoc
init|=
operator|(
name|TopDocumentsCollector
operator|.
name|SuggestDoc
operator|)
name|suggestScoreDoc
decl_stmt|;
comment|// collect contexts
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldType
operator|.
name|hasContextMappings
argument_list|()
operator|&&
name|suggestDoc
operator|.
name|getContexts
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|contexts
operator|=
name|fieldType
operator|.
name|getContextMappings
argument_list|()
operator|.
name|getNamedContexts
argument_list|(
name|suggestDoc
operator|.
name|getContexts
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// collect payloads
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|payload
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|payloadFields
init|=
name|suggestionContext
operator|.
name|getPayloadFields
argument_list|()
decl_stmt|;
if|if
condition|(
name|payloadFields
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
specifier|final
name|int
name|readerIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|suggestDoc
operator|.
name|doc
argument_list|,
name|leaves
argument_list|)
decl_stmt|;
specifier|final
name|LeafReaderContext
name|subReaderContext
init|=
name|leaves
operator|.
name|get
argument_list|(
name|readerIndex
argument_list|)
decl_stmt|;
specifier|final
name|int
name|subDocId
init|=
name|suggestDoc
operator|.
name|doc
operator|-
name|subReaderContext
operator|.
name|docBase
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|payloadFields
control|)
block|{
name|MapperService
name|mapperService
init|=
name|suggestionContext
operator|.
name|getShardContext
argument_list|()
operator|.
name|getMapperService
argument_list|()
decl_stmt|;
name|MappedFieldType
name|payloadFieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|payloadFieldType
operator|!=
literal|null
condition|)
block|{
name|QueryShardContext
name|shardContext
init|=
name|suggestionContext
operator|.
name|getShardContext
argument_list|()
decl_stmt|;
specifier|final
name|AtomicFieldData
name|data
init|=
name|shardContext
operator|.
name|getForField
argument_list|(
name|payloadFieldType
argument_list|)
operator|.
name|load
argument_list|(
name|subReaderContext
argument_list|)
decl_stmt|;
specifier|final
name|ScriptDocValues
name|scriptValues
init|=
name|data
operator|.
name|getScriptValues
argument_list|()
decl_stmt|;
name|scriptValues
operator|.
name|setNextDocId
argument_list|(
name|subDocId
argument_list|)
expr_stmt|;
name|payload
operator|.
name|put
argument_list|(
name|field
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|scriptValues
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"payload field ["
operator|+
name|field
operator|+
literal|"] does not exist"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|numResult
operator|++
operator|<
name|suggestionContext
operator|.
name|getSize
argument_list|()
condition|)
block|{
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|option
init|=
operator|new
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
name|suggestDoc
operator|.
name|doc
argument_list|,
operator|new
name|Text
argument_list|(
name|suggestDoc
operator|.
name|key
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|suggestDoc
operator|.
name|score
argument_list|,
name|contexts
argument_list|,
name|payload
argument_list|)
decl_stmt|;
name|completionSuggestEntry
operator|.
name|addOption
argument_list|(
name|option
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
return|return
name|completionSuggestion
return|;
block|}
elseif|else
if|if
condition|(
name|suggestionContext
operator|.
name|getFieldType2x
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|IndexReader
name|indexReader
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
name|completionSuggestion
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
argument_list|(
name|name
argument_list|,
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|suggestionContext
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
name|completionSuggestEntry
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
argument_list|(
operator|new
name|Text
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|,
name|spare
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|completionSuggestion
operator|.
name|addTerm
argument_list|(
name|completionSuggestEntry
argument_list|)
expr_stmt|;
name|String
name|fieldName
init|=
name|suggestionContext
operator|.
name|getField
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|results
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|indexReader
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
operator|*
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|atomicReaderContext
range|:
name|indexReader
operator|.
name|leaves
argument_list|()
control|)
block|{
name|LeafReader
name|atomicReader
init|=
name|atomicReaderContext
operator|.
name|reader
argument_list|()
decl_stmt|;
name|Terms
name|terms
init|=
name|atomicReader
operator|.
name|fields
argument_list|()
operator|.
name|terms
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|instanceof
name|Completion090PostingsFormat
operator|.
name|CompletionTerms
condition|)
block|{
specifier|final
name|Completion090PostingsFormat
operator|.
name|CompletionTerms
name|lookupTerms
init|=
operator|(
name|Completion090PostingsFormat
operator|.
name|CompletionTerms
operator|)
name|terms
decl_stmt|;
specifier|final
name|Lookup
name|lookup
init|=
name|lookupTerms
operator|.
name|getLookup
argument_list|(
name|suggestionContext
operator|.
name|getFieldType2x
argument_list|()
argument_list|,
name|suggestionContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|lookup
operator|==
literal|null
condition|)
block|{
comment|// we don't have a lookup for this segment.. this might be possible if a merge dropped all
comment|// docs from the segment that had a value in this segment.
continue|continue;
block|}
name|List
argument_list|<
name|Lookup
operator|.
name|LookupResult
argument_list|>
name|lookupResults
init|=
name|lookup
operator|.
name|lookup
argument_list|(
name|spare
operator|.
name|get
argument_list|()
argument_list|,
literal|false
argument_list|,
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Lookup
operator|.
name|LookupResult
name|res
range|:
name|lookupResults
control|)
block|{
specifier|final
name|String
name|key
init|=
name|res
operator|.
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|final
name|float
name|score
init|=
name|res
operator|.
name|value
decl_stmt|;
specifier|final
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|value
init|=
name|results
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
specifier|final
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|option
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
operator|new
name|Text
argument_list|(
name|key
argument_list|)
argument_list|,
name|score
argument_list|,
name|res
operator|.
name|payload
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BytesArray
argument_list|(
name|res
operator|.
name|payload
argument_list|)
argument_list|)
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|option
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|getScore
argument_list|()
operator|<
name|score
condition|)
block|{
name|value
operator|.
name|setScore
argument_list|(
name|score
argument_list|)
expr_stmt|;
name|value
operator|.
name|setPayload
argument_list|(
name|res
operator|.
name|payload
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BytesArray
argument_list|(
name|res
operator|.
name|payload
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|final
name|List
argument_list|<
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|options
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|results
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|options
argument_list|,
name|scoreComparator
argument_list|)
expr_stmt|;
name|int
name|optionCount
init|=
name|Math
operator|.
name|min
argument_list|(
name|suggestionContext
operator|.
name|getSize
argument_list|()
argument_list|,
name|options
operator|.
name|size
argument_list|()
argument_list|)
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
name|optionCount
condition|;
name|i
operator|++
control|)
block|{
name|completionSuggestEntry
operator|.
name|addOption
argument_list|(
name|options
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|completionSuggestion
return|;
block|}
return|return
literal|null
return|;
block|}
DECL|field|scoreComparator
specifier|private
specifier|static
specifier|final
name|ScoreComparator
name|scoreComparator
init|=
operator|new
name|ScoreComparator
argument_list|()
decl_stmt|;
DECL|class|ScoreComparator
specifier|public
specifier|static
class|class
name|ScoreComparator
implements|implements
name|Comparator
argument_list|<
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|o1
parameter_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion2x
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|o2
parameter_list|)
block|{
return|return
name|Float
operator|.
name|compare
argument_list|(
name|o2
operator|.
name|getScore
argument_list|()
argument_list|,
name|o1
operator|.
name|getScore
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|suggest
specifier|private
specifier|static
name|void
name|suggest
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|CompletionQuery
name|query
parameter_list|,
name|TopSuggestDocsCollector
name|collector
parameter_list|)
throws|throws
name|IOException
block|{
name|query
operator|=
operator|(
name|CompletionQuery
operator|)
name|query
operator|.
name|rewrite
argument_list|(
name|searcher
operator|.
name|getIndexReader
argument_list|()
argument_list|)
expr_stmt|;
name|Weight
name|weight
init|=
name|query
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|,
name|collector
operator|.
name|needsScores
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
control|)
block|{
name|BulkScorer
name|scorer
init|=
name|weight
operator|.
name|bulkScorer
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|scorer
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scorer
operator|.
name|score
argument_list|(
name|collector
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CollectionTerminatedException
name|e
parameter_list|)
block|{
comment|// collection was terminated prematurely
comment|// continue with the following leaf
block|}
block|}
block|}
block|}
comment|// TODO: this should be refactored and moved to lucene
comment|// see https://issues.apache.org/jira/browse/LUCENE-6880
DECL|class|TopDocumentsCollector
specifier|private
specifier|static
specifier|final
class|class
name|TopDocumentsCollector
extends|extends
name|TopSuggestDocsCollector
block|{
comment|/**          * Holds a list of suggest meta data for a doc          */
DECL|class|SuggestDoc
specifier|private
specifier|static
specifier|final
class|class
name|SuggestDoc
extends|extends
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
block|{
DECL|field|suggestScoreDocs
specifier|private
name|List
argument_list|<
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
argument_list|>
name|suggestScoreDocs
decl_stmt|;
DECL|method|SuggestDoc
specifier|public
name|SuggestDoc
parameter_list|(
name|int
name|doc
parameter_list|,
name|CharSequence
name|key
parameter_list|,
name|CharSequence
name|context
parameter_list|,
name|float
name|score
parameter_list|)
block|{
name|super
argument_list|(
name|doc
argument_list|,
name|key
argument_list|,
name|context
argument_list|,
name|score
argument_list|)
expr_stmt|;
block|}
DECL|method|add
name|void
name|add
parameter_list|(
name|CharSequence
name|key
parameter_list|,
name|CharSequence
name|context
parameter_list|,
name|float
name|score
parameter_list|)
block|{
if|if
condition|(
name|suggestScoreDocs
operator|==
literal|null
condition|)
block|{
name|suggestScoreDocs
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|suggestScoreDocs
operator|.
name|add
argument_list|(
operator|new
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
argument_list|(
name|doc
argument_list|,
name|key
argument_list|,
name|context
argument_list|,
name|score
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getKeys
specifier|public
name|List
argument_list|<
name|CharSequence
argument_list|>
name|getKeys
parameter_list|()
block|{
if|if
condition|(
name|suggestScoreDocs
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|key
argument_list|)
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|CharSequence
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|suggestScoreDocs
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
for|for
control|(
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
name|scoreDoc
range|:
name|suggestScoreDocs
control|)
block|{
name|keys
operator|.
name|add
argument_list|(
name|scoreDoc
operator|.
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|keys
return|;
block|}
block|}
DECL|method|getContexts
specifier|public
name|List
argument_list|<
name|CharSequence
argument_list|>
name|getContexts
parameter_list|()
block|{
if|if
condition|(
name|suggestScoreDocs
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|context
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|CharSequence
argument_list|>
name|contexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|suggestScoreDocs
operator|.
name|size
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|contexts
operator|.
name|add
argument_list|(
name|context
argument_list|)
expr_stmt|;
for|for
control|(
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
name|scoreDoc
range|:
name|suggestScoreDocs
control|)
block|{
name|contexts
operator|.
name|add
argument_list|(
name|scoreDoc
operator|.
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|contexts
return|;
block|}
block|}
block|}
DECL|class|SuggestDocPriorityQueue
specifier|private
specifier|static
specifier|final
class|class
name|SuggestDocPriorityQueue
extends|extends
name|PriorityQueue
argument_list|<
name|SuggestDoc
argument_list|>
block|{
DECL|method|SuggestDocPriorityQueue
specifier|public
name|SuggestDocPriorityQueue
parameter_list|(
name|int
name|maxSize
parameter_list|)
block|{
name|super
argument_list|(
name|maxSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|lessThan
specifier|protected
name|boolean
name|lessThan
parameter_list|(
name|SuggestDoc
name|a
parameter_list|,
name|SuggestDoc
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|.
name|score
operator|==
name|b
operator|.
name|score
condition|)
block|{
name|int
name|cmp
init|=
name|Lookup
operator|.
name|CHARSEQUENCE_COMPARATOR
operator|.
name|compare
argument_list|(
name|a
operator|.
name|key
argument_list|,
name|b
operator|.
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
comment|// prefer smaller doc id, in case of a tie
return|return
name|a
operator|.
name|doc
operator|>
name|b
operator|.
name|doc
return|;
block|}
else|else
block|{
return|return
name|cmp
operator|>
literal|0
return|;
block|}
block|}
return|return
name|a
operator|.
name|score
operator|<
name|b
operator|.
name|score
return|;
block|}
DECL|method|getResults
specifier|public
name|SuggestDoc
index|[]
name|getResults
parameter_list|()
block|{
name|int
name|size
init|=
name|size
argument_list|()
decl_stmt|;
name|SuggestDoc
index|[]
name|res
init|=
operator|new
name|SuggestDoc
index|[
name|size
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|size
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|res
index|[
name|i
index|]
operator|=
name|pop
argument_list|()
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
block|}
DECL|field|num
specifier|private
specifier|final
name|int
name|num
decl_stmt|;
DECL|field|pq
specifier|private
specifier|final
name|SuggestDocPriorityQueue
name|pq
decl_stmt|;
DECL|field|scoreDocMap
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|SuggestDoc
argument_list|>
name|scoreDocMap
decl_stmt|;
DECL|method|TopDocumentsCollector
specifier|public
name|TopDocumentsCollector
parameter_list|(
name|int
name|num
parameter_list|)
block|{
name|super
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// TODO hack, we don't use the underlying pq, so we allocate a size of 1
name|this
operator|.
name|num
operator|=
name|num
expr_stmt|;
name|this
operator|.
name|scoreDocMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|num
argument_list|)
expr_stmt|;
name|this
operator|.
name|pq
operator|=
operator|new
name|SuggestDocPriorityQueue
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getCountToCollect
specifier|public
name|int
name|getCountToCollect
parameter_list|()
block|{
comment|// This is only needed because we initialize
comment|// the base class with 1 instead of the actual num
return|return
name|num
return|;
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doSetNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|updateResults
argument_list|()
expr_stmt|;
block|}
DECL|method|updateResults
specifier|private
name|void
name|updateResults
parameter_list|()
block|{
for|for
control|(
name|SuggestDoc
name|suggestDoc
range|:
name|scoreDocMap
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|pq
operator|.
name|insertWithOverflow
argument_list|(
name|suggestDoc
argument_list|)
operator|==
name|suggestDoc
condition|)
block|{
break|break;
block|}
block|}
name|scoreDocMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|docID
parameter_list|,
name|CharSequence
name|key
parameter_list|,
name|CharSequence
name|context
parameter_list|,
name|float
name|score
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scoreDocMap
operator|.
name|containsKey
argument_list|(
name|docID
argument_list|)
condition|)
block|{
name|SuggestDoc
name|suggestDoc
init|=
name|scoreDocMap
operator|.
name|get
argument_list|(
name|docID
argument_list|)
decl_stmt|;
name|suggestDoc
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|context
argument_list|,
name|score
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scoreDocMap
operator|.
name|size
argument_list|()
operator|<=
name|num
condition|)
block|{
name|scoreDocMap
operator|.
name|put
argument_list|(
name|docID
argument_list|,
operator|new
name|SuggestDoc
argument_list|(
name|docBase
operator|+
name|docID
argument_list|,
name|key
argument_list|,
name|context
argument_list|,
name|score
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|TopSuggestDocs
name|get
parameter_list|()
throws|throws
name|IOException
block|{
name|updateResults
argument_list|()
expr_stmt|;
comment|// to empty the last set of collected suggest docs
name|TopSuggestDocs
operator|.
name|SuggestScoreDoc
index|[]
name|suggestScoreDocs
init|=
name|pq
operator|.
name|getResults
argument_list|()
decl_stmt|;
if|if
condition|(
name|suggestScoreDocs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|TopSuggestDocs
argument_list|(
name|suggestScoreDocs
operator|.
name|length
argument_list|,
name|suggestScoreDocs
argument_list|,
name|suggestScoreDocs
index|[
literal|0
index|]
operator|.
name|score
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|TopSuggestDocs
operator|.
name|EMPTY
return|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|innerFromXContent
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|innerFromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|CompletionSuggestionBuilder
operator|.
name|innerFromXContent
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|CompletionSuggestionBuilder
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
end_class

end_unit

