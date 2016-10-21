begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.rescore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
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
name|search
operator|.
name|Explanation
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
name|ScoreDoc
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
name|TopDocs
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
name|ContextIndexSearcher
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
name|Arrays
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
name|Set
import|;
end_import

begin_class
DECL|class|QueryRescorer
specifier|public
specifier|final
class|class
name|QueryRescorer
implements|implements
name|Rescorer
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|Rescorer
name|INSTANCE
init|=
operator|new
name|QueryRescorer
argument_list|()
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"query"
decl_stmt|;
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|rescore
specifier|public
name|TopDocs
name|rescore
parameter_list|(
name|TopDocs
name|topDocs
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|RescoreSearchContext
name|rescoreContext
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|rescoreContext
operator|!=
literal|null
assert|;
if|if
condition|(
name|topDocs
operator|==
literal|null
operator|||
name|topDocs
operator|.
name|totalHits
operator|==
literal|0
operator|||
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|topDocs
return|;
block|}
specifier|final
name|QueryRescoreContext
name|rescore
init|=
operator|(
name|QueryRescoreContext
operator|)
name|rescoreContext
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Rescorer
name|rescorer
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|QueryRescorer
argument_list|(
name|rescore
operator|.
name|query
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|float
name|combine
parameter_list|(
name|float
name|firstPassScore
parameter_list|,
name|boolean
name|secondPassMatches
parameter_list|,
name|float
name|secondPassScore
parameter_list|)
block|{
if|if
condition|(
name|secondPassMatches
condition|)
block|{
return|return
name|rescore
operator|.
name|scoreMode
operator|.
name|combine
argument_list|(
name|firstPassScore
operator|*
name|rescore
operator|.
name|queryWeight
argument_list|()
argument_list|,
name|secondPassScore
operator|*
name|rescore
operator|.
name|rescoreQueryWeight
argument_list|()
argument_list|)
return|;
block|}
comment|// TODO: shouldn't this be up to the ScoreMode?  I.e., we should just invoke ScoreMode.combine, passing 0.0f for the
comment|// secondary score?
return|return
name|firstPassScore
operator|*
name|rescore
operator|.
name|queryWeight
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|// First take top slice of incoming docs, to be rescored:
name|TopDocs
name|topNFirstPass
init|=
name|topN
argument_list|(
name|topDocs
argument_list|,
name|rescoreContext
operator|.
name|window
argument_list|()
argument_list|)
decl_stmt|;
comment|// Rescore them:
name|TopDocs
name|rescored
init|=
name|rescorer
operator|.
name|rescore
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
argument_list|,
name|topNFirstPass
argument_list|,
name|rescoreContext
operator|.
name|window
argument_list|()
argument_list|)
decl_stmt|;
comment|// Splice back to non-topN hits and resort all of them:
return|return
name|combine
argument_list|(
name|topDocs
argument_list|,
name|rescored
argument_list|,
operator|(
name|QueryRescoreContext
operator|)
name|rescoreContext
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|int
name|topLevelDocId
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|RescoreSearchContext
name|rescoreContext
parameter_list|,
name|Explanation
name|sourceExplanation
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryRescoreContext
name|rescore
init|=
operator|(
name|QueryRescoreContext
operator|)
name|rescoreContext
decl_stmt|;
name|ContextIndexSearcher
name|searcher
init|=
name|context
operator|.
name|searcher
argument_list|()
decl_stmt|;
if|if
condition|(
name|sourceExplanation
operator|==
literal|null
condition|)
block|{
comment|// this should not happen but just in case
return|return
name|Explanation
operator|.
name|noMatch
argument_list|(
literal|"nothing matched"
argument_list|)
return|;
block|}
comment|// TODO: this isn't right?  I.e., we are incorrectly pretending all first pass hits were rescored?  If the requested docID was
comment|// beyond the top rescoreContext.window() in the first pass hits, we don't rescore it now?
name|Explanation
name|rescoreExplain
init|=
name|searcher
operator|.
name|explain
argument_list|(
name|rescore
operator|.
name|query
argument_list|()
argument_list|,
name|topLevelDocId
argument_list|)
decl_stmt|;
name|float
name|primaryWeight
init|=
name|rescore
operator|.
name|queryWeight
argument_list|()
decl_stmt|;
name|Explanation
name|prim
decl_stmt|;
if|if
condition|(
name|sourceExplanation
operator|.
name|isMatch
argument_list|()
condition|)
block|{
name|prim
operator|=
name|Explanation
operator|.
name|match
argument_list|(
name|sourceExplanation
operator|.
name|getValue
argument_list|()
operator|*
name|primaryWeight
argument_list|,
literal|"product of:"
argument_list|,
name|sourceExplanation
argument_list|,
name|Explanation
operator|.
name|match
argument_list|(
name|primaryWeight
argument_list|,
literal|"primaryWeight"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|prim
operator|=
name|Explanation
operator|.
name|noMatch
argument_list|(
literal|"First pass did not match"
argument_list|,
name|sourceExplanation
argument_list|)
expr_stmt|;
block|}
comment|// NOTE: we don't use Lucene's Rescorer.explain because we want to insert our own description with which ScoreMode was used.  Maybe
comment|// we should add QueryRescorer.explainCombine to Lucene?
if|if
condition|(
name|rescoreExplain
operator|!=
literal|null
operator|&&
name|rescoreExplain
operator|.
name|isMatch
argument_list|()
condition|)
block|{
name|float
name|secondaryWeight
init|=
name|rescore
operator|.
name|rescoreQueryWeight
argument_list|()
decl_stmt|;
name|Explanation
name|sec
init|=
name|Explanation
operator|.
name|match
argument_list|(
name|rescoreExplain
operator|.
name|getValue
argument_list|()
operator|*
name|secondaryWeight
argument_list|,
literal|"product of:"
argument_list|,
name|rescoreExplain
argument_list|,
name|Explanation
operator|.
name|match
argument_list|(
name|secondaryWeight
argument_list|,
literal|"secondaryWeight"
argument_list|)
argument_list|)
decl_stmt|;
name|QueryRescoreMode
name|scoreMode
init|=
name|rescore
operator|.
name|scoreMode
argument_list|()
decl_stmt|;
return|return
name|Explanation
operator|.
name|match
argument_list|(
name|scoreMode
operator|.
name|combine
argument_list|(
name|prim
operator|.
name|getValue
argument_list|()
argument_list|,
name|sec
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|scoreMode
operator|+
literal|" of:"
argument_list|,
name|prim
argument_list|,
name|sec
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|prim
return|;
block|}
block|}
DECL|field|SCORE_DOC_COMPARATOR
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|ScoreDoc
argument_list|>
name|SCORE_DOC_COMPARATOR
init|=
operator|new
name|Comparator
argument_list|<
name|ScoreDoc
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|ScoreDoc
name|o1
parameter_list|,
name|ScoreDoc
name|o2
parameter_list|)
block|{
name|int
name|cmp
init|=
name|Float
operator|.
name|compare
argument_list|(
name|o2
operator|.
name|score
argument_list|,
name|o1
operator|.
name|score
argument_list|)
decl_stmt|;
return|return
name|cmp
operator|==
literal|0
condition|?
name|Integer
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|doc
argument_list|,
name|o2
operator|.
name|doc
argument_list|)
else|:
name|cmp
return|;
block|}
block|}
decl_stmt|;
comment|/** Returns a new {@link TopDocs} with the topN from the incoming one, or the same TopDocs if the number of hits is already&lt;=      *  topN. */
DECL|method|topN
specifier|private
name|TopDocs
name|topN
parameter_list|(
name|TopDocs
name|in
parameter_list|,
name|int
name|topN
parameter_list|)
block|{
if|if
condition|(
name|in
operator|.
name|totalHits
operator|<
name|topN
condition|)
block|{
assert|assert
name|in
operator|.
name|scoreDocs
operator|.
name|length
operator|==
name|in
operator|.
name|totalHits
assert|;
return|return
name|in
return|;
block|}
name|ScoreDoc
index|[]
name|subset
init|=
operator|new
name|ScoreDoc
index|[
name|topN
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|in
operator|.
name|scoreDocs
argument_list|,
literal|0
argument_list|,
name|subset
argument_list|,
literal|0
argument_list|,
name|topN
argument_list|)
expr_stmt|;
return|return
operator|new
name|TopDocs
argument_list|(
name|in
operator|.
name|totalHits
argument_list|,
name|subset
argument_list|,
name|in
operator|.
name|getMaxScore
argument_list|()
argument_list|)
return|;
block|}
comment|/** Modifies incoming TopDocs (in) by replacing the top hits with resorted's hits, and then resorting all hits. */
DECL|method|combine
specifier|private
name|TopDocs
name|combine
parameter_list|(
name|TopDocs
name|in
parameter_list|,
name|TopDocs
name|resorted
parameter_list|,
name|QueryRescoreContext
name|ctx
parameter_list|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|resorted
operator|.
name|scoreDocs
argument_list|,
literal|0
argument_list|,
name|in
operator|.
name|scoreDocs
argument_list|,
literal|0
argument_list|,
name|resorted
operator|.
name|scoreDocs
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|scoreDocs
operator|.
name|length
operator|>
name|resorted
operator|.
name|scoreDocs
operator|.
name|length
condition|)
block|{
comment|// These hits were not rescored (beyond the rescore window), so we treat them the same as a hit that did get rescored but did
comment|// not match the 2nd pass query:
for|for
control|(
name|int
name|i
init|=
name|resorted
operator|.
name|scoreDocs
operator|.
name|length
init|;
name|i
operator|<
name|in
operator|.
name|scoreDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// TODO: shouldn't this be up to the ScoreMode?  I.e., we should just invoke ScoreMode.combine, passing 0.0f for the
comment|// secondary score?
name|in
operator|.
name|scoreDocs
index|[
name|i
index|]
operator|.
name|score
operator|*=
name|ctx
operator|.
name|queryWeight
argument_list|()
expr_stmt|;
block|}
comment|// TODO: this is wrong, i.e. we are comparing apples and oranges at this point.  It would be better if we always rescored all
comment|// incoming first pass hits, instead of allowing recoring of just the top subset:
name|Arrays
operator|.
name|sort
argument_list|(
name|in
operator|.
name|scoreDocs
argument_list|,
name|SCORE_DOC_COMPARATOR
argument_list|)
expr_stmt|;
block|}
comment|// update the max score after the resort
name|in
operator|.
name|setMaxScore
argument_list|(
name|in
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|score
argument_list|)
expr_stmt|;
return|return
name|in
return|;
block|}
DECL|class|QueryRescoreContext
specifier|public
specifier|static
class|class
name|QueryRescoreContext
extends|extends
name|RescoreSearchContext
block|{
DECL|field|DEFAULT_WINDOW_SIZE
specifier|static
specifier|final
name|int
name|DEFAULT_WINDOW_SIZE
init|=
literal|10
decl_stmt|;
DECL|method|QueryRescoreContext
specifier|public
name|QueryRescoreContext
parameter_list|(
name|QueryRescorer
name|rescorer
parameter_list|)
block|{
name|super
argument_list|(
name|NAME
argument_list|,
name|DEFAULT_WINDOW_SIZE
argument_list|,
name|rescorer
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreMode
operator|=
name|QueryRescoreMode
operator|.
name|Total
expr_stmt|;
block|}
DECL|field|query
specifier|private
name|Query
name|query
decl_stmt|;
DECL|field|queryWeight
specifier|private
name|float
name|queryWeight
init|=
literal|1.0f
decl_stmt|;
DECL|field|rescoreQueryWeight
specifier|private
name|float
name|rescoreQueryWeight
init|=
literal|1.0f
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|QueryRescoreMode
name|scoreMode
decl_stmt|;
DECL|method|setQuery
specifier|public
name|void
name|setQuery
parameter_list|(
name|Query
name|query
parameter_list|)
block|{
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
DECL|method|query
specifier|public
name|Query
name|query
parameter_list|()
block|{
return|return
name|query
return|;
block|}
DECL|method|queryWeight
specifier|public
name|float
name|queryWeight
parameter_list|()
block|{
return|return
name|queryWeight
return|;
block|}
DECL|method|rescoreQueryWeight
specifier|public
name|float
name|rescoreQueryWeight
parameter_list|()
block|{
return|return
name|rescoreQueryWeight
return|;
block|}
DECL|method|scoreMode
specifier|public
name|QueryRescoreMode
name|scoreMode
parameter_list|()
block|{
return|return
name|scoreMode
return|;
block|}
DECL|method|setRescoreQueryWeight
specifier|public
name|void
name|setRescoreQueryWeight
parameter_list|(
name|float
name|rescoreQueryWeight
parameter_list|)
block|{
name|this
operator|.
name|rescoreQueryWeight
operator|=
name|rescoreQueryWeight
expr_stmt|;
block|}
DECL|method|setQueryWeight
specifier|public
name|void
name|setQueryWeight
parameter_list|(
name|float
name|queryWeight
parameter_list|)
block|{
name|this
operator|.
name|queryWeight
operator|=
name|queryWeight
expr_stmt|;
block|}
DECL|method|setScoreMode
specifier|public
name|void
name|setScoreMode
parameter_list|(
name|QueryRescoreMode
name|scoreMode
parameter_list|)
block|{
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
block|}
DECL|method|setScoreMode
specifier|public
name|void
name|setScoreMode
parameter_list|(
name|String
name|scoreMode
parameter_list|)
block|{
name|setScoreMode
argument_list|(
name|QueryRescoreMode
operator|.
name|fromString
argument_list|(
name|scoreMode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|extractTerms
specifier|public
name|void
name|extractTerms
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|RescoreSearchContext
name|rescoreContext
parameter_list|,
name|Set
argument_list|<
name|Term
argument_list|>
name|termsSet
parameter_list|)
block|{
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|createNormalizedWeight
argument_list|(
operator|(
operator|(
name|QueryRescoreContext
operator|)
name|rescoreContext
operator|)
operator|.
name|query
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|extractTerms
argument_list|(
name|termsSet
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
name|IllegalStateException
argument_list|(
literal|"Failed to extract terms"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

