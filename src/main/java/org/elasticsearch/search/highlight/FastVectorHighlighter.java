begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
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
name|Maps
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
name|highlight
operator|.
name|Encoder
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
name|vectorhighlight
operator|.
name|*
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
name|vectorhighlight
operator|.
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
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
name|inject
operator|.
name|Inject
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
name|text
operator|.
name|StringText
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
name|FieldMapper
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
name|fetch
operator|.
name|FetchPhaseExecutionException
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
name|fetch
operator|.
name|FetchSubPhase
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
name|highlight
operator|.
name|vectorhighlight
operator|.
name|SimpleFragmentsBuilder
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
name|highlight
operator|.
name|vectorhighlight
operator|.
name|SourceScoreOrderFragmentsBuilder
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
name|highlight
operator|.
name|vectorhighlight
operator|.
name|SourceSimpleFragmentsBuilder
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
name|Collections
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
DECL|class|FastVectorHighlighter
specifier|public
class|class
name|FastVectorHighlighter
implements|implements
name|Highlighter
block|{
DECL|field|DEFAULT_BOUNDARY_SCANNER
specifier|private
specifier|static
specifier|final
name|SimpleBoundaryScanner
name|DEFAULT_BOUNDARY_SCANNER
init|=
operator|new
name|SimpleBoundaryScanner
argument_list|()
decl_stmt|;
DECL|field|CACHE_KEY
specifier|private
specifier|static
specifier|final
name|String
name|CACHE_KEY
init|=
literal|"highlight-fsv"
decl_stmt|;
DECL|field|termVectorMultiValue
specifier|private
specifier|final
name|Boolean
name|termVectorMultiValue
decl_stmt|;
annotation|@
name|Inject
DECL|method|FastVectorHighlighter
specifier|public
name|FastVectorHighlighter
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|termVectorMultiValue
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"search.highlight.term_vector_multi_value"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
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
literal|"fvh"
block|,
literal|"fast-vector-highlighter"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|highlight
specifier|public
name|HighlightField
name|highlight
parameter_list|(
name|HighlighterContext
name|highlighterContext
parameter_list|)
block|{
name|SearchContextHighlight
operator|.
name|Field
name|field
init|=
name|highlighterContext
operator|.
name|field
decl_stmt|;
name|SearchContext
name|context
init|=
name|highlighterContext
operator|.
name|context
decl_stmt|;
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
init|=
name|highlighterContext
operator|.
name|hitContext
decl_stmt|;
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|highlighterContext
operator|.
name|mapper
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectors
argument_list|()
operator|&&
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectorOffsets
argument_list|()
operator|&&
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectorPositions
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"the field ["
operator|+
name|field
operator|.
name|field
argument_list|()
operator|+
literal|"] should be indexed with term vector with position offsets to be used with fast vector highlighter"
argument_list|)
throw|;
block|}
name|Encoder
name|encoder
init|=
name|field
operator|.
name|encoder
argument_list|()
operator|.
name|equals
argument_list|(
literal|"html"
argument_list|)
condition|?
name|HighlightUtils
operator|.
name|Encoders
operator|.
name|HTML
else|:
name|HighlightUtils
operator|.
name|Encoders
operator|.
name|DEFAULT
decl_stmt|;
if|if
condition|(
operator|!
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|containsKey
argument_list|(
name|CACHE_KEY
argument_list|)
condition|)
block|{
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|put
argument_list|(
name|CACHE_KEY
argument_list|,
operator|new
name|HighlighterEntry
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HighlighterEntry
name|cache
init|=
operator|(
name|HighlighterEntry
operator|)
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|get
argument_list|(
name|CACHE_KEY
argument_list|)
decl_stmt|;
try|try
block|{
name|FieldQuery
name|fieldQuery
decl_stmt|;
if|if
condition|(
name|field
operator|.
name|requireFieldMatch
argument_list|()
condition|)
block|{
if|if
condition|(
name|cache
operator|.
name|fieldMatchFieldQuery
operator|==
literal|null
condition|)
block|{
comment|// we use top level reader to rewrite the query against all readers, with use caching it across hits (and across readers...)
name|cache
operator|.
name|fieldMatchFieldQuery
operator|=
operator|new
name|CustomFieldQuery
argument_list|(
name|highlighterContext
operator|.
name|query
operator|.
name|originalQuery
argument_list|()
argument_list|,
name|hitContext
operator|.
name|topLevelReader
argument_list|()
argument_list|,
literal|true
argument_list|,
name|field
operator|.
name|requireFieldMatch
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fieldQuery
operator|=
name|cache
operator|.
name|fieldMatchFieldQuery
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|cache
operator|.
name|noFieldMatchFieldQuery
operator|==
literal|null
condition|)
block|{
comment|// we use top level reader to rewrite the query against all readers, with use caching it across hits (and across readers...)
name|cache
operator|.
name|noFieldMatchFieldQuery
operator|=
operator|new
name|CustomFieldQuery
argument_list|(
name|highlighterContext
operator|.
name|query
operator|.
name|originalQuery
argument_list|()
argument_list|,
name|hitContext
operator|.
name|topLevelReader
argument_list|()
argument_list|,
literal|true
argument_list|,
name|field
operator|.
name|requireFieldMatch
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fieldQuery
operator|=
name|cache
operator|.
name|noFieldMatchFieldQuery
expr_stmt|;
block|}
name|MapperHighlightEntry
name|entry
init|=
name|cache
operator|.
name|mappers
operator|.
name|get
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|FragListBuilder
name|fragListBuilder
decl_stmt|;
name|BaseFragmentsBuilder
name|fragmentsBuilder
decl_stmt|;
name|BoundaryScanner
name|boundaryScanner
init|=
name|DEFAULT_BOUNDARY_SCANNER
decl_stmt|;
if|if
condition|(
name|field
operator|.
name|boundaryMaxScan
argument_list|()
operator|!=
name|SimpleBoundaryScanner
operator|.
name|DEFAULT_MAX_SCAN
operator|||
name|field
operator|.
name|boundaryChars
argument_list|()
operator|!=
name|SimpleBoundaryScanner
operator|.
name|DEFAULT_BOUNDARY_CHARS
condition|)
block|{
name|boundaryScanner
operator|=
operator|new
name|SimpleBoundaryScanner
argument_list|(
name|field
operator|.
name|boundaryMaxScan
argument_list|()
argument_list|,
name|field
operator|.
name|boundaryChars
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|numberOfFragments
argument_list|()
operator|==
literal|0
condition|)
block|{
name|fragListBuilder
operator|=
operator|new
name|SingleFragListBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|field
operator|.
name|forceSource
argument_list|()
operator|&&
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|fragmentsBuilder
operator|=
operator|new
name|SimpleFragmentsBuilder
argument_list|(
name|mapper
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fragmentsBuilder
operator|=
operator|new
name|SourceSimpleFragmentsBuilder
argument_list|(
name|mapper
argument_list|,
name|context
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fragListBuilder
operator|=
name|field
operator|.
name|fragmentOffset
argument_list|()
operator|==
operator|-
literal|1
condition|?
operator|new
name|SimpleFragListBuilder
argument_list|()
else|:
operator|new
name|SimpleFragListBuilder
argument_list|(
name|field
operator|.
name|fragmentOffset
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|scoreOrdered
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|field
operator|.
name|forceSource
argument_list|()
operator|&&
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|fragmentsBuilder
operator|=
operator|new
name|ScoreOrderFragmentsBuilder
argument_list|(
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fragmentsBuilder
operator|=
operator|new
name|SourceScoreOrderFragmentsBuilder
argument_list|(
name|mapper
argument_list|,
name|context
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|field
operator|.
name|forceSource
argument_list|()
operator|&&
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|fragmentsBuilder
operator|=
operator|new
name|SimpleFragmentsBuilder
argument_list|(
name|mapper
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fragmentsBuilder
operator|=
operator|new
name|SourceSimpleFragmentsBuilder
argument_list|(
name|mapper
argument_list|,
name|context
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|fragmentsBuilder
operator|.
name|setDiscreteMultiValueHighlighting
argument_list|(
name|termVectorMultiValue
argument_list|)
expr_stmt|;
name|entry
operator|=
operator|new
name|MapperHighlightEntry
argument_list|()
expr_stmt|;
name|entry
operator|.
name|fragListBuilder
operator|=
name|fragListBuilder
expr_stmt|;
name|entry
operator|.
name|fragmentsBuilder
operator|=
name|fragmentsBuilder
expr_stmt|;
if|if
condition|(
name|cache
operator|.
name|fvh
operator|==
literal|null
condition|)
block|{
comment|// parameters to FVH are not requires since:
comment|// first two booleans are not relevant since they are set on the CustomFieldQuery (phrase and fieldMatch)
comment|// fragment builders are used explicitly
name|cache
operator|.
name|fvh
operator|=
operator|new
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|vectorhighlight
operator|.
name|FastVectorHighlighter
argument_list|()
expr_stmt|;
block|}
name|CustomFieldQuery
operator|.
name|highlightFilters
operator|.
name|set
argument_list|(
name|field
operator|.
name|highlightFilter
argument_list|()
argument_list|)
expr_stmt|;
name|cache
operator|.
name|mappers
operator|.
name|put
argument_list|(
name|mapper
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
name|cache
operator|.
name|fvh
operator|.
name|setPhraseLimit
argument_list|(
name|field
operator|.
name|phraseLimit
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|fragments
decl_stmt|;
comment|// a HACK to make highlighter do highlighting, even though its using the single frag list builder
name|int
name|numberOfFragments
init|=
name|field
operator|.
name|numberOfFragments
argument_list|()
operator|==
literal|0
condition|?
name|Integer
operator|.
name|MAX_VALUE
else|:
name|field
operator|.
name|numberOfFragments
argument_list|()
decl_stmt|;
name|int
name|fragmentCharSize
init|=
name|field
operator|.
name|numberOfFragments
argument_list|()
operator|==
literal|0
condition|?
name|Integer
operator|.
name|MAX_VALUE
else|:
name|field
operator|.
name|fragmentCharSize
argument_list|()
decl_stmt|;
comment|// we highlight against the low level reader and docId, because if we load source, we want to reuse it if possible
comment|// Only send matched fields if they were requested to save time.
if|if
condition|(
name|field
operator|.
name|matchedFields
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|field
operator|.
name|matchedFields
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fragments
operator|=
name|cache
operator|.
name|fvh
operator|.
name|getBestFragments
argument_list|(
name|fieldQuery
argument_list|,
name|hitContext
operator|.
name|reader
argument_list|()
argument_list|,
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|,
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|field
operator|.
name|matchedFields
argument_list|()
argument_list|,
name|fragmentCharSize
argument_list|,
name|numberOfFragments
argument_list|,
name|entry
operator|.
name|fragListBuilder
argument_list|,
name|entry
operator|.
name|fragmentsBuilder
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|encoder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fragments
operator|=
name|cache
operator|.
name|fvh
operator|.
name|getBestFragments
argument_list|(
name|fieldQuery
argument_list|,
name|hitContext
operator|.
name|reader
argument_list|()
argument_list|,
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|,
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|fragmentCharSize
argument_list|,
name|numberOfFragments
argument_list|,
name|entry
operator|.
name|fragListBuilder
argument_list|,
name|entry
operator|.
name|fragmentsBuilder
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|encoder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fragments
operator|!=
literal|null
operator|&&
name|fragments
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|HighlightField
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|,
name|StringText
operator|.
name|convertFromStringArray
argument_list|(
name|fragments
argument_list|)
argument_list|)
return|;
block|}
name|int
name|noMatchSize
init|=
name|highlighterContext
operator|.
name|field
operator|.
name|noMatchSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|noMatchSize
operator|>
literal|0
condition|)
block|{
comment|// Essentially we just request that a fragment is built from 0 to noMatchSize using the normal fragmentsBuilder
name|FieldFragList
name|fieldFragList
init|=
operator|new
name|SimpleFieldFragList
argument_list|(
operator|-
literal|1
comment|/*ignored*/
argument_list|)
decl_stmt|;
name|fieldFragList
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|noMatchSize
argument_list|,
name|Collections
operator|.
expr|<
name|WeightedPhraseInfo
operator|>
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
name|fragments
operator|=
name|entry
operator|.
name|fragmentsBuilder
operator|.
name|createFragments
argument_list|(
name|hitContext
operator|.
name|reader
argument_list|()
argument_list|,
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|,
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
name|fieldFragList
argument_list|,
literal|1
argument_list|,
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
argument_list|,
name|encoder
argument_list|)
expr_stmt|;
if|if
condition|(
name|fragments
operator|!=
literal|null
operator|&&
name|fragments
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|HighlightField
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|,
name|StringText
operator|.
name|convertFromStringArray
argument_list|(
name|fragments
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|FetchPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Failed to highlight field ["
operator|+
name|highlighterContext
operator|.
name|fieldName
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|class|MapperHighlightEntry
specifier|private
class|class
name|MapperHighlightEntry
block|{
DECL|field|fragListBuilder
specifier|public
name|FragListBuilder
name|fragListBuilder
decl_stmt|;
DECL|field|fragmentsBuilder
specifier|public
name|FragmentsBuilder
name|fragmentsBuilder
decl_stmt|;
DECL|field|highlighter
specifier|public
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|highlight
operator|.
name|Highlighter
name|highlighter
decl_stmt|;
block|}
DECL|class|HighlighterEntry
specifier|private
class|class
name|HighlighterEntry
block|{
DECL|field|fvh
specifier|public
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|vectorhighlight
operator|.
name|FastVectorHighlighter
name|fvh
decl_stmt|;
DECL|field|noFieldMatchFieldQuery
specifier|public
name|FieldQuery
name|noFieldMatchFieldQuery
decl_stmt|;
DECL|field|fieldMatchFieldQuery
specifier|public
name|FieldQuery
name|fieldMatchFieldQuery
decl_stmt|;
DECL|field|mappers
specifier|public
name|Map
argument_list|<
name|FieldMapper
argument_list|,
name|MapperHighlightEntry
argument_list|>
name|mappers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
block|}
block|}
end_class

end_unit

