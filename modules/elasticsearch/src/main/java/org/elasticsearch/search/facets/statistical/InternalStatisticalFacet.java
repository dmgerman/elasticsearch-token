begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.statistical
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|statistical
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
name|Facet
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
name|internal
operator|.
name|InternalFacet
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalStatisticalFacet
specifier|public
class|class
name|InternalStatisticalFacet
implements|implements
name|StatisticalFacet
implements|,
name|InternalFacet
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|min
specifier|private
name|double
name|min
decl_stmt|;
DECL|field|max
specifier|private
name|double
name|max
decl_stmt|;
DECL|field|total
specifier|private
name|double
name|total
decl_stmt|;
DECL|field|count
specifier|private
name|long
name|count
decl_stmt|;
DECL|method|InternalStatisticalFacet
specifier|private
name|InternalStatisticalFacet
parameter_list|()
block|{     }
DECL|method|InternalStatisticalFacet
specifier|public
name|InternalStatisticalFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|min
parameter_list|,
name|double
name|max
parameter_list|,
name|double
name|total
parameter_list|,
name|long
name|count
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
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|getName
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
argument_list|()
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|Type
operator|.
name|STATISTICAL
return|;
block|}
DECL|method|getType
annotation|@
name|Override
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
argument_list|()
return|;
block|}
DECL|method|count
annotation|@
name|Override
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|this
operator|.
name|count
return|;
block|}
DECL|method|getCount
annotation|@
name|Override
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|count
argument_list|()
return|;
block|}
DECL|method|total
annotation|@
name|Override
specifier|public
name|double
name|total
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
DECL|method|getTotal
annotation|@
name|Override
specifier|public
name|double
name|getTotal
parameter_list|()
block|{
return|return
name|total
argument_list|()
return|;
block|}
DECL|method|mean
annotation|@
name|Override
specifier|public
name|double
name|mean
parameter_list|()
block|{
return|return
name|total
operator|/
name|count
return|;
block|}
DECL|method|getMean
annotation|@
name|Override
specifier|public
name|double
name|getMean
parameter_list|()
block|{
return|return
name|mean
argument_list|()
return|;
block|}
DECL|method|min
annotation|@
name|Override
specifier|public
name|double
name|min
parameter_list|()
block|{
return|return
name|this
operator|.
name|min
return|;
block|}
DECL|method|getMin
annotation|@
name|Override
specifier|public
name|double
name|getMin
parameter_list|()
block|{
return|return
name|min
argument_list|()
return|;
block|}
DECL|method|max
annotation|@
name|Override
specifier|public
name|double
name|max
parameter_list|()
block|{
return|return
name|this
operator|.
name|max
return|;
block|}
DECL|method|getMax
annotation|@
name|Override
specifier|public
name|double
name|getMax
parameter_list|()
block|{
return|return
name|max
argument_list|()
return|;
block|}
DECL|method|aggregate
annotation|@
name|Override
specifier|public
name|Facet
name|aggregate
parameter_list|(
name|Iterable
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|)
block|{
name|double
name|min
init|=
name|Double
operator|.
name|MAX_VALUE
decl_stmt|;
name|double
name|max
init|=
name|Double
operator|.
name|MIN_VALUE
decl_stmt|;
name|double
name|total
init|=
literal|0
decl_stmt|;
name|long
name|count
init|=
literal|0
decl_stmt|;
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
operator|!
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
continue|continue;
block|}
name|InternalStatisticalFacet
name|statsFacet
init|=
operator|(
name|InternalStatisticalFacet
operator|)
name|facet
decl_stmt|;
if|if
condition|(
name|statsFacet
operator|.
name|min
argument_list|()
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|statsFacet
operator|.
name|min
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|statsFacet
operator|.
name|max
argument_list|()
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|statsFacet
operator|.
name|max
argument_list|()
expr_stmt|;
block|}
name|total
operator|+=
name|statsFacet
operator|.
name|total
argument_list|()
expr_stmt|;
name|count
operator|+=
name|statsFacet
operator|.
name|count
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|InternalStatisticalFacet
argument_list|(
name|name
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|total
argument_list|,
name|count
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
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_type"
argument_list|,
literal|"statistical"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"count"
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"min"
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"max"
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|readStatisticalFacet
specifier|public
specifier|static
name|StatisticalFacet
name|readStatisticalFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalStatisticalFacet
name|facet
init|=
operator|new
name|InternalStatisticalFacet
argument_list|()
decl_stmt|;
name|facet
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|facet
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
name|name
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|count
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|total
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|min
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|max
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
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
name|writeUTF
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|count
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|min
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|max
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

