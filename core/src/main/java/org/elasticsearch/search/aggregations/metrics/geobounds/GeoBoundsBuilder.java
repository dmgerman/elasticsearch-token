begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.geobounds
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|geobounds
package|;
end_package

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
name|ValuesSourceAggregationBuilder
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
comment|/**  * Builder for the {@link GeoBounds} aggregation.  */
end_comment

begin_class
DECL|class|GeoBoundsBuilder
specifier|public
class|class
name|GeoBoundsBuilder
extends|extends
name|ValuesSourceAggregationBuilder
argument_list|<
name|GeoBoundsBuilder
argument_list|>
block|{
DECL|field|wrapLongitude
specifier|private
name|Boolean
name|wrapLongitude
decl_stmt|;
comment|/**      * Sole constructor.      */
DECL|method|GeoBoundsBuilder
specifier|public
name|GeoBoundsBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalGeoBounds
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Set whether to wrap longitudes. Defaults to true.      */
DECL|method|wrapLongitude
specifier|public
name|GeoBoundsBuilder
name|wrapLongitude
parameter_list|(
name|boolean
name|wrapLongitude
parameter_list|)
block|{
name|this
operator|.
name|wrapLongitude
operator|=
name|wrapLongitude
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doInternalXContent
specifier|protected
name|XContentBuilder
name|doInternalXContent
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
name|wrapLongitude
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"wrap_longitude"
argument_list|,
name|wrapLongitude
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit
