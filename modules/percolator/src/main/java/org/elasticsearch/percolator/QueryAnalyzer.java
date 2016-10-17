begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|PrefixCodedTerms
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
name|queries
operator|.
name|BlendedTermQuery
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
name|CommonTermsQuery
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
name|BoostQuery
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
name|DisjunctionMaxQuery
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
name|PhraseQuery
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
name|SynonymQuery
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
name|search
operator|.
name|spans
operator|.
name|SpanFirstQuery
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
name|spans
operator|.
name|SpanNearQuery
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
name|spans
operator|.
name|SpanNotQuery
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
name|spans
operator|.
name|SpanOrQuery
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
name|spans
operator|.
name|SpanQuery
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
name|spans
operator|.
name|SpanTermQuery
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
name|common
operator|.
name|logging
operator|.
name|LoggerMessageFormat
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
name|MatchNoDocsQuery
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
name|HashMap
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
name|Locale
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|QueryAnalyzer
specifier|public
specifier|final
class|class
name|QueryAnalyzer
block|{
DECL|field|queryProcessors
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Query
argument_list|>
argument_list|,
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
argument_list|>
name|queryProcessors
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Query
argument_list|>
argument_list|,
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|,
name|matchNoDocsQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|,
name|constantScoreQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|BoostQuery
operator|.
name|class
argument_list|,
name|boostQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|,
name|termQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|TermsQuery
operator|.
name|class
argument_list|,
name|termsQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|CommonTermsQuery
operator|.
name|class
argument_list|,
name|commonTermsQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|BlendedTermQuery
operator|.
name|class
argument_list|,
name|blendedTermQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|PhraseQuery
operator|.
name|class
argument_list|,
name|phraseQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SpanTermQuery
operator|.
name|class
argument_list|,
name|spanTermQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SpanNearQuery
operator|.
name|class
argument_list|,
name|spanNearQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SpanOrQuery
operator|.
name|class
argument_list|,
name|spanOrQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SpanFirstQuery
operator|.
name|class
argument_list|,
name|spanFirstQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SpanNotQuery
operator|.
name|class
argument_list|,
name|spanNotQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|,
name|booleanQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|DisjunctionMaxQuery
operator|.
name|class
argument_list|,
name|disjunctionMaxQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|SynonymQuery
operator|.
name|class
argument_list|,
name|synonymQuery
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|FunctionScoreQuery
operator|.
name|class
argument_list|,
name|functionScoreQuery
argument_list|()
argument_list|)
expr_stmt|;
name|queryProcessors
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
DECL|method|QueryAnalyzer
specifier|private
name|QueryAnalyzer
parameter_list|()
block|{     }
comment|/**      * Extracts terms from the provided query. These terms are stored with the percolator query and      * used by the percolate query's candidate query as fields to be query by. The candidate query      * holds the terms from the document to be percolated and allows to the percolate query to ignore      * percolator queries that we know would otherwise never match.      *      *<p>      * When extracting the terms for the specified query, we can also determine if the percolator query is      * always going to match. For example if a percolator query just contains a term query or a disjunction      * query then when the candidate query matches with that, we know the entire percolator query always      * matches. This allows the percolate query to skip the expensive memory index verification step that      * it would otherwise have to execute (for example when a percolator query contains a phrase query or a      * conjunction query).      *      *<p>      * The query analyzer doesn't always extract all terms from the specified query. For example from a      * boolean query with no should clauses or phrase queries only the longest term are selected,      * since that those terms are likely to be the rarest. Boolean query's must_not clauses are always ignored.      *      *<p>      * Sometimes the query analyzer can't always extract terms from a sub query, if that happens then      * query analysis is stopped and an UnsupportedQueryException is thrown. So that the caller can mark      * this query in such a way that the PercolatorQuery always verifies if this query with the MemoryIndex.      */
DECL|method|analyze
specifier|public
specifier|static
name|Result
name|analyze
parameter_list|(
name|Query
name|query
parameter_list|)
block|{
name|Class
name|queryClass
init|=
name|query
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryClass
operator|.
name|isAnonymousClass
argument_list|()
condition|)
block|{
comment|// Sometimes queries have anonymous classes in that case we need the direct super class.
comment|// (for example blended term query)
name|queryClass
operator|=
name|queryClass
operator|.
name|getSuperclass
argument_list|()
expr_stmt|;
block|}
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|queryProcessor
init|=
name|queryProcessors
operator|.
name|get
argument_list|(
name|queryClass
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryProcessor
operator|!=
literal|null
condition|)
block|{
return|return
name|queryProcessor
operator|.
name|apply
argument_list|(
name|query
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedQueryException
argument_list|(
name|query
argument_list|)
throw|;
block|}
block|}
DECL|method|matchNoDocsQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|matchNoDocsQuery
parameter_list|()
block|{
return|return
operator|(
name|query
lambda|->
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
operator|)
return|;
block|}
DECL|method|constantScoreQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|constantScoreQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Query
name|wrappedQuery
init|=
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|query
operator|)
operator|.
name|getQuery
argument_list|()
decl_stmt|;
return|return
name|analyze
argument_list|(
name|wrappedQuery
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|boostQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|boostQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Query
name|wrappedQuery
init|=
operator|(
operator|(
name|BoostQuery
operator|)
name|query
operator|)
operator|.
name|getQuery
argument_list|()
decl_stmt|;
return|return
name|analyze
argument_list|(
name|wrappedQuery
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|termQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|termQuery
parameter_list|()
block|{
return|return
operator|(
name|query
lambda|->
block|{
name|TermQuery
name|termQuery
init|=
operator|(
name|TermQuery
operator|)
name|query
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|termQuery
operator|.
name|getTerm
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
operator|)
return|;
block|}
DECL|method|termsQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|termsQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|TermsQuery
name|termsQuery
init|=
operator|(
name|TermsQuery
operator|)
name|query
decl_stmt|;
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|PrefixCodedTerms
operator|.
name|TermIterator
name|iterator
init|=
name|termsQuery
operator|.
name|getTermData
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|BytesRef
name|term
init|=
name|iterator
operator|.
name|next
argument_list|()
init|;
name|term
operator|!=
literal|null
condition|;
name|term
operator|=
name|iterator
operator|.
name|next
argument_list|()
control|)
block|{
name|terms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|iterator
operator|.
name|field
argument_list|()
argument_list|,
name|term
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|synonymQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|synonymQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
operator|(
operator|(
name|SynonymQuery
operator|)
name|query
operator|)
operator|.
name|getTerms
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|commonTermsQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|commonTermsQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|List
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|(
operator|(
name|CommonTermsQuery
operator|)
name|query
operator|)
operator|.
name|getTerms
argument_list|()
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|terms
argument_list|)
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|blendedTermQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|blendedTermQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|List
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|(
operator|(
name|BlendedTermQuery
operator|)
name|query
operator|)
operator|.
name|getTerms
argument_list|()
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|terms
argument_list|)
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|phraseQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|phraseQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Term
index|[]
name|terms
init|=
operator|(
operator|(
name|PhraseQuery
operator|)
name|query
operator|)
operator|.
name|getTerms
argument_list|()
decl_stmt|;
if|if
condition|(
name|terms
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
return|;
block|}
comment|// the longest term is likely to be the rarest,
comment|// so from a performance perspective it makes sense to extract that
name|Term
name|longestTerm
init|=
name|terms
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|Term
name|term
range|:
name|terms
control|)
block|{
if|if
condition|(
name|longestTerm
operator|.
name|bytes
argument_list|()
operator|.
name|length
operator|<
name|term
operator|.
name|bytes
argument_list|()
operator|.
name|length
condition|)
block|{
name|longestTerm
operator|=
name|term
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|longestTerm
argument_list|)
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|spanTermQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|spanTermQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Term
name|term
init|=
operator|(
operator|(
name|SpanTermQuery
operator|)
name|query
operator|)
operator|.
name|getTerm
argument_list|()
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|term
argument_list|)
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|spanNearQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|spanNearQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|bestClauses
init|=
literal|null
decl_stmt|;
name|SpanNearQuery
name|spanNearQuery
init|=
operator|(
name|SpanNearQuery
operator|)
name|query
decl_stmt|;
for|for
control|(
name|SpanQuery
name|clause
range|:
name|spanNearQuery
operator|.
name|getClauses
argument_list|()
control|)
block|{
name|Result
name|temp
init|=
name|analyze
argument_list|(
name|clause
argument_list|)
decl_stmt|;
name|bestClauses
operator|=
name|selectTermListWithTheLongestShortestTerm
argument_list|(
name|temp
operator|.
name|terms
argument_list|,
name|bestClauses
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|bestClauses
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|spanOrQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|spanOrQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|SpanOrQuery
name|spanOrQuery
init|=
operator|(
name|SpanOrQuery
operator|)
name|query
decl_stmt|;
for|for
control|(
name|SpanQuery
name|clause
range|:
name|spanOrQuery
operator|.
name|getClauses
argument_list|()
control|)
block|{
name|terms
operator|.
name|addAll
argument_list|(
name|analyze
argument_list|(
name|clause
argument_list|)
operator|.
name|terms
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|spanNotQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|spanNotQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Result
name|result
init|=
name|analyze
argument_list|(
operator|(
operator|(
name|SpanNotQuery
operator|)
name|query
operator|)
operator|.
name|getInclude
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|result
operator|.
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|spanFirstQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|spanFirstQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|Result
name|result
init|=
name|analyze
argument_list|(
operator|(
operator|(
name|SpanFirstQuery
operator|)
name|query
operator|)
operator|.
name|getMatch
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|result
operator|.
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|booleanQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|booleanQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|BooleanQuery
name|bq
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
name|bq
operator|.
name|clauses
argument_list|()
decl_stmt|;
name|int
name|minimumShouldMatch
init|=
name|bq
operator|.
name|getMinimumNumberShouldMatch
argument_list|()
decl_stmt|;
name|int
name|numRequiredClauses
init|=
literal|0
decl_stmt|;
name|int
name|numOptionalClauses
init|=
literal|0
decl_stmt|;
name|int
name|numProhibitedClauses
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|clauses
control|)
block|{
if|if
condition|(
name|clause
operator|.
name|isRequired
argument_list|()
condition|)
block|{
name|numRequiredClauses
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|clause
operator|.
name|isProhibited
argument_list|()
condition|)
block|{
name|numProhibitedClauses
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
condition|)
block|{
name|numOptionalClauses
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|numRequiredClauses
operator|>
literal|0
condition|)
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|bestClause
init|=
literal|null
decl_stmt|;
name|UnsupportedQueryException
name|uqe
init|=
literal|null
decl_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|clauses
control|)
block|{
if|if
condition|(
name|clause
operator|.
name|isRequired
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// skip must_not clauses, we don't need to remember the things that do *not* match...
comment|// skip should clauses, this bq has must clauses, so we don't need to remember should clauses,
comment|// since they are completely optional.
continue|continue;
block|}
name|Result
name|temp
decl_stmt|;
try|try
block|{
name|temp
operator|=
name|analyze
argument_list|(
name|clause
operator|.
name|getQuery
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedQueryException
name|e
parameter_list|)
block|{
name|uqe
operator|=
name|e
expr_stmt|;
continue|continue;
block|}
name|bestClause
operator|=
name|selectTermListWithTheLongestShortestTerm
argument_list|(
name|temp
operator|.
name|terms
argument_list|,
name|bestClause
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bestClause
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|Result
argument_list|(
literal|false
argument_list|,
name|bestClause
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|uqe
operator|!=
literal|null
condition|)
block|{
comment|// we're unable to select the best clause and an exception occurred, so we bail
throw|throw
name|uqe
throw|;
block|}
else|else
block|{
comment|// We didn't find a clause and no exception occurred, so this bq only contained MatchNoDocsQueries,
return|return
operator|new
name|Result
argument_list|(
literal|true
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|Query
argument_list|>
name|disjunctions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numOptionalClauses
argument_list|)
decl_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|clauses
control|)
block|{
if|if
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
condition|)
block|{
name|disjunctions
operator|.
name|add
argument_list|(
name|clause
operator|.
name|getQuery
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|handleDisjunction
argument_list|(
name|disjunctions
argument_list|,
name|minimumShouldMatch
argument_list|,
name|numProhibitedClauses
operator|>
literal|0
argument_list|)
return|;
block|}
block|}
return|;
block|}
DECL|method|disjunctionMaxQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|disjunctionMaxQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|List
argument_list|<
name|Query
argument_list|>
name|disjuncts
init|=
operator|(
operator|(
name|DisjunctionMaxQuery
operator|)
name|query
operator|)
operator|.
name|getDisjuncts
argument_list|()
decl_stmt|;
return|return
name|handleDisjunction
argument_list|(
name|disjuncts
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|functionScoreQuery
specifier|static
name|Function
argument_list|<
name|Query
argument_list|,
name|Result
argument_list|>
name|functionScoreQuery
parameter_list|()
block|{
return|return
name|query
lambda|->
block|{
name|FunctionScoreQuery
name|functionScoreQuery
init|=
operator|(
name|FunctionScoreQuery
operator|)
name|query
decl_stmt|;
name|Result
name|result
init|=
name|analyze
argument_list|(
name|functionScoreQuery
operator|.
name|getSubQuery
argument_list|()
argument_list|)
decl_stmt|;
comment|// If min_score is specified we can't guarantee upfront that this percolator query matches,
comment|// so in that case we set verified to false.
comment|// (if it matches with the percolator document matches with the extracted terms.
comment|// Min score filters out docs, which is different than the functions, which just influences the score.)
name|boolean
name|verified
init|=
name|functionScoreQuery
operator|.
name|getMinScore
argument_list|()
operator|==
literal|null
decl_stmt|;
return|return
operator|new
name|Result
argument_list|(
name|verified
argument_list|,
name|result
operator|.
name|terms
argument_list|)
return|;
block|}
return|;
block|}
DECL|method|handleDisjunction
specifier|static
name|Result
name|handleDisjunction
parameter_list|(
name|List
argument_list|<
name|Query
argument_list|>
name|disjunctions
parameter_list|,
name|int
name|minimumShouldMatch
parameter_list|,
name|boolean
name|otherClauses
parameter_list|)
block|{
name|boolean
name|verified
init|=
name|minimumShouldMatch
operator|<=
literal|1
operator|&&
name|otherClauses
operator|==
literal|false
decl_stmt|;
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Query
name|disjunct
range|:
name|disjunctions
control|)
block|{
name|Result
name|subResult
init|=
name|analyze
argument_list|(
name|disjunct
argument_list|)
decl_stmt|;
if|if
condition|(
name|subResult
operator|.
name|verified
operator|==
literal|false
condition|)
block|{
name|verified
operator|=
literal|false
expr_stmt|;
block|}
name|terms
operator|.
name|addAll
argument_list|(
name|subResult
operator|.
name|terms
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Result
argument_list|(
name|verified
argument_list|,
name|terms
argument_list|)
return|;
block|}
DECL|method|selectTermListWithTheLongestShortestTerm
specifier|static
name|Set
argument_list|<
name|Term
argument_list|>
name|selectTermListWithTheLongestShortestTerm
parameter_list|(
name|Set
argument_list|<
name|Term
argument_list|>
name|terms1
parameter_list|,
name|Set
argument_list|<
name|Term
argument_list|>
name|terms2
parameter_list|)
block|{
if|if
condition|(
name|terms1
operator|==
literal|null
condition|)
block|{
return|return
name|terms2
return|;
block|}
elseif|else
if|if
condition|(
name|terms2
operator|==
literal|null
condition|)
block|{
return|return
name|terms1
return|;
block|}
else|else
block|{
name|int
name|terms1ShortestTerm
init|=
name|minTermLength
argument_list|(
name|terms1
argument_list|)
decl_stmt|;
name|int
name|terms2ShortestTerm
init|=
name|minTermLength
argument_list|(
name|terms2
argument_list|)
decl_stmt|;
comment|// keep the clause with longest terms, this likely to be rarest.
if|if
condition|(
name|terms1ShortestTerm
operator|>=
name|terms2ShortestTerm
condition|)
block|{
return|return
name|terms1
return|;
block|}
else|else
block|{
return|return
name|terms2
return|;
block|}
block|}
block|}
DECL|method|minTermLength
specifier|static
name|int
name|minTermLength
parameter_list|(
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
parameter_list|)
block|{
name|int
name|min
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|Term
name|term
range|:
name|terms
control|)
block|{
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|min
argument_list|,
name|term
operator|.
name|bytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|min
return|;
block|}
DECL|class|Result
specifier|static
class|class
name|Result
block|{
DECL|field|terms
specifier|final
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
decl_stmt|;
DECL|field|verified
specifier|final
name|boolean
name|verified
decl_stmt|;
DECL|method|Result
name|Result
parameter_list|(
name|boolean
name|verified
parameter_list|,
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
parameter_list|)
block|{
name|this
operator|.
name|terms
operator|=
name|terms
expr_stmt|;
name|this
operator|.
name|verified
operator|=
name|verified
expr_stmt|;
block|}
block|}
comment|/**      * Exception indicating that none or some query terms couldn't extracted from a percolator query.      */
DECL|class|UnsupportedQueryException
specifier|static
class|class
name|UnsupportedQueryException
extends|extends
name|RuntimeException
block|{
DECL|field|unsupportedQuery
specifier|private
specifier|final
name|Query
name|unsupportedQuery
decl_stmt|;
DECL|method|UnsupportedQueryException
specifier|public
name|UnsupportedQueryException
parameter_list|(
name|Query
name|unsupportedQuery
parameter_list|)
block|{
name|super
argument_list|(
name|LoggerMessageFormat
operator|.
name|format
argument_list|(
literal|"no query terms can be extracted from query [{}]"
argument_list|,
name|unsupportedQuery
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|unsupportedQuery
operator|=
name|unsupportedQuery
expr_stmt|;
block|}
comment|/**          * The actual Lucene query that was unsupported and caused this exception to be thrown.          */
DECL|method|getUnsupportedQuery
specifier|public
name|Query
name|getUnsupportedQuery
parameter_list|()
block|{
return|return
name|unsupportedQuery
return|;
block|}
block|}
block|}
end_class

end_unit
