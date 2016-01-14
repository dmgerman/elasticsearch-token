begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range
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
name|range
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
name|AggregatorFactory
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
name|bucket
operator|.
name|range
operator|.
name|RangeAggregator
operator|.
name|Range
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
name|ArrayList
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
comment|/**  *  */
end_comment

begin_class
DECL|class|RangeParser
specifier|public
class|class
name|RangeParser
extends|extends
name|NumericValuesSourceParser
block|{
DECL|method|RangeParser
specifier|public
name|RangeParser
parameter_list|()
block|{
name|this
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|RangeParser
specifier|protected
name|RangeParser
parameter_list|(
name|boolean
name|scriptable
parameter_list|,
name|boolean
name|formattable
parameter_list|,
name|boolean
name|timezoneAware
parameter_list|)
block|{
name|super
argument_list|(
name|scriptable
argument_list|,
name|formattable
argument_list|,
name|timezoneAware
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
name|InternalRange
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|createFactory
specifier|protected
name|RangeAggregator
operator|.
name|AbstractFactory
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
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
name|RangeAggregator
operator|.
name|Factory
name|factory
init|=
operator|new
name|RangeAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Range
argument_list|>
name|ranges
init|=
operator|(
name|List
argument_list|<
name|?
extends|extends
name|Range
argument_list|>
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|RangeAggregator
operator|.
name|RANGES_FIELD
argument_list|)
decl_stmt|;
for|for
control|(
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|factory
operator|.
name|addRange
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
name|Boolean
name|keyed
init|=
operator|(
name|Boolean
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|RangeAggregator
operator|.
name|KEYED_FIELD
argument_list|)
decl_stmt|;
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
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|RangeAggregator
operator|.
name|RANGES_FIELD
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|Range
name|range
init|=
name|parseRange
argument_list|(
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|ranges
operator|.
name|add
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
name|otherOptions
operator|.
name|put
argument_list|(
name|RangeAggregator
operator|.
name|RANGES_FIELD
argument_list|,
name|ranges
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_BOOLEAN
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|RangeAggregator
operator|.
name|KEYED_FIELD
argument_list|)
condition|)
block|{
name|boolean
name|keyed
init|=
name|parser
operator|.
name|booleanValue
argument_list|()
decl_stmt|;
name|otherOptions
operator|.
name|put
argument_list|(
name|RangeAggregator
operator|.
name|KEYED_FIELD
argument_list|,
name|keyed
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
DECL|method|parseRange
specifier|protected
name|Range
name|parseRange
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Range
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
operator|new
name|RangeAggregator
operator|.
name|Factory
argument_list|(
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

