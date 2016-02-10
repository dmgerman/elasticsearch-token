begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles
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
name|percentiles
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
name|ParseField
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
name|AggregatorBuilder
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
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
operator|.
name|InternalTDigestPercentileRanks
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
name|support
operator|.
name|ValuesSource
operator|.
name|Numeric
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
name|support
operator|.
name|ValuesSourceAggregatorBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PercentileRanksParser
specifier|public
class|class
name|PercentileRanksParser
extends|extends
name|AbstractPercentilesParser
block|{
DECL|field|VALUES_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|VALUES_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
DECL|method|PercentileRanksParser
specifier|public
name|PercentileRanksParser
parameter_list|()
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalTDigestPercentileRanks
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|keysField
specifier|protected
name|ParseField
name|keysField
parameter_list|()
block|{
return|return
name|VALUES_FIELD
return|;
block|}
annotation|@
name|Override
DECL|method|buildFactory
specifier|protected
name|ValuesSourceAggregatorBuilder
argument_list|<
name|Numeric
argument_list|,
name|?
argument_list|>
name|buildFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|double
index|[]
name|keys
parameter_list|,
name|PercentilesMethod
name|method
parameter_list|,
name|Double
name|compression
parameter_list|,
name|Integer
name|numberOfSignificantValueDigits
parameter_list|,
name|Boolean
name|keyed
parameter_list|)
block|{
name|PercentileRanksAggregatorBuilder
name|factory
init|=
operator|new
name|PercentileRanksAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
if|if
condition|(
name|keys
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|values
argument_list|(
name|keys
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|method
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compression
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|compression
argument_list|(
name|compression
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numberOfSignificantValueDigits
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|numberOfSignificantValueDigits
argument_list|(
name|numberOfSignificantValueDigits
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|keyed
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|keyed
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|AggregatorBuilder
argument_list|<
name|?
argument_list|>
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
name|PercentileRanksAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

