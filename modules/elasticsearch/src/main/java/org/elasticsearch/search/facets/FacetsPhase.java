begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|QueryWrapperFilter
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
name|OpenBitSet
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
name|ElasticSearchIllegalStateException
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
name|internal
operator|.
name|SearchContextFacets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FacetsPhase
specifier|public
class|class
name|FacetsPhase
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
literal|"facets"
argument_list|,
operator|new
name|FacetsParseElement
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
name|facets
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|facets
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// no need to compute the facets twice, they should be computed on a per conext basis
return|return;
block|}
name|SearchContextFacets
name|contextFacets
init|=
name|context
operator|.
name|facets
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|contextFacets
operator|.
name|queryFacets
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|SearchContextFacets
operator|.
name|QueryFacet
name|queryFacet
range|:
name|contextFacets
operator|.
name|queryFacets
argument_list|()
control|)
block|{
name|Filter
name|facetFilter
init|=
operator|new
name|QueryWrapperFilter
argument_list|(
name|queryFacet
operator|.
name|query
argument_list|()
argument_list|)
decl_stmt|;
name|facetFilter
operator|=
name|context
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|facetFilter
argument_list|)
expr_stmt|;
name|long
name|count
decl_stmt|;
if|if
condition|(
name|contextFacets
operator|.
name|queryType
argument_list|()
operator|==
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|COLLECT
condition|)
block|{
name|count
operator|=
name|executeQueryCollectorCount
argument_list|(
name|context
argument_list|,
name|queryFacet
argument_list|,
name|facetFilter
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|contextFacets
operator|.
name|queryType
argument_list|()
operator|==
name|SearchContextFacets
operator|.
name|QueryExecutionType
operator|.
name|IDSET
condition|)
block|{
name|count
operator|=
name|executeQueryIdSetCount
argument_list|(
name|context
argument_list|,
name|queryFacet
argument_list|,
name|facetFilter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"No matching for type ["
operator|+
name|contextFacets
operator|.
name|queryType
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|facets
operator|.
name|add
argument_list|(
operator|new
name|CountFacet
argument_list|(
name|queryFacet
operator|.
name|name
argument_list|()
argument_list|,
name|count
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|facets
argument_list|(
operator|new
name|Facets
argument_list|(
name|facets
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|executeQueryIdSetCount
specifier|private
name|long
name|executeQueryIdSetCount
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|SearchContextFacets
operator|.
name|QueryFacet
name|queryFacet
parameter_list|,
name|Filter
name|facetFilter
parameter_list|)
block|{
try|try
block|{
name|DocIdSet
name|filterDocIdSet
init|=
name|facetFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|OpenBitSet
operator|.
name|intersectionCount
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|docIdSet
argument_list|()
argument_list|,
operator|(
name|OpenBitSet
operator|)
name|filterDocIdSet
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|queryFacet
operator|.
name|name
argument_list|()
argument_list|,
literal|"Failed to bitset facets for query ["
operator|+
name|queryFacet
operator|.
name|query
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|executeQueryCollectorCount
specifier|private
name|long
name|executeQueryCollectorCount
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|SearchContextFacets
operator|.
name|QueryFacet
name|queryFacet
parameter_list|,
name|Filter
name|facetFilter
parameter_list|)
block|{
name|Lucene
operator|.
name|CountCollector
name|countCollector
init|=
operator|new
name|Lucene
operator|.
name|CountCollector
argument_list|(
operator|-
literal|1.0f
argument_list|)
decl_stmt|;
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|query
argument_list|()
argument_list|,
name|facetFilter
argument_list|,
name|countCollector
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
name|FacetPhaseExecutionException
argument_list|(
name|queryFacet
operator|.
name|name
argument_list|()
argument_list|,
literal|"Failed to collect facets for query ["
operator|+
name|queryFacet
operator|.
name|query
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|countCollector
operator|.
name|count
argument_list|()
return|;
block|}
block|}
end_class

end_unit

