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
name|FieldType
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
name|Fields
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
name|MultiFields
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
name|index
operator|.
name|TermsEnum
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
name|index
operator|.
name|mapper
operator|.
name|ParseContext
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

begin_comment
comment|/**  * Utility to extract query terms from queries and create queries from documents.  */
end_comment

begin_class
DECL|class|ExtractQueryTermsService
specifier|public
specifier|final
class|class
name|ExtractQueryTermsService
block|{
DECL|field|FIELD_VALUE_SEPARATOR
specifier|private
specifier|static
specifier|final
name|byte
name|FIELD_VALUE_SEPARATOR
init|=
literal|0
decl_stmt|;
comment|// nul code point
DECL|method|ExtractQueryTermsService
specifier|private
name|ExtractQueryTermsService
parameter_list|()
block|{     }
comment|/**      * Extracts all terms from the specified query and adds it to the specified document.      * @param query                 The query to extract terms from      * @param document              The document to add the extracted terms to      * @param queryTermsFieldField  The field in the document holding the extracted terms      * @param unknownQueryField     The field used to mark a document that not all query terms could be extracted.      *                              For example the query contained an unsupported query (e.g. WildcardQuery).      * @param fieldType The field type for the query metadata field      */
DECL|method|extractQueryTerms
specifier|public
specifier|static
name|void
name|extractQueryTerms
parameter_list|(
name|Query
name|query
parameter_list|,
name|ParseContext
operator|.
name|Document
name|document
parameter_list|,
name|String
name|queryTermsFieldField
parameter_list|,
name|String
name|unknownQueryField
parameter_list|,
name|FieldType
name|fieldType
parameter_list|)
block|{
name|Set
argument_list|<
name|Term
argument_list|>
name|queryTerms
decl_stmt|;
try|try
block|{
name|queryTerms
operator|=
name|extractQueryTerms
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedQueryException
name|e
parameter_list|)
block|{
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|unknownQueryField
argument_list|,
operator|new
name|BytesRef
argument_list|()
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|Term
name|term
range|:
name|queryTerms
control|)
block|{
name|BytesRefBuilder
name|builder
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|FIELD_VALUE_SEPARATOR
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|term
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|queryTermsFieldField
argument_list|,
name|builder
operator|.
name|toBytesRef
argument_list|()
argument_list|,
name|fieldType
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Extracts all query terms from the provided query and adds it to specified list.      *      * From boolean query with no should clauses or phrase queries only the longest term are selected,      * since that those terms are likely to be the rarest. Boolean query's must_not clauses are always ignored.      *      * If from part of the query, no query terms can be extracted then term extraction is stopped and      * an UnsupportedQueryException is thrown.      */
DECL|method|extractQueryTerms
specifier|static
name|Set
argument_list|<
name|Term
argument_list|>
name|extractQueryTerms
parameter_list|(
name|Query
name|query
parameter_list|)
block|{
if|if
condition|(
name|query
operator|instanceof
name|MatchNoDocsQuery
condition|)
block|{
comment|// no terms to extract as this query matches no docs
return|return
name|Collections
operator|.
name|emptySet
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|TermQuery
condition|)
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
operator|(
operator|(
name|TermQuery
operator|)
name|query
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|TermsQuery
condition|)
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
name|TermsQuery
name|termsQuery
init|=
operator|(
name|TermsQuery
operator|)
name|query
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
name|terms
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|PhraseQuery
condition|)
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
name|Collections
operator|.
name|emptySet
argument_list|()
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
name|Collections
operator|.
name|singleton
argument_list|(
name|longestTerm
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|BooleanQuery
condition|)
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|(
operator|(
name|BooleanQuery
operator|)
name|query
operator|)
operator|.
name|clauses
argument_list|()
decl_stmt|;
name|boolean
name|hasRequiredClauses
init|=
literal|false
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
name|hasRequiredClauses
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|hasRequiredClauses
condition|)
block|{
name|UnsupportedQueryException
name|uqe
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|Term
argument_list|>
name|bestClause
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
name|Set
argument_list|<
name|Term
argument_list|>
name|temp
decl_stmt|;
try|try
block|{
name|temp
operator|=
name|extractQueryTerms
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
name|bestClause
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
throw|throw
name|uqe
throw|;
block|}
return|return
name|Collections
operator|.
name|emptySet
argument_list|()
return|;
block|}
block|}
else|else
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
name|isProhibited
argument_list|()
condition|)
block|{
comment|// we don't need to remember the things that do *not* match...
continue|continue;
block|}
name|terms
operator|.
name|addAll
argument_list|(
name|extractQueryTerms
argument_list|(
name|clause
operator|.
name|getQuery
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|ConstantScoreQuery
condition|)
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
name|extractQueryTerms
argument_list|(
name|wrappedQuery
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|BoostQuery
condition|)
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
name|extractQueryTerms
argument_list|(
name|wrappedQuery
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|CommonTermsQuery
condition|)
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
name|HashSet
argument_list|<>
argument_list|(
name|terms
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|BlendedTermQuery
condition|)
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
name|HashSet
argument_list|<>
argument_list|(
name|terms
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|DisjunctionMaxQuery
condition|)
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
name|disjuncts
control|)
block|{
name|terms
operator|.
name|addAll
argument_list|(
name|extractQueryTerms
argument_list|(
name|disjunct
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanTermQuery
condition|)
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
operator|(
operator|(
name|SpanTermQuery
operator|)
name|query
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanNearQuery
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
name|Set
argument_list|<
name|Term
argument_list|>
name|temp
init|=
name|extractQueryTerms
argument_list|(
name|clause
argument_list|)
decl_stmt|;
name|bestClause
operator|=
name|selectTermListWithTheLongestShortestTerm
argument_list|(
name|temp
argument_list|,
name|bestClause
argument_list|)
expr_stmt|;
block|}
return|return
name|bestClause
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanOrQuery
condition|)
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
name|extractQueryTerms
argument_list|(
name|clause
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanFirstQuery
condition|)
block|{
return|return
name|extractQueryTerms
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
return|;
block|}
elseif|else
if|if
condition|(
name|query
operator|instanceof
name|SpanNotQuery
condition|)
block|{
return|return
name|extractQueryTerms
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
specifier|private
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
comment|/**      * Creates a boolean query with a should clause for each term on all fields of the specified index reader.      */
DECL|method|createQueryTermsQuery
specifier|public
specifier|static
name|Query
name|createQueryTermsQuery
parameter_list|(
name|IndexReader
name|indexReader
parameter_list|,
name|String
name|queryMetadataField
parameter_list|,
name|String
name|unknownQueryField
parameter_list|)
throws|throws
name|IOException
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|queryMetadataField
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|unknownQueryField
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Term
argument_list|>
name|extractedTerms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|extractedTerms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|unknownQueryField
argument_list|)
argument_list|)
expr_stmt|;
name|Fields
name|fields
init|=
name|MultiFields
operator|.
name|getFields
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|BytesRef
name|fieldBr
init|=
operator|new
name|BytesRef
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|TermsEnum
name|tenum
init|=
name|terms
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|BytesRef
name|term
init|=
name|tenum
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
name|tenum
operator|.
name|next
argument_list|()
control|)
block|{
name|BytesRefBuilder
name|builder
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|fieldBr
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|FIELD_VALUE_SEPARATOR
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|extractedTerms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|queryMetadataField
argument_list|,
name|builder
operator|.
name|toBytesRef
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|TermsQuery
argument_list|(
name|extractedTerms
argument_list|)
return|;
block|}
comment|/**      * Exception indicating that none or some query terms couldn't extracted from a percolator query.      */
DECL|class|UnsupportedQueryException
specifier|public
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

