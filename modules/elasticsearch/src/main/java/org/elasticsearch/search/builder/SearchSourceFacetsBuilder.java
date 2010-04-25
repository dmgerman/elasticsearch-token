begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.builder
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
package|;
end_package

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
name|json
operator|.
name|JsonQueryBuilder
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
name|json
operator|.
name|JsonBuilder
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
name|json
operator|.
name|ToJson
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A search source facets builder.  *  * @author kimchy (shay.banon)  * @see SearchSourceBuilder#facets(SearchSourceFacetsBuilder)  */
end_comment

begin_class
DECL|class|SearchSourceFacetsBuilder
specifier|public
class|class
name|SearchSourceFacetsBuilder
implements|implements
name|ToJson
block|{
DECL|field|queryExecution
specifier|private
name|String
name|queryExecution
decl_stmt|;
DECL|field|queryFacets
specifier|private
name|List
argument_list|<
name|FacetQuery
argument_list|>
name|queryFacets
decl_stmt|;
comment|/**      * Controls the type of query facet execution.      */
DECL|method|queryExecution
specifier|public
name|SearchSourceFacetsBuilder
name|queryExecution
parameter_list|(
name|String
name|queryExecution
parameter_list|)
block|{
name|this
operator|.
name|queryExecution
operator|=
name|queryExecution
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a query facet (which results in a count facet returned).      *      * @param name  The logical name of the facet, it will be returned under the name      * @param query The query facet      */
DECL|method|facet
specifier|public
name|SearchSourceFacetsBuilder
name|facet
parameter_list|(
name|String
name|name
parameter_list|,
name|JsonQueryBuilder
name|query
parameter_list|)
block|{
if|if
condition|(
name|queryFacets
operator|==
literal|null
condition|)
block|{
name|queryFacets
operator|=
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|queryFacets
operator|.
name|add
argument_list|(
operator|new
name|FacetQuery
argument_list|(
name|name
argument_list|,
name|query
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a query facet (which results in a count facet returned) with an option to      * be global on the index or bounded by the search query.      *      * @param name  The logical name of the facet, it will be returned under the name      * @param query The query facet      */
DECL|method|facet
specifier|public
name|SearchSourceFacetsBuilder
name|facet
parameter_list|(
name|String
name|name
parameter_list|,
name|JsonQueryBuilder
name|query
parameter_list|,
name|boolean
name|global
parameter_list|)
block|{
if|if
condition|(
name|queryFacets
operator|==
literal|null
condition|)
block|{
name|queryFacets
operator|=
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|queryFacets
operator|.
name|add
argument_list|(
operator|new
name|FacetQuery
argument_list|(
name|name
argument_list|,
name|query
argument_list|,
name|global
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toJson
annotation|@
name|Override
specifier|public
name|void
name|toJson
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|queryExecution
operator|==
literal|null
operator|&&
name|queryFacets
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"facets"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|queryExecution
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"query_execution"
argument_list|,
name|queryExecution
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryFacets
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FacetQuery
name|facetQuery
range|:
name|queryFacets
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|facetQuery
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|)
expr_stmt|;
name|facetQuery
operator|.
name|queryBuilder
argument_list|()
operator|.
name|toJson
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|facetQuery
operator|.
name|global
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"global"
argument_list|,
name|facetQuery
operator|.
name|global
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|class|FacetQuery
specifier|private
specifier|static
class|class
name|FacetQuery
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|queryBuilder
specifier|private
specifier|final
name|JsonQueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|global
specifier|private
specifier|final
name|Boolean
name|global
decl_stmt|;
DECL|method|FacetQuery
specifier|private
name|FacetQuery
parameter_list|(
name|String
name|name
parameter_list|,
name|JsonQueryBuilder
name|queryBuilder
parameter_list|,
name|Boolean
name|global
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
name|this
operator|.
name|global
operator|=
name|global
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|queryBuilder
specifier|public
name|JsonQueryBuilder
name|queryBuilder
parameter_list|()
block|{
return|return
name|queryBuilder
return|;
block|}
DECL|method|global
specifier|public
name|Boolean
name|global
parameter_list|()
block|{
return|return
name|this
operator|.
name|global
return|;
block|}
block|}
block|}
end_class

end_unit

