begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.geodistance
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
operator|.
name|geodistance
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|SortedNumericDocValues
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
name|geo
operator|.
name|GeoDistance
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
name|geo
operator|.
name|GeoDistance
operator|.
name|FixedSourceDistance
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
name|geo
operator|.
name|GeoPoint
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
name|common
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
name|common
operator|.
name|unit
operator|.
name|DistanceUnit
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
name|index
operator|.
name|fielddata
operator|.
name|MultiGeoPointValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|SortedBinaryDocValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|SortedNumericDoubleValues
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
name|Aggregator
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
name|InternalRange
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
name|Unmapped
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
name|pipeline
operator|.
name|PipelineAggregator
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
name|GeoPointValuesSourceParser
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
name|AggregationContext
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
name|GeoPointParser
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
name|ValuesSource
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
name|ValuesSourceAggregatorFactory
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|GeoDistanceParser
specifier|public
class|class
name|GeoDistanceParser
extends|extends
name|GeoPointValuesSourceParser
block|{
DECL|field|ORIGIN_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|ORIGIN_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"origin"
argument_list|,
literal|"center"
argument_list|,
literal|"point"
argument_list|,
literal|"por"
argument_list|)
decl_stmt|;
DECL|field|UNIT_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|UNIT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"unit"
argument_list|)
decl_stmt|;
DECL|field|DISTANCE_TYPE_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|DISTANCE_TYPE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"distance_type"
argument_list|)
decl_stmt|;
DECL|field|geoPointParser
specifier|private
name|GeoPointParser
name|geoPointParser
init|=
operator|new
name|GeoPointParser
argument_list|(
name|InternalGeoDistance
operator|.
name|TYPE
argument_list|,
name|ORIGIN_FIELD
argument_list|)
decl_stmt|;
DECL|method|GeoDistanceParser
specifier|public
name|GeoDistanceParser
parameter_list|()
block|{
name|super
argument_list|(
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
name|InternalGeoDistance
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
DECL|class|Range
specifier|public
specifier|static
class|class
name|Range
extends|extends
name|RangeAggregator
operator|.
name|Range
block|{
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|Range
name|PROTOTYPE
init|=
operator|new
name|Range
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|method|Range
specifier|public
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|Double
name|from
parameter_list|,
name|Double
name|to
parameter_list|)
block|{
name|super
argument_list|(
name|key
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
DECL|method|key
specifier|private
specifier|static
name|String
name|key
parameter_list|(
name|String
name|key
parameter_list|,
name|Double
name|from
parameter_list|,
name|Double
name|to
parameter_list|)
block|{
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
return|return
name|key
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
operator|(
name|from
operator|==
literal|null
operator|||
name|from
operator|==
literal|0
operator|)
condition|?
literal|"*"
else|:
name|from
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
operator|(
name|to
operator|==
literal|null
operator|||
name|Double
operator|.
name|isInfinite
argument_list|(
name|to
argument_list|)
operator|)
condition|?
literal|"*"
else|:
name|to
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|Range
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|double
name|from
init|=
name|in
operator|.
name|readDouble
argument_list|()
decl_stmt|;
name|double
name|to
init|=
name|in
operator|.
name|readDouble
argument_list|()
decl_stmt|;
return|return
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|writeOptionalString
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|createFactory
specifier|protected
name|GeoDistanceFactory
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
name|GeoPoint
name|origin
init|=
operator|(
name|GeoPoint
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|ORIGIN_FIELD
argument_list|)
decl_stmt|;
name|GeoDistanceFactory
name|factory
init|=
operator|new
name|GeoDistanceFactory
argument_list|(
name|aggregationName
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
init|=
operator|(
name|List
argument_list|<
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
name|DistanceUnit
name|unit
init|=
operator|(
name|DistanceUnit
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|UNIT_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|unit
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|unit
argument_list|(
name|unit
argument_list|)
expr_stmt|;
block|}
name|GeoDistance
name|distanceType
init|=
operator|(
name|GeoDistance
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|DISTANCE_TYPE_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|distanceType
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|distanceType
argument_list|(
name|distanceType
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
name|geoPointParser
operator|.
name|token
argument_list|(
name|aggregationName
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|,
name|otherOptions
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
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
name|VALUE_STRING
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
name|UNIT_FIELD
argument_list|)
condition|)
block|{
name|DistanceUnit
name|unit
init|=
name|DistanceUnit
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|otherOptions
operator|.
name|put
argument_list|(
name|UNIT_FIELD
argument_list|,
name|unit
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|DISTANCE_TYPE_FIELD
argument_list|)
condition|)
block|{
name|GeoDistance
name|distanceType
init|=
name|GeoDistance
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|otherOptions
operator|.
name|put
argument_list|(
name|DISTANCE_TYPE_FIELD
argument_list|,
name|distanceType
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
elseif|else
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
name|String
name|fromAsStr
init|=
literal|null
decl_stmt|;
name|String
name|toAsStr
init|=
literal|null
decl_stmt|;
name|double
name|from
init|=
literal|0.0
decl_stmt|;
name|double
name|to
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|String
name|key
init|=
literal|null
decl_stmt|;
name|String
name|toOrFromOrKey
init|=
literal|null
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
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|toOrFromOrKey
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
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
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|toOrFromOrKey
argument_list|,
name|Range
operator|.
name|FROM_FIELD
argument_list|)
condition|)
block|{
name|from
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|toOrFromOrKey
argument_list|,
name|Range
operator|.
name|TO_FIELD
argument_list|)
condition|)
block|{
name|to
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
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
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|toOrFromOrKey
argument_list|,
name|Range
operator|.
name|KEY_FIELD
argument_list|)
condition|)
block|{
name|key
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|toOrFromOrKey
argument_list|,
name|Range
operator|.
name|FROM_FIELD
argument_list|)
condition|)
block|{
name|fromAsStr
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|toOrFromOrKey
argument_list|,
name|Range
operator|.
name|TO_FIELD
argument_list|)
condition|)
block|{
name|toAsStr
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|fromAsStr
operator|!=
literal|null
operator|||
name|toAsStr
operator|!=
literal|null
condition|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|Double
operator|.
name|parseDouble
argument_list|(
name|fromAsStr
argument_list|)
argument_list|,
name|Double
operator|.
name|parseDouble
argument_list|(
name|toAsStr
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
return|return
literal|false
return|;
block|}
DECL|class|GeoDistanceFactory
specifier|public
specifier|static
class|class
name|GeoDistanceFactory
extends|extends
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|,
name|GeoDistanceFactory
argument_list|>
block|{
DECL|field|origin
specifier|private
specifier|final
name|GeoPoint
name|origin
decl_stmt|;
DECL|field|rangeFactory
specifier|private
specifier|final
name|InternalRange
operator|.
name|Factory
name|rangeFactory
decl_stmt|;
DECL|field|ranges
specifier|private
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
DECL|field|unit
specifier|private
name|DistanceUnit
name|unit
init|=
name|DistanceUnit
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|distanceType
specifier|private
name|GeoDistance
name|distanceType
init|=
name|GeoDistance
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
init|=
literal|false
decl_stmt|;
DECL|method|GeoDistanceFactory
specifier|public
name|GeoDistanceFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|GeoPoint
name|origin
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|origin
argument_list|,
name|InternalGeoDistance
operator|.
name|FACTORY
argument_list|)
expr_stmt|;
block|}
DECL|method|GeoDistanceFactory
specifier|private
name|GeoDistanceFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|GeoPoint
name|origin
parameter_list|,
name|InternalRange
operator|.
name|Factory
name|rangeFactory
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|rangeFactory
operator|.
name|type
argument_list|()
argument_list|,
name|rangeFactory
operator|.
name|getValueSourceType
argument_list|()
argument_list|,
name|rangeFactory
operator|.
name|getValueType
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|origin
expr_stmt|;
name|this
operator|.
name|rangeFactory
operator|=
name|rangeFactory
expr_stmt|;
block|}
DECL|method|addRange
specifier|public
name|GeoDistanceFactory
name|addRange
parameter_list|(
name|Range
name|range
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
name|range
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Add a new range to this aggregation.          *          * @param key          *            the key to use for this range in the response          * @param from          *            the lower bound on the distances, inclusive          * @param to          *            the upper bound on the distances, exclusive          */
DECL|method|addRange
specifier|public
name|GeoDistanceFactory
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Same as {@link #addRange(String, double, double)} but the key will be          * automatically generated based on<code>from</code> and          *<code>to</code>.          */
DECL|method|addRange
specifier|public
name|GeoDistanceFactory
name|addRange
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**          * Add a new range with no lower bound.          *          * @param key          *            the key to use for this range in the response          * @param to          *            the upper bound on the distances, exclusive          */
DECL|method|addUnboundedTo
specifier|public
name|GeoDistanceFactory
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Same as {@link #addUnboundedTo(String, double)} but the key will be          * computed automatically.          */
DECL|method|addUnboundedTo
specifier|public
name|GeoDistanceFactory
name|addUnboundedTo
parameter_list|(
name|double
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**          * Add a new range with no upper bound.          *          * @param key          *            the key to use for this range in the response          * @param from          *            the lower bound on the distances, inclusive          */
DECL|method|addUnboundedFrom
specifier|public
name|GeoDistanceFactory
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Same as {@link #addUnboundedFrom(String, double)} but the key will be          * computed automatically.          */
DECL|method|addUnboundedFrom
specifier|public
name|GeoDistanceFactory
name|addUnboundedFrom
parameter_list|(
name|double
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
DECL|method|range
specifier|public
name|List
argument_list|<
name|Range
argument_list|>
name|range
parameter_list|()
block|{
return|return
name|ranges
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|InternalGeoDistance
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
DECL|method|unit
specifier|public
name|GeoDistanceFactory
name|unit
parameter_list|(
name|DistanceUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|unit
specifier|public
name|DistanceUnit
name|unit
parameter_list|()
block|{
return|return
name|unit
return|;
block|}
DECL|method|distanceType
specifier|public
name|GeoDistanceFactory
name|distanceType
parameter_list|(
name|GeoDistance
name|distanceType
parameter_list|)
block|{
name|this
operator|.
name|distanceType
operator|=
name|distanceType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|distanceType
specifier|public
name|GeoDistance
name|distanceType
parameter_list|()
block|{
return|return
name|distanceType
return|;
block|}
DECL|method|keyed
specifier|public
name|GeoDistanceFactory
name|keyed
parameter_list|(
name|boolean
name|keyed
parameter_list|)
block|{
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|keyed
specifier|public
name|boolean
name|keyed
parameter_list|()
block|{
return|return
name|keyed
return|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Unmapped
argument_list|(
name|name
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|rangeFactory
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doCreateInternal
specifier|protected
name|Aggregator
name|doCreateInternal
parameter_list|(
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|DistanceSource
name|distanceSource
init|=
operator|new
name|DistanceSource
argument_list|(
name|valuesSource
argument_list|,
name|distanceType
argument_list|,
name|origin
argument_list|,
name|unit
argument_list|)
decl_stmt|;
return|return
operator|new
name|RangeAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|distanceSource
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|rangeFactory
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|XContentBuilder
name|doXContentBody
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
name|field
argument_list|(
name|ORIGIN_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|origin
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|RANGES_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|ranges
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|RangeAggregator
operator|.
name|KEYED_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|keyed
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|UNIT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|unit
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|DISTANCE_TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|distanceType
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|innerReadFrom
specifier|protected
name|GeoDistanceFactory
name|innerReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|GeoPoint
name|origin
init|=
operator|new
name|GeoPoint
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|GeoDistanceFactory
name|factory
init|=
operator|new
name|GeoDistanceFactory
argument_list|(
name|name
argument_list|,
name|origin
argument_list|)
decl_stmt|;
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
name|factory
operator|.
name|addRange
argument_list|(
name|Range
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|keyed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|factory
operator|.
name|distanceType
operator|=
name|GeoDistance
operator|.
name|readGeoDistanceFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|factory
operator|.
name|unit
operator|=
name|DistanceUnit
operator|.
name|readDistanceUnit
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|protected
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|origin
operator|.
name|lat
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|origin
operator|.
name|lon
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|ranges
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|range
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
name|distanceType
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|DistanceUnit
operator|.
name|writeDistanceUnit
argument_list|(
name|out
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerHashCode
specifier|protected
name|int
name|innerHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|origin
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|distanceType
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerEquals
specifier|protected
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|GeoDistanceFactory
name|other
init|=
operator|(
name|GeoDistanceFactory
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|origin
argument_list|,
name|other
operator|.
name|origin
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|ranges
argument_list|,
name|other
operator|.
name|ranges
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|keyed
argument_list|,
name|other
operator|.
name|keyed
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|distanceType
argument_list|,
name|other
operator|.
name|distanceType
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|unit
argument_list|,
name|other
operator|.
name|unit
argument_list|)
return|;
block|}
DECL|class|DistanceSource
specifier|private
specifier|static
class|class
name|DistanceSource
extends|extends
name|ValuesSource
operator|.
name|Numeric
block|{
DECL|field|source
specifier|private
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|source
decl_stmt|;
DECL|field|distanceType
specifier|private
specifier|final
name|GeoDistance
name|distanceType
decl_stmt|;
DECL|field|unit
specifier|private
specifier|final
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|origin
specifier|private
specifier|final
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoPoint
name|origin
decl_stmt|;
DECL|method|DistanceSource
specifier|public
name|DistanceSource
parameter_list|(
name|ValuesSource
operator|.
name|GeoPoint
name|source
parameter_list|,
name|GeoDistance
name|distanceType
parameter_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoPoint
name|origin
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
comment|// even if the geo points are unique, there's no guarantee the distances are
name|this
operator|.
name|distanceType
operator|=
name|distanceType
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|origin
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isFloatingPoint
specifier|public
name|boolean
name|isFloatingPoint
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|longValues
specifier|public
name|SortedNumericDocValues
name|longValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|doubleValues
specifier|public
name|SortedNumericDoubleValues
name|doubleValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
specifier|final
name|MultiGeoPointValues
name|geoValues
init|=
name|source
operator|.
name|geoPointValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|FixedSourceDistance
name|distance
init|=
name|distanceType
operator|.
name|fixedSourceDistance
argument_list|(
name|origin
operator|.
name|getLat
argument_list|()
argument_list|,
name|origin
operator|.
name|getLon
argument_list|()
argument_list|,
name|unit
argument_list|)
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|distanceValues
argument_list|(
name|geoValues
argument_list|,
name|distance
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|bytesValues
specifier|public
name|SortedBinaryDocValues
name|bytesValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
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
name|GeoDistanceFactory
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

