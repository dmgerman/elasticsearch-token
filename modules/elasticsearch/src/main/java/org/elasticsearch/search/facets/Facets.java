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
name|InternalQueryFacet
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
name|statistical
operator|.
name|InternalStatisticalFacet
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
name|InternalTermsFacet
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
name|collect
operator|.
name|ImmutableList
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|Iterator
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
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
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
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Facets of search action.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|Facets
specifier|public
class|class
name|Facets
implements|implements
name|Streamable
implements|,
name|ToXContent
implements|,
name|Iterable
argument_list|<
name|Facet
argument_list|>
block|{
DECL|field|EMPTY
specifier|private
specifier|final
name|List
argument_list|<
name|Facet
argument_list|>
name|EMPTY
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|facets
specifier|private
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
init|=
name|EMPTY
decl_stmt|;
DECL|field|facetsAsMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Facet
argument_list|>
name|facetsAsMap
decl_stmt|;
DECL|method|Facets
specifier|private
name|Facets
parameter_list|()
block|{      }
comment|/**      * Constructs a new facets.      */
DECL|method|Facets
specifier|public
name|Facets
parameter_list|(
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|)
block|{
name|this
operator|.
name|facets
operator|=
name|facets
expr_stmt|;
block|}
comment|/**      * Iterates over the {@link Facet}s.      */
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Facet
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|facets
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**      * The list of {@link Facet}s.      */
DECL|method|facets
specifier|public
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
comment|/**      * Returns the {@link Facet}s keyed by map.      */
DECL|method|getFacets
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Facet
argument_list|>
name|getFacets
parameter_list|()
block|{
return|return
name|facetsAsMap
argument_list|()
return|;
block|}
comment|/**      * Returns the {@link Facet}s keyed by map.      */
DECL|method|facetsAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Facet
argument_list|>
name|facetsAsMap
parameter_list|()
block|{
if|if
condition|(
name|facetsAsMap
operator|!=
literal|null
condition|)
block|{
return|return
name|facetsAsMap
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Facet
argument_list|>
name|facetsAsMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
name|facetsAsMap
operator|.
name|put
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|facet
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|facetsAsMap
operator|=
name|facetsAsMap
expr_stmt|;
return|return
name|facetsAsMap
return|;
block|}
comment|/**      * Returns the facet by name already casted to the specified type.      */
DECL|method|facet
specifier|public
parameter_list|<
name|T
extends|extends
name|Facet
parameter_list|>
name|T
name|facet
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|facetType
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|facetType
operator|.
name|cast
argument_list|(
name|facet
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * A facet of the specified name.      */
DECL|method|facet
specifier|public
name|Facet
name|facet
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|facetsAsMap
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
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
name|builder
operator|.
name|startObject
argument_list|(
literal|"facets"
argument_list|)
expr_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
operator|(
operator|(
name|InternalFacet
operator|)
name|facet
operator|)
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|readFacets
specifier|public
specifier|static
name|Facets
name|readFacets
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Facets
name|result
init|=
operator|new
name|Facets
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|facets
operator|=
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|facets
operator|=
name|newArrayListWithCapacity
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|id
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|id
operator|==
name|Facet
operator|.
name|Type
operator|.
name|TERMS
operator|.
name|id
argument_list|()
condition|)
block|{
name|facets
operator|.
name|add
argument_list|(
name|InternalTermsFacet
operator|.
name|readTermsFacet
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|Facet
operator|.
name|Type
operator|.
name|QUERY
operator|.
name|id
argument_list|()
condition|)
block|{
name|facets
operator|.
name|add
argument_list|(
name|InternalQueryFacet
operator|.
name|readCountFacet
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|Facet
operator|.
name|Type
operator|.
name|STATISTICAL
operator|.
name|id
argument_list|()
condition|)
block|{
name|facets
operator|.
name|add
argument_list|(
name|InternalStatisticalFacet
operator|.
name|readStatisticalFacet
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't handle facet type with id ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|facets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|facet
operator|.
name|type
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|InternalFacet
operator|)
name|facet
operator|)
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

