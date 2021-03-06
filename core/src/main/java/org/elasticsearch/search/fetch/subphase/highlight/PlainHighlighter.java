begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|tokenattributes
operator|.
name|OffsetAttribute
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
name|highlight
operator|.
name|Formatter
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
name|Fragmenter
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
name|NullFragmenter
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
name|QueryScorer
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
name|SimpleFragmenter
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
name|SimpleHTMLFormatter
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
name|SimpleSpanFragmenter
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
name|TextFragment
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
name|BytesRefHash
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
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|internal
operator|.
name|SearchContext
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
DECL|class|PlainHighlighter
specifier|public
class|class
name|PlainHighlighter
implements|implements
name|Highlighter
block|{
DECL|field|CACHE_KEY
specifier|private
specifier|static
specifier|final
name|String
name|CACHE_KEY
init|=
literal|"highlight-plain"
decl_stmt|;
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
name|mapper
init|=
name|highlighterContext
operator|.
name|mapper
decl_stmt|;
name|Encoder
name|encoder
init|=
name|field
operator|.
name|fieldOptions
argument_list|()
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
name|Map
argument_list|<
name|FieldMapper
argument_list|,
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
argument_list|>
name|mappers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|put
argument_list|(
name|CACHE_KEY
argument_list|,
name|mappers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|FieldMapper
argument_list|,
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
argument_list|>
name|cache
init|=
operator|(
name|Map
argument_list|<
name|FieldMapper
argument_list|,
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
argument_list|>
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
name|entry
init|=
name|cache
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
name|QueryScorer
name|queryScorer
init|=
operator|new
name|CustomQueryScorer
argument_list|(
name|highlighterContext
operator|.
name|query
argument_list|,
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|requireFieldMatch
argument_list|()
condition|?
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
else|:
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
name|fieldOptions
argument_list|()
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
elseif|else
if|if
condition|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|fragmenter
argument_list|()
operator|==
literal|null
condition|)
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
name|fieldOptions
argument_list|()
operator|.
name|fragmentCharSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"simple"
operator|.
name|equals
argument_list|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|fragmenter
argument_list|()
argument_list|)
condition|)
block|{
name|fragmenter
operator|=
operator|new
name|SimpleFragmenter
argument_list|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|fragmentCharSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"span"
operator|.
name|equals
argument_list|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|fragmenter
argument_list|()
argument_list|)
condition|)
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
name|fieldOptions
argument_list|()
operator|.
name|fragmentCharSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown fragmenter option ["
operator|+
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|fragmenter
argument_list|()
operator|+
literal|"] for the field ["
operator|+
name|highlighterContext
operator|.
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Formatter
name|formatter
init|=
operator|new
name|SimpleHTMLFormatter
argument_list|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|preTags
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|postTags
argument_list|()
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|entry
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
name|highlight
operator|.
name|Highlighter
argument_list|(
name|formatter
argument_list|,
name|encoder
argument_list|,
name|queryScorer
argument_list|)
expr_stmt|;
name|entry
operator|.
name|setTextFragmenter
argument_list|(
name|fragmenter
argument_list|)
expr_stmt|;
comment|// always highlight across all data
name|entry
operator|.
name|setMaxDocCharsToAnalyze
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|mapper
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
comment|// a HACK to make highlighter do highlighting, even though its using the single frag list builder
name|int
name|numberOfFragments
init|=
name|field
operator|.
name|fieldOptions
argument_list|()
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
name|fieldOptions
argument_list|()
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
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|textsToHighlight
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
name|getType
argument_list|()
argument_list|)
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
decl_stmt|;
try|try
block|{
name|textsToHighlight
operator|=
name|HighlightUtils
operator|.
name|loadFieldValues
argument_list|(
name|field
argument_list|,
name|mapper
argument_list|,
name|context
argument_list|,
name|hitContext
argument_list|)
expr_stmt|;
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
decl_stmt|;
if|if
condition|(
name|textToHighlight
operator|instanceof
name|BytesRef
condition|)
block|{
name|text
operator|=
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|valueForDisplay
argument_list|(
name|textToHighlight
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|text
operator|=
name|textToHighlight
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
try|try
init|(
name|TokenStream
name|tokenStream
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|text
argument_list|)
init|)
block|{
if|if
condition|(
operator|!
name|tokenStream
operator|.
name|hasAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
operator|||
operator|!
name|tokenStream
operator|.
name|hasAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// can't perform highlighting if the stream has no terms (binary token stream) or no offsets
continue|continue;
block|}
name|TextFragment
index|[]
name|bestTextFragments
init|=
name|entry
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrap
argument_list|(
name|e
argument_list|,
name|BytesRefHash
operator|.
name|MaxBytesLengthExceededException
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// this can happen if for example a field is not_analyzed and ignore_above option is set.
comment|// the field will be ignored when indexing but the huge term is still in the source and
comment|// the plain highlighter will parse the source and try to analyze it.
return|return
literal|null
return|;
block|}
else|else
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
if|if
condition|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|scoreOrdered
argument_list|()
condition|)
block|{
name|CollectionUtil
operator|.
name|introSort
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
annotation|@
name|Override
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
decl_stmt|;
comment|// number_of_fragments is set to 0 but we have a multivalued field
if|if
condition|(
name|field
operator|.
name|fieldOptions
argument_list|()
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
name|fragsList
operator|.
name|size
argument_list|()
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
name|highlighterContext
operator|.
name|fieldName
argument_list|,
name|Text
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
name|fieldOptions
argument_list|()
operator|.
name|noMatchSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|noMatchSize
operator|>
literal|0
operator|&&
name|textsToHighlight
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// Pull an excerpt from the beginning of the string but make sure to split the string on a term boundary.
name|String
name|fieldContents
init|=
name|textsToHighlight
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|end
decl_stmt|;
try|try
block|{
name|end
operator|=
name|findGoodEndForNoHighlightExcerpt
argument_list|(
name|noMatchSize
argument_list|,
name|analyzer
argument_list|,
name|mapper
operator|.
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|fieldContents
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|end
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|HighlightField
argument_list|(
name|highlighterContext
operator|.
name|fieldName
argument_list|,
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|fieldContents
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|end
argument_list|)
argument_list|)
block|}
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|canHighlight
specifier|public
name|boolean
name|canHighlight
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
DECL|method|findGoodEndForNoHighlightExcerpt
specifier|private
specifier|static
name|int
name|findGoodEndForNoHighlightExcerpt
parameter_list|(
name|int
name|noMatchSize
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|String
name|contents
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|TokenStream
name|tokenStream
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|fieldName
argument_list|,
name|contents
argument_list|)
init|)
block|{
if|if
condition|(
operator|!
name|tokenStream
operator|.
name|hasAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// Can't split on term boundaries without offsets
return|return
operator|-
literal|1
return|;
block|}
name|int
name|end
init|=
operator|-
literal|1
decl_stmt|;
name|tokenStream
operator|.
name|reset
argument_list|()
expr_stmt|;
while|while
condition|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|OffsetAttribute
name|attr
init|=
name|tokenStream
operator|.
name|getAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|attr
operator|.
name|endOffset
argument_list|()
operator|>=
name|noMatchSize
condition|)
block|{
comment|// Jump to the end of this token if it wouldn't put us past the boundary
if|if
condition|(
name|attr
operator|.
name|endOffset
argument_list|()
operator|==
name|noMatchSize
condition|)
block|{
name|end
operator|=
name|noMatchSize
expr_stmt|;
block|}
return|return
name|end
return|;
block|}
name|end
operator|=
name|attr
operator|.
name|endOffset
argument_list|()
expr_stmt|;
block|}
name|tokenStream
operator|.
name|end
argument_list|()
expr_stmt|;
comment|// We've exhausted the token stream so we should just highlight everything.
return|return
name|end
return|;
block|}
block|}
block|}
end_class

end_unit

