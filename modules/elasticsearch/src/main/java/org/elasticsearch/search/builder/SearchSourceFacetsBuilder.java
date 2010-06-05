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
name|xcontent
operator|.
name|XContentQueryBuilder
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
name|facets
operator|.
name|query
operator|.
name|QueryFacetCollectorParser
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
name|facets
operator|.
name|terms
operator|.
name|TermFacetCollectorParser
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|builder
operator|.
name|XContentBuilder
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
name|ToXContent
block|{
DECL|field|queryFacets
specifier|private
name|List
argument_list|<
name|QueryFacet
argument_list|>
name|queryFacets
decl_stmt|;
DECL|field|termsFacets
specifier|private
name|List
argument_list|<
name|TermsFacet
argument_list|>
name|termsFacets
decl_stmt|;
comment|/**      * Adds a query facet (which results in a count facet returned).      *      * @param name  The logical name of the facet, it will be returned under the name      * @param query The query facet      */
DECL|method|queryFacet
specifier|public
name|SearchSourceFacetsBuilder
name|queryFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|XContentQueryBuilder
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
name|QueryFacet
argument_list|(
name|name
argument_list|,
name|query
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a query facet (which results in a count facet returned) with an option to      * be global on the index or bounded by the search query.      *      * @param name   The logical name of the facet, it will be returned under the name      * @param query  The query facet      * @param global Should the facet be executed globally or not      */
DECL|method|queryFacetGlobal
specifier|public
name|SearchSourceFacetsBuilder
name|queryFacetGlobal
parameter_list|(
name|String
name|name
parameter_list|,
name|XContentQueryBuilder
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
name|QueryFacet
argument_list|(
name|name
argument_list|,
name|query
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|termsFacet
specifier|public
name|SearchSourceFacetsBuilder
name|termsFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|termsFacets
operator|==
literal|null
condition|)
block|{
name|termsFacets
operator|=
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|termsFacets
operator|.
name|add
argument_list|(
operator|new
name|TermsFacet
argument_list|(
name|name
argument_list|,
name|fieldName
argument_list|,
name|size
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|termsFacetGlobal
specifier|public
name|SearchSourceFacetsBuilder
name|termsFacetGlobal
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|termsFacets
operator|==
literal|null
condition|)
block|{
name|termsFacets
operator|=
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|termsFacets
operator|.
name|add
argument_list|(
operator|new
name|TermsFacet
argument_list|(
name|name
argument_list|,
name|fieldName
argument_list|,
name|size
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
name|toXContent
parameter_list|(
name|XContentBuilder
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
name|queryFacets
operator|==
literal|null
operator|&&
name|termsFacets
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
name|queryFacets
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|QueryFacet
name|queryFacet
range|:
name|queryFacets
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|queryFacet
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|QueryFacetCollectorParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|queryFacet
operator|.
name|queryBuilder
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryFacet
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
name|queryFacet
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
if|if
condition|(
name|termsFacets
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|TermsFacet
name|termsFacet
range|:
name|termsFacets
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|termsFacet
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|TermFacetCollectorParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|termsFacet
operator|.
name|fieldName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|termsFacet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|termsFacet
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
name|termsFacet
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
DECL|class|TermsFacet
specifier|private
specifier|static
class|class
name|TermsFacet
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|global
specifier|private
specifier|final
name|Boolean
name|global
decl_stmt|;
DECL|method|TermsFacet
specifier|private
name|TermsFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|int
name|size
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
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
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
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|global
specifier|public
name|Boolean
name|global
parameter_list|()
block|{
return|return
name|global
return|;
block|}
block|}
DECL|class|QueryFacet
specifier|private
specifier|static
class|class
name|QueryFacet
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
name|XContentQueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|global
specifier|private
specifier|final
name|Boolean
name|global
decl_stmt|;
DECL|method|QueryFacet
specifier|private
name|QueryFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|XContentQueryBuilder
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
name|XContentQueryBuilder
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

