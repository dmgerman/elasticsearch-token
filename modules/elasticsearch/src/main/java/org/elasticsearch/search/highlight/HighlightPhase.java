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
name|com
operator|.
name|google
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
name|search
operator|.
name|SearchHit
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
name|SearchPhase
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
name|HashMap
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|HighlightPhase
specifier|public
class|class
name|HighlightPhase
implements|implements
name|SearchPhase
block|{
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
DECL|method|preProcess
annotation|@
name|Override
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{     }
DECL|method|execute
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|FragListBuilder
name|fragListBuilder
init|=
operator|new
name|SimpleFragListBuilder
argument_list|()
decl_stmt|;
name|FragmentsBuilder
name|fragmentsBuilder
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|scoreOrdered
argument_list|()
condition|)
block|{
name|fragmentsBuilder
operator|=
operator|new
name|ScoreOrderFragmentsBuilder
argument_list|(
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|preTags
argument_list|()
argument_list|,
name|context
operator|.
name|highlight
argument_list|()
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
name|SimpleFragmentsBuilder
argument_list|(
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|preTags
argument_list|()
argument_list|,
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|postTags
argument_list|()
argument_list|)
expr_stmt|;
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
name|highlighter
operator|.
name|getFieldQuery
argument_list|(
name|context
operator|.
name|query
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|context
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
control|)
block|{
name|InternalSearchHit
name|internalHit
init|=
operator|(
name|InternalSearchHit
operator|)
name|hit
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|type
argument_list|(
name|internalHit
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|docId
init|=
name|internalHit
operator|.
name|docId
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|highlightFields
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchContextHighlight
operator|.
name|ParsedHighlightField
name|parsedHighlightField
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
name|String
name|indexName
init|=
name|parsedHighlightField
operator|.
name|field
argument_list|()
decl_stmt|;
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
name|parsedHighlightField
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|indexName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
block|}
name|String
index|[]
name|fragments
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fragments
operator|=
name|highlighter
operator|.
name|getBestFragments
argument_list|(
name|fieldQuery
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
argument_list|,
name|docId
argument_list|,
name|indexName
argument_list|,
name|parsedHighlightField
operator|.
name|fragmentCharSize
argument_list|()
argument_list|,
name|parsedHighlightField
operator|.
name|numberOfFragments
argument_list|()
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
name|parsedHighlightField
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
name|HighlightField
name|highlightField
init|=
operator|new
name|HighlightField
argument_list|(
name|parsedHighlightField
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
name|internalHit
operator|.
name|highlightFields
argument_list|(
name|highlightFields
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

