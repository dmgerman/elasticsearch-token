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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|com
operator|.
name|google
operator|.
name|common
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
name|search
operator|.
name|facets
operator|.
name|CountFacet
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Facets
specifier|public
class|class
name|Facets
implements|implements
name|Streamable
implements|,
name|ToJson
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
decl_stmt|;
DECL|method|Facets
specifier|private
name|Facets
parameter_list|()
block|{      }
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
DECL|method|countFacet
specifier|public
name|CountFacet
name|countFacet
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|(
name|CountFacet
operator|)
name|facet
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|facet
specifier|public
name|Facet
name|facet
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|facets
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
if|if
condition|(
name|facet
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|facet
return|;
block|}
block|}
return|return
literal|null
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
name|facet
operator|.
name|toJson
argument_list|(
name|builder
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
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
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
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|int
name|size
init|=
name|in
operator|.
name|readInt
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
name|byte
name|id
init|=
name|in
operator|.
name|readByte
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
name|COUNT
operator|.
name|id
argument_list|()
condition|)
block|{
name|facets
operator|.
name|add
argument_list|(
name|readCountFacet
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
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
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
name|write
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
name|facet
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

