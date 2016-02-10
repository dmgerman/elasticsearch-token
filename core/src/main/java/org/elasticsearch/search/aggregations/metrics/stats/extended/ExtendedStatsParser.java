begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.stats.extended
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
name|stats
operator|.
name|extended
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
name|common
operator|.
name|ParseFieldMatcher
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
name|support
operator|.
name|AbstractValuesSourceParser
operator|.
name|NumericValuesSourceParser
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
name|ValueType
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
name|ValuesSourceType
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
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ExtendedStatsParser
specifier|public
class|class
name|ExtendedStatsParser
extends|extends
name|NumericValuesSourceParser
block|{
DECL|method|ExtendedStatsParser
specifier|public
name|ExtendedStatsParser
parameter_list|()
block|{
name|super
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
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
name|InternalExtendedStats
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|token
specifier|protected
name|boolean
name|token
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|String
name|currentFieldName
parameter_list|,
name|XContentParser
operator|.
name|Token
name|token
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ExtendedStatsAggregator
operator|.
name|SIGMA_FIELD
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|ExtendedStatsAggregator
operator|.
name|SIGMA_FIELD
argument_list|,
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|createFactory
specifier|protected
name|ExtendedStatsAggregator
operator|.
name|ExtendedStatsAggregatorBuilder
name|createFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
block|{
name|ExtendedStatsAggregator
operator|.
name|ExtendedStatsAggregatorBuilder
name|factory
init|=
operator|new
name|ExtendedStatsAggregator
operator|.
name|ExtendedStatsAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
name|Double
name|sigma
init|=
operator|(
name|Double
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|ExtendedStatsAggregator
operator|.
name|SIGMA_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|sigma
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|sigma
argument_list|(
name|sigma
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
name|ExtendedStatsAggregator
operator|.
name|ExtendedStatsAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

