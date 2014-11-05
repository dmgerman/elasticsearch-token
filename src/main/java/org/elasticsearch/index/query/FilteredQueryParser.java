begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|DocIdSet
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
name|FilteredQuery
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
name|FilteredQuery
operator|.
name|FilterStrategy
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
name|util
operator|.
name|Bits
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|Queries
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
name|cache
operator|.
name|filter
operator|.
name|support
operator|.
name|CacheKeyFilter
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FilteredQueryParser
specifier|public
class|class
name|FilteredQueryParser
implements|implements
name|QueryParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"filtered"
decl_stmt|;
DECL|field|ALWAYS_RANDOM_ACCESS_FILTER_STRATEGY
specifier|public
specifier|static
specifier|final
name|FilterStrategy
name|ALWAYS_RANDOM_ACCESS_FILTER_STRATEGY
init|=
operator|new
name|CustomRandomAccessFilterStrategy
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|CUSTOM_FILTER_STRATEGY
specifier|public
specifier|static
specifier|final
name|CustomRandomAccessFilterStrategy
name|CUSTOM_FILTER_STRATEGY
init|=
operator|new
name|CustomRandomAccessFilterStrategy
argument_list|()
decl_stmt|;
comment|/**      * Extends {@link org.apache.lucene.search.FilteredQuery.RandomAccessFilterStrategy}.      *<p/>      * Adds a threshold value, which defaults to -1. When set to -1, it will check if the filter docSet is      * *not*  a fast docSet, and if not, it will use {@link FilteredQuery#QUERY_FIRST_FILTER_STRATEGY} (since      * the assumption is that its a "slow" filter and better computed only on whatever matched the query).      *<p/>      * If the threshold value is 0, it always tries to pass "down" the filter as acceptDocs, and it the filter      * can't be represented as Bits (never really), then it uses {@link FilteredQuery#LEAP_FROG_QUERY_FIRST_STRATEGY}.      *<p/>      * If the above conditions are not met, then it reverts to the {@link FilteredQuery.RandomAccessFilterStrategy} logic,      * with the threshold used to control {@link #useRandomAccess(org.apache.lucene.util.Bits, int)}.      */
DECL|class|CustomRandomAccessFilterStrategy
specifier|public
specifier|static
class|class
name|CustomRandomAccessFilterStrategy
extends|extends
name|FilteredQuery
operator|.
name|RandomAccessFilterStrategy
block|{
DECL|field|threshold
specifier|private
specifier|final
name|int
name|threshold
decl_stmt|;
DECL|method|CustomRandomAccessFilterStrategy
specifier|public
name|CustomRandomAccessFilterStrategy
parameter_list|()
block|{
name|this
operator|.
name|threshold
operator|=
operator|-
literal|1
expr_stmt|;
block|}
DECL|method|CustomRandomAccessFilterStrategy
specifier|public
name|CustomRandomAccessFilterStrategy
parameter_list|(
name|int
name|threshold
parameter_list|)
block|{
name|this
operator|.
name|threshold
operator|=
name|threshold
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|filteredScorer
specifier|public
name|Scorer
name|filteredScorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|Weight
name|weight
parameter_list|,
name|DocIdSet
name|docIdSet
parameter_list|)
throws|throws
name|IOException
block|{
comment|// CHANGE: If threshold is 0, always pass down the accept docs, don't pay the price of calling nextDoc even...
if|if
condition|(
name|threshold
operator|==
literal|0
condition|)
block|{
specifier|final
name|Bits
name|filterAcceptDocs
init|=
name|docIdSet
operator|.
name|bits
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterAcceptDocs
operator|!=
literal|null
condition|)
block|{
return|return
name|weight
operator|.
name|scorer
argument_list|(
name|context
argument_list|,
name|filterAcceptDocs
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|FilteredQuery
operator|.
name|LEAP_FROG_QUERY_FIRST_STRATEGY
operator|.
name|filteredScorer
argument_list|(
name|context
argument_list|,
name|weight
argument_list|,
name|docIdSet
argument_list|)
return|;
block|}
block|}
comment|// CHANGE: handle "default" value
if|if
condition|(
name|threshold
operator|==
operator|-
literal|1
condition|)
block|{
comment|// default  value, don't iterate on only apply filter after query if its not a "fast" docIdSet
if|if
condition|(
operator|!
name|DocIdSets
operator|.
name|isFastIterator
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
return|return
name|FilteredQuery
operator|.
name|QUERY_FIRST_FILTER_STRATEGY
operator|.
name|filteredScorer
argument_list|(
name|context
argument_list|,
name|weight
argument_list|,
name|docIdSet
argument_list|)
return|;
block|}
block|}
return|return
name|super
operator|.
name|filteredScorer
argument_list|(
name|context
argument_list|,
name|weight
argument_list|,
name|docIdSet
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|useRandomAccess
specifier|protected
name|boolean
name|useRandomAccess
parameter_list|(
name|Bits
name|bits
parameter_list|,
name|long
name|filterCost
parameter_list|)
block|{
name|int
name|multiplier
init|=
name|threshold
decl_stmt|;
if|if
condition|(
name|threshold
operator|==
operator|-
literal|1
condition|)
block|{
comment|// default
name|multiplier
operator|=
literal|100
expr_stmt|;
block|}
return|return
name|filterCost
operator|*
name|multiplier
operator|>
name|bits
operator|.
name|length
argument_list|()
return|;
block|}
block|}
annotation|@
name|Inject
DECL|method|FilteredQueryParser
specifier|public
name|FilteredQueryParser
parameter_list|()
block|{     }
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
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|Query
name|query
init|=
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
decl_stmt|;
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
name|boolean
name|filterFound
init|=
literal|false
decl_stmt|;
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|boolean
name|cache
init|=
literal|false
decl_stmt|;
name|CacheKeyFilter
operator|.
name|Key
name|cacheKey
init|=
literal|null
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|FilteredQuery
operator|.
name|FilterStrategy
name|filterStrategy
init|=
name|CUSTOM_FILTER_STRATEGY
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
name|currentFieldName
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
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|query
operator|=
name|parseContext
operator|.
name|parseInnerQuery
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"filter"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|filterFound
operator|=
literal|true
expr_stmt|;
name|filter
operator|=
name|parseContext
operator|.
name|parseInnerFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[filtered] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
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
literal|"strategy"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"query_first"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
operator|||
literal|"queryFirst"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|filterStrategy
operator|=
name|FilteredQuery
operator|.
name|QUERY_FIRST_FILTER_STRATEGY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"random_access_always"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
operator|||
literal|"randomAccessAlways"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|filterStrategy
operator|=
name|ALWAYS_RANDOM_ACCESS_FILTER_STRATEGY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"leap_frog"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
operator|||
literal|"leapFrog"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|filterStrategy
operator|=
name|FilteredQuery
operator|.
name|LEAP_FROG_QUERY_FIRST_STRATEGY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|startsWith
argument_list|(
literal|"random_access_"
argument_list|)
condition|)
block|{
name|int
name|threshold
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|"random_access_"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|filterStrategy
operator|=
operator|new
name|CustomRandomAccessFilterStrategy
argument_list|(
name|threshold
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|startsWith
argument_list|(
literal|"randomAccess"
argument_list|)
condition|)
block|{
name|int
name|threshold
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|"randomAccess"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|filterStrategy
operator|=
operator|new
name|CustomRandomAccessFilterStrategy
argument_list|(
name|threshold
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"leap_frog_query_first"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
operator|||
literal|"leapFrogQueryFirst"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|filterStrategy
operator|=
name|FilteredQuery
operator|.
name|LEAP_FROG_QUERY_FIRST_STRATEGY
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"leap_frog_filter_first"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
operator|||
literal|"leapFrogFilterFirst"
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|filterStrategy
operator|=
name|FilteredQuery
operator|.
name|LEAP_FROG_FILTER_FIRST_STRATEGY
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[filtered] strategy value not supported ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queryName
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
literal|"boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_cache"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cache
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
literal|"_cache_key"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"_cacheKey"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cacheKey
operator|=
operator|new
name|CacheKeyFilter
operator|.
name|Key
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[filtered] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|// parsed internally, but returned null during parsing...
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|filterFound
condition|)
block|{
comment|// we allow for null filter, so it makes compositions on the client side to be simpler
return|return
name|query
return|;
block|}
else|else
block|{
comment|// even if the filter is not found, and its null, we should simply ignore it, and go
comment|// by the query
return|return
name|query
return|;
block|}
block|}
if|if
condition|(
name|filter
operator|==
name|Queries
operator|.
name|MATCH_ALL_FILTER
condition|)
block|{
comment|// this is an instance of match all filter, just execute the query
return|return
name|query
return|;
block|}
comment|// cache if required
if|if
condition|(
name|cache
condition|)
block|{
name|filter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|filter
argument_list|,
name|cacheKey
argument_list|)
expr_stmt|;
block|}
comment|// if its a match_all query, use constant_score
if|if
condition|(
name|Queries
operator|.
name|isConstantMatchAllQuery
argument_list|(
name|query
argument_list|)
condition|)
block|{
name|Query
name|q
init|=
operator|new
name|ConstantScoreQuery
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|q
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|q
return|;
block|}
name|FilteredQuery
name|filteredQuery
init|=
operator|new
name|FilteredQuery
argument_list|(
name|query
argument_list|,
name|filter
argument_list|,
name|filterStrategy
argument_list|)
decl_stmt|;
name|filteredQuery
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedQuery
argument_list|(
name|queryName
argument_list|,
name|filteredQuery
argument_list|)
expr_stmt|;
block|}
return|return
name|filteredQuery
return|;
block|}
block|}
end_class

end_unit

