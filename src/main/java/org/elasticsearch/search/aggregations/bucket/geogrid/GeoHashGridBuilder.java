begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.geogrid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|geogrid
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|XContentBuilder
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
name|aggregations
operator|.
name|AggregationBuilder
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
comment|/**  * Creates an aggregation based on bucketing points into GeoHashes  *  */
end_comment

begin_class
DECL|class|GeoHashGridBuilder
specifier|public
class|class
name|GeoHashGridBuilder
extends|extends
name|AggregationBuilder
argument_list|<
name|GeoHashGridBuilder
argument_list|>
block|{
DECL|field|field
specifier|private
name|String
name|field
decl_stmt|;
DECL|field|precision
specifier|private
name|int
name|precision
init|=
name|GeoHashGridParser
operator|.
name|DEFAULT_PRECISION
decl_stmt|;
DECL|field|requiredSize
specifier|private
name|int
name|requiredSize
init|=
name|GeoHashGridParser
operator|.
name|DEFAULT_MAX_NUM_CELLS
decl_stmt|;
DECL|field|shardSize
specifier|private
name|int
name|shardSize
init|=
literal|0
decl_stmt|;
DECL|method|GeoHashGridBuilder
specifier|public
name|GeoHashGridBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalGeoHashGrid
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|field
specifier|public
name|GeoHashGridBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|precision
specifier|public
name|GeoHashGridBuilder
name|precision
parameter_list|(
name|int
name|precision
parameter_list|)
block|{
if|if
condition|(
operator|(
name|precision
operator|<
literal|1
operator|)
operator|||
operator|(
name|precision
operator|>
literal|12
operator|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"Invalid geohash aggregation precision of "
operator|+
name|precision
operator|+
literal|"must be between 1 and 12"
argument_list|)
throw|;
block|}
name|this
operator|.
name|precision
operator|=
name|precision
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|size
specifier|public
name|GeoHashGridBuilder
name|size
parameter_list|(
name|int
name|requiredSize
parameter_list|)
block|{
name|this
operator|.
name|requiredSize
operator|=
name|requiredSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|shardSize
specifier|public
name|GeoHashGridBuilder
name|shardSize
parameter_list|(
name|int
name|shardSize
parameter_list|)
block|{
name|this
operator|.
name|shardSize
operator|=
name|shardSize
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
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
argument_list|()
expr_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|precision
operator|!=
name|GeoHashGridParser
operator|.
name|DEFAULT_PRECISION
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"precision"
argument_list|,
name|precision
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|requiredSize
operator|!=
name|GeoHashGridParser
operator|.
name|DEFAULT_MAX_NUM_CELLS
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|requiredSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shardSize
operator|!=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"shard_size"
argument_list|,
name|shardSize
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

