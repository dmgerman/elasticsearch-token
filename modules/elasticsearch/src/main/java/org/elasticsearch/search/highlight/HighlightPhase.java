begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
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
name|analysis
operator|.
name|TokenStream
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
name|Document
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
name|Fieldable
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
name|highlight
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|collect
operator|.
name|ImmutableMap
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
name|FastStringReader
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
name|document
operator|.
name|SingleFieldSelector
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
name|function
operator|.
name|FiltersFunctionScoreQuery
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
name|function
operator|.
name|FunctionScoreQuery
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
name|FieldMapper
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
name|search
operator|.
name|SearchParseElement
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
name|InternalSearchHit
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
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
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|HighlightPhase
specifier|public
class|class
name|HighlightPhase
implements|implements
name|FetchSubPhase
block|{
DECL|class|Encoders
specifier|public
specifier|static
class|class
name|Encoders
block|{
DECL|field|DEFAULT
specifier|public
specifier|static
name|Encoder
name|DEFAULT
init|=
operator|new
name|DefaultEncoder
argument_list|()
decl_stmt|;
DECL|field|HTML
specifier|public
specifier|static
name|Encoder
name|HTML
init|=
operator|new
name|SimpleHTMLEncoder
argument_list|()
decl_stmt|;
block|}
DECL|method|parseElements
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"highlight"
argument_list|,
operator|new
name|HighlighterParseElement
argument_list|()
argument_list|)
return|;
block|}
DECL|method|hitsExecutionNeeded
annotation|@
name|Override
specifier|public
name|boolean
name|hitsExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|hitsExecute
annotation|@
name|Override
specifier|public
name|void
name|hitsExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|InternalSearchHit
index|[]
name|hits
parameter_list|)
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|hitExecutionNeeded
annotation|@
name|Override
specifier|public
name|boolean
name|hitExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
return|;
block|}
DECL|method|hitExecute
annotation|@
name|Override
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
try|try
block|{
name|DocumentMapper
name|documentMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|highlightFields
init|=
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchContextHighlight
operator|.
name|Field
name|field
range|:
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|fields
argument_list|()
control|)
block|{
name|Encoder
name|encoder
decl_stmt|;
if|if
condition|(
name|field
operator|.
name|encoder
argument_list|()
operator|.
name|equals
argument_list|(
literal|"html"
argument_list|)
condition|)
block|{
name|encoder
operator|=
name|Encoders
operator|.
name|HTML
expr_stmt|;
block|}
else|else
block|{
name|encoder
operator|=
name|Encoders
operator|.
name|DEFAULT
expr_stmt|;
block|}
name|FieldMapper
name|mapper
init|=
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
name|MapperService
operator|.
name|SmartNameFieldMappers
name|fullMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartName
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fullMapper
operator|==
literal|null
operator|||
operator|!
name|fullMapper
operator|.
name|hasDocMapper
argument_list|()
condition|)
block|{
comment|//Save skipping missing fields
continue|continue;
block|}
if|if
condition|(
operator|!
name|fullMapper
operator|.
name|docMapper
argument_list|()
operator|.
name|type
argument_list|()
operator|.
name|equals
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|mapper
operator|=
name|fullMapper
operator|.
name|mapper
argument_list|()
expr_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
block|}
comment|// if we can do highlighting using Term Vectors, use FastVectorHighlighter, otherwise, use the
comment|// slower plain highlighter
if|if
condition|(
name|mapper
operator|.
name|termVector
argument_list|()
operator|!=
name|Field
operator|.
name|TermVector
operator|.
name|WITH_POSITIONS_OFFSETS
condition|)
block|{
comment|// Don't use the context.query() since it might be rewritten, and we need to pass the non rewritten queries to
comment|// let the highlighter handle MultiTerm ones
comment|// QueryScorer uses WeightedSpanTermExtractor to extract terms, but we can't really plug into
comment|// it, so, we hack here (and really only support top level queries)
name|Query
name|query
init|=
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|instanceof
name|FunctionScoreQuery
condition|)
block|{
name|query
operator|=
operator|(
operator|(
name|FunctionScoreQuery
operator|)
name|query
operator|)
operator|.
name|getSubQuery
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|FiltersFunctionScoreQuery
condition|)
block|{
name|query
operator|=
operator|(
operator|(
name|FiltersFunctionScoreQuery
operator|)
name|query
operator|)
operator|.
name|getSubQuery
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|ConstantScoreQuery
condition|)
block|{
name|ConstantScoreQuery
name|q
init|=
operator|(
name|ConstantScoreQuery
operator|)
name|query
decl_stmt|;
if|if
condition|(
name|q
operator|.
name|getQuery
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|query
operator|=
name|q
operator|.
name|getQuery
argument_list|()
expr_stmt|;
block|}
block|}
name|QueryScorer
name|queryScorer
init|=
operator|new
name|QueryScorer
argument_list|(
name|query
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|queryScorer
operator|.
name|setExpandMultiTermQuery
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Fragmenter
name|fragmenter
decl_stmt|;
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
name|fragmenter
operator|=
operator|new
name|NullFragmenter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fragmenter
operator|=
operator|new
name|SimpleSpanFragmenter
argument_list|(
name|queryScorer
argument_list|,
name|field
operator|.
name|fragmentCharSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Formatter
name|formatter
init|=
operator|new
name|SimpleHTMLFormatter
argument_list|(
name|field
operator|.
name|preTags
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|Highlighter
name|highlighter
init|=
operator|new
name|Highlighter
argument_list|(
name|formatter
argument_list|,
name|encoder
argument_list|,
name|queryScorer
argument_list|)
decl_stmt|;
name|highlighter
operator|.
name|setTextFragmenter
argument_list|(
name|fragmenter
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|textsToHighlight
decl_stmt|;
if|if
condition|(
name|mapper
operator|.
name|stored
argument_list|()
condition|)
block|{
try|try
block|{
name|Document
name|doc
init|=
name|hitContext
operator|.
name|reader
argument_list|()
operator|.
name|document
argument_list|(
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|,
operator|new
name|SingleFieldSelector
argument_list|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|textsToHighlight
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|doc
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Fieldable
name|docField
range|:
name|doc
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|docField
operator|.
name|stringValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|textsToHighlight
operator|.
name|add
argument_list|(
name|docField
operator|.
name|stringValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
name|field
operator|.
name|field
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|SearchLookup
name|lookup
init|=
name|context
operator|.
name|lookup
argument_list|()
decl_stmt|;
name|lookup
operator|.
name|setNextReader
argument_list|(
name|hitContext
operator|.
name|reader
argument_list|()
argument_list|)
expr_stmt|;
name|lookup
operator|.
name|setNextDocId
argument_list|(
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|)
expr_stmt|;
name|textsToHighlight
operator|=
name|lookup
operator|.
name|source
argument_list|()
operator|.
name|extractRawValues
argument_list|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
literal|1
else|:
name|field
operator|.
name|numberOfFragments
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|TextFragment
argument_list|>
name|fragsList
init|=
operator|new
name|ArrayList
argument_list|<
name|TextFragment
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Object
name|textToHighlight
range|:
name|textsToHighlight
control|)
block|{
name|String
name|text
init|=
name|textToHighlight
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
decl_stmt|;
name|TokenStream
name|tokenStream
init|=
name|analyzer
operator|.
name|reusableTokenStream
argument_list|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
operator|new
name|FastStringReader
argument_list|(
name|text
argument_list|)
argument_list|)
decl_stmt|;
name|TextFragment
index|[]
name|bestTextFragments
init|=
name|highlighter
operator|.
name|getBestTextFragments
argument_list|(
name|tokenStream
argument_list|,
name|text
argument_list|,
literal|false
argument_list|,
name|numberOfFragments
argument_list|)
decl_stmt|;
for|for
control|(
name|TextFragment
name|bestTextFragment
range|:
name|bestTextFragments
control|)
block|{
if|if
condition|(
name|bestTextFragment
operator|!=
literal|null
operator|&&
name|bestTextFragment
operator|.
name|getScore
argument_list|()
operator|>
literal|0
condition|)
block|{
name|fragsList
operator|.
name|add
argument_list|(
name|bestTextFragment
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|field
operator|.
name|field
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|field
operator|.
name|scoreOrdered
argument_list|()
condition|)
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|fragsList
argument_list|,
operator|new
name|Comparator
argument_list|<
name|TextFragment
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|TextFragment
name|o1
parameter_list|,
name|TextFragment
name|o2
parameter_list|)
block|{
return|return
name|Math
operator|.
name|round
argument_list|(
name|o2
operator|.
name|getScore
argument_list|()
operator|-
name|o1
operator|.
name|getScore
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|fragments
init|=
literal|null
decl_stmt|;
comment|// number_of_fragments is set to 0 but we have a multivalued field
if|if
condition|(
name|field
operator|.
name|numberOfFragments
argument_list|()
operator|==
literal|0
operator|&&
name|textsToHighlight
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|&&
name|fragsList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|fragments
operator|=
operator|new
name|String
index|[
literal|1
index|]
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
name|fragsList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fragments
index|[
literal|0
index|]
operator|=
operator|(
name|fragments
index|[
literal|0
index|]
operator|!=
literal|null
condition|?
operator|(
name|fragments
index|[
literal|0
index|]
operator|+
literal|" "
operator|)
else|:
literal|""
operator|)
operator|+
name|fragsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// refine numberOfFragments if needed
name|numberOfFragments
operator|=
name|fragsList
operator|.
name|size
argument_list|()
operator|<
name|numberOfFragments
condition|?
name|fragsList
operator|.
name|size
argument_list|()
else|:
name|numberOfFragments
expr_stmt|;
name|fragments
operator|=
operator|new
name|String
index|[
name|numberOfFragments
index|]
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
name|fragments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fragments
index|[
name|i
index|]
operator|=
name|fragsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
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
name|HighlightField
name|highlightField
init|=
operator|new
name|HighlightField
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|,
name|fragments
argument_list|)
decl_stmt|;
name|highlightFields
operator|.
name|put
argument_list|(
name|highlightField
operator|.
name|name
argument_list|()
argument_list|,
name|highlightField
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|FragListBuilder
name|fragListBuilder
decl_stmt|;
name|FragmentsBuilder
name|fragmentsBuilder
decl_stmt|;
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
name|mapper
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
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
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
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|field
operator|.
name|fragmentOffset
argument_list|()
operator|==
operator|-
literal|1
condition|)
name|fragListBuilder
operator|=
operator|new
name|SimpleFragListBuilder
argument_list|()
expr_stmt|;
else|else
name|fragListBuilder
operator|=
operator|new
name|MarginFragListBuilder
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
name|mapper
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
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|mapper
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
name|field
operator|.
name|preTags
argument_list|()
argument_list|,
name|field
operator|.
name|postTags
argument_list|()
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
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|FastVectorHighlighter
name|highlighter
init|=
operator|new
name|FastVectorHighlighter
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|fragListBuilder
argument_list|,
name|fragmentsBuilder
argument_list|)
decl_stmt|;
name|FieldQuery
name|fieldQuery
init|=
name|buildFieldQuery
argument_list|(
name|highlighter
argument_list|,
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
argument_list|,
name|hitContext
operator|.
name|reader
argument_list|()
argument_list|,
name|field
argument_list|)
decl_stmt|;
name|String
index|[]
name|fragments
decl_stmt|;
try|try
block|{
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
literal|1
else|:
name|field
operator|.
name|numberOfFragments
argument_list|()
decl_stmt|;
name|fragments
operator|=
name|highlighter
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
name|fragmentCharSize
argument_list|()
argument_list|,
name|numberOfFragments
argument_list|,
name|fragListBuilder
argument_list|,
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
catch|catch
parameter_list|(
name|IOException
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
name|field
operator|.
name|field
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
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
name|HighlightField
name|highlightField
init|=
operator|new
name|HighlightField
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|,
name|fragments
argument_list|)
decl_stmt|;
name|highlightFields
operator|.
name|put
argument_list|(
name|highlightField
operator|.
name|name
argument_list|()
argument_list|,
name|highlightField
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|highlightFields
argument_list|(
name|highlightFields
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|CustomFieldQuery
operator|.
name|reader
operator|.
name|remove
argument_list|()
expr_stmt|;
name|CustomFieldQuery
operator|.
name|highlightFilters
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|buildFieldQuery
specifier|private
name|FieldQuery
name|buildFieldQuery
parameter_list|(
name|FastVectorHighlighter
name|highlighter
parameter_list|,
name|Query
name|query
parameter_list|,
name|IndexReader
name|indexReader
parameter_list|,
name|SearchContextHighlight
operator|.
name|Field
name|field
parameter_list|)
block|{
name|CustomFieldQuery
operator|.
name|reader
operator|.
name|set
argument_list|(
name|indexReader
argument_list|)
expr_stmt|;
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
return|return
operator|new
name|CustomFieldQuery
argument_list|(
name|query
argument_list|,
name|highlighter
argument_list|)
return|;
block|}
block|}
end_class

end_unit

