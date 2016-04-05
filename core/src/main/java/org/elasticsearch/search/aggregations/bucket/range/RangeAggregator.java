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
name|util
operator|.
name|InPlaceMergeSorter
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|ToXContent
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
name|DocValueFormat
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
name|AggregatorFactories
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
name|InternalAggregation
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
name|InternalAggregations
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
name|LeafBucketCollector
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
name|LeafBucketCollectorBase
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
name|NonCollectingAggregator
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
name|BucketsAggregator
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
name|internal
operator|.
name|SearchContext
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
DECL|class|RangeAggregator
specifier|public
class|class
name|RangeAggregator
extends|extends
name|BucketsAggregator
block|{
DECL|field|RANGES_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|RANGES_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"ranges"
argument_list|)
decl_stmt|;
DECL|field|KEYED_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|KEYED_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"keyed"
argument_list|)
decl_stmt|;
DECL|class|Range
specifier|public
specifier|static
class|class
name|Range
implements|implements
name|Writeable
argument_list|<
name|Range
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|PROTOTYPE
specifier|public
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
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|field|KEY_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|KEY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"key"
argument_list|)
decl_stmt|;
DECL|field|FROM_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FROM_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"from"
argument_list|)
decl_stmt|;
DECL|field|TO_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|TO_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"to"
argument_list|)
decl_stmt|;
DECL|field|key
specifier|protected
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|from
specifier|protected
specifier|final
name|double
name|from
decl_stmt|;
DECL|field|fromAsStr
specifier|protected
specifier|final
name|String
name|fromAsStr
decl_stmt|;
DECL|field|to
specifier|protected
specifier|final
name|double
name|to
decl_stmt|;
DECL|field|toAsStr
specifier|protected
specifier|final
name|String
name|toAsStr
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
name|this
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|,
name|to
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|Range
specifier|public
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|this
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|from
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
DECL|method|Range
specifier|protected
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|Double
name|from
parameter_list|,
name|String
name|fromAsStr
parameter_list|,
name|Double
name|to
parameter_list|,
name|String
name|toAsStr
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
operator|==
literal|null
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|from
expr_stmt|;
name|this
operator|.
name|fromAsStr
operator|=
name|fromAsStr
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|to
operator|==
literal|null
condition|?
name|Double
operator|.
name|POSITIVE_INFINITY
else|:
name|to
expr_stmt|;
name|this
operator|.
name|toAsStr
operator|=
name|toAsStr
expr_stmt|;
block|}
DECL|method|matches
name|boolean
name|matches
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|value
operator|>=
name|from
operator|&&
name|value
operator|<
name|to
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|from
operator|+
literal|" to "
operator|+
name|to
operator|+
literal|")"
return|;
block|}
DECL|method|process
specifier|public
name|Range
name|process
parameter_list|(
name|DocValueFormat
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
assert|assert
name|parser
operator|!=
literal|null
assert|;
name|Double
name|from
init|=
name|this
operator|.
name|from
decl_stmt|;
name|Double
name|to
init|=
name|this
operator|.
name|to
decl_stmt|;
if|if
condition|(
name|fromAsStr
operator|!=
literal|null
condition|)
block|{
name|from
operator|=
name|parser
operator|.
name|parseDouble
argument_list|(
name|fromAsStr
argument_list|,
literal|false
argument_list|,
name|context
operator|.
name|nowCallable
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|toAsStr
operator|!=
literal|null
condition|)
block|{
name|to
operator|=
name|parser
operator|.
name|parseDouble
argument_list|(
name|toAsStr
argument_list|,
literal|false
argument_list|,
name|context
operator|.
name|nowCallable
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|fromAsStr
argument_list|,
name|to
argument_list|,
name|toAsStr
argument_list|)
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
name|String
name|fromAsStr
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|String
name|toAsStr
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
name|fromAsStr
argument_list|,
name|to
argument_list|,
name|toAsStr
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
name|writeOptionalString
argument_list|(
name|fromAsStr
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|toAsStr
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
DECL|method|fromXContent
specifier|public
name|Range
name|fromXContent
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
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|double
name|from
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
name|String
name|fromAsStr
init|=
literal|null
decl_stmt|;
name|double
name|to
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|String
name|toAsStr
init|=
literal|null
decl_stmt|;
name|String
name|key
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
name|currentFieldName
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
name|currentFieldName
argument_list|,
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
name|currentFieldName
argument_list|,
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
name|currentFieldName
argument_list|,
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
name|currentFieldName
argument_list|,
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
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
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
block|}
block|}
return|return
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|fromAsStr
argument_list|,
name|to
argument_list|,
name|toAsStr
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
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
argument_list|()
expr_stmt|;
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|KEY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Double
operator|.
name|isFinite
argument_list|(
name|from
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FROM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Double
operator|.
name|isFinite
argument_list|(
name|to
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|TO_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fromAsStr
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FROM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|fromAsStr
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|toAsStr
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|TO_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|toAsStr
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|fromAsStr
argument_list|,
name|to
argument_list|,
name|toAsStr
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Range
name|other
init|=
operator|(
name|Range
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|key
argument_list|,
name|other
operator|.
name|key
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|from
argument_list|,
name|other
operator|.
name|from
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|fromAsStr
argument_list|,
name|other
operator|.
name|fromAsStr
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|to
argument_list|,
name|other
operator|.
name|to
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|toAsStr
argument_list|,
name|other
operator|.
name|toAsStr
argument_list|)
return|;
block|}
block|}
DECL|field|valuesSource
specifier|final
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
decl_stmt|;
DECL|field|format
specifier|final
name|DocValueFormat
name|format
decl_stmt|;
DECL|field|ranges
specifier|final
name|Range
index|[]
name|ranges
decl_stmt|;
DECL|field|keyed
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|rangeFactory
specifier|final
name|InternalRange
operator|.
name|Factory
name|rangeFactory
decl_stmt|;
DECL|field|maxTo
specifier|final
name|double
index|[]
name|maxTo
decl_stmt|;
DECL|method|RangeAggregator
specifier|public
name|RangeAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|InternalRange
operator|.
name|Factory
name|rangeFactory
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Range
argument_list|>
name|ranges
parameter_list|,
name|boolean
name|keyed
parameter_list|,
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
name|super
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
assert|assert
name|valuesSource
operator|!=
literal|null
assert|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|rangeFactory
operator|=
name|rangeFactory
expr_stmt|;
name|this
operator|.
name|ranges
operator|=
operator|new
name|Range
index|[
name|ranges
operator|.
name|size
argument_list|()
index|]
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
name|this
operator|.
name|ranges
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|ranges
index|[
name|i
index|]
operator|=
name|ranges
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|process
argument_list|(
name|format
argument_list|,
name|context
operator|.
name|searchContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sortRanges
argument_list|(
name|this
operator|.
name|ranges
argument_list|)
expr_stmt|;
name|maxTo
operator|=
operator|new
name|double
index|[
name|this
operator|.
name|ranges
operator|.
name|length
index|]
expr_stmt|;
name|maxTo
index|[
literal|0
index|]
operator|=
name|this
operator|.
name|ranges
index|[
literal|0
index|]
operator|.
name|to
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|this
operator|.
name|ranges
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|maxTo
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|ranges
index|[
name|i
index|]
operator|.
name|to
argument_list|,
name|maxTo
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
operator|(
name|valuesSource
operator|!=
literal|null
operator|&&
name|valuesSource
operator|.
name|needsScores
argument_list|()
operator|)
operator|||
name|super
operator|.
name|needsScores
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|,
specifier|final
name|LeafBucketCollector
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|SortedNumericDoubleValues
name|values
init|=
name|valuesSource
operator|.
name|doubleValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
name|values
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|valuesCount
init|=
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|lo
init|=
literal|0
init|;
name|i
operator|<
name|valuesCount
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|double
name|value
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|lo
operator|=
name|collect
argument_list|(
name|doc
argument_list|,
name|value
argument_list|,
name|bucket
argument_list|,
name|lo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|double
name|value
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|,
name|int
name|lowBound
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|lo
init|=
name|lowBound
decl_stmt|,
name|hi
init|=
name|ranges
operator|.
name|length
operator|-
literal|1
decl_stmt|;
comment|// all candidates are between these indexes
name|int
name|mid
init|=
operator|(
name|lo
operator|+
name|hi
operator|)
operator|>>>
literal|1
decl_stmt|;
while|while
condition|(
name|lo
operator|<=
name|hi
condition|)
block|{
if|if
condition|(
name|value
operator|<
name|ranges
index|[
name|mid
index|]
operator|.
name|from
condition|)
block|{
name|hi
operator|=
name|mid
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|>=
name|maxTo
index|[
name|mid
index|]
condition|)
block|{
name|lo
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
name|mid
operator|=
operator|(
name|lo
operator|+
name|hi
operator|)
operator|>>>
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|lo
operator|>
name|hi
condition|)
return|return
name|lo
return|;
comment|// no potential candidate
comment|// binary search the lower bound
name|int
name|startLo
init|=
name|lo
decl_stmt|,
name|startHi
init|=
name|mid
decl_stmt|;
while|while
condition|(
name|startLo
operator|<=
name|startHi
condition|)
block|{
specifier|final
name|int
name|startMid
init|=
operator|(
name|startLo
operator|+
name|startHi
operator|)
operator|>>>
literal|1
decl_stmt|;
if|if
condition|(
name|value
operator|>=
name|maxTo
index|[
name|startMid
index|]
condition|)
block|{
name|startLo
operator|=
name|startMid
operator|+
literal|1
expr_stmt|;
block|}
else|else
block|{
name|startHi
operator|=
name|startMid
operator|-
literal|1
expr_stmt|;
block|}
block|}
comment|// binary search the upper bound
name|int
name|endLo
init|=
name|mid
decl_stmt|,
name|endHi
init|=
name|hi
decl_stmt|;
while|while
condition|(
name|endLo
operator|<=
name|endHi
condition|)
block|{
specifier|final
name|int
name|endMid
init|=
operator|(
name|endLo
operator|+
name|endHi
operator|)
operator|>>>
literal|1
decl_stmt|;
if|if
condition|(
name|value
operator|<
name|ranges
index|[
name|endMid
index|]
operator|.
name|from
condition|)
block|{
name|endHi
operator|=
name|endMid
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|endLo
operator|=
name|endMid
operator|+
literal|1
expr_stmt|;
block|}
block|}
assert|assert
name|startLo
operator|==
name|lowBound
operator|||
name|value
operator|>=
name|maxTo
index|[
name|startLo
operator|-
literal|1
index|]
assert|;
assert|assert
name|endHi
operator|==
name|ranges
operator|.
name|length
operator|-
literal|1
operator|||
name|value
operator|<
name|ranges
index|[
name|endHi
operator|+
literal|1
index|]
operator|.
name|from
assert|;
for|for
control|(
name|int
name|i
init|=
name|startLo
init|;
name|i
operator|<=
name|endHi
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|ranges
index|[
name|i
index|]
operator|.
name|matches
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|subBucketOrdinal
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|endHi
operator|+
literal|1
return|;
block|}
block|}
return|;
block|}
DECL|method|subBucketOrdinal
specifier|private
specifier|final
name|long
name|subBucketOrdinal
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|,
name|int
name|rangeOrd
parameter_list|)
block|{
return|return
name|owningBucketOrdinal
operator|*
name|ranges
operator|.
name|length
operator|+
name|rangeOrd
return|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
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
name|Range
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ranges
operator|.
name|length
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
name|ranges
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Range
name|range
init|=
name|ranges
index|[
name|i
index|]
decl_stmt|;
specifier|final
name|long
name|bucketOrd
init|=
name|subBucketOrdinal
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|i
argument_list|)
decl_stmt|;
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
name|Range
operator|.
name|Bucket
name|bucket
init|=
name|rangeFactory
operator|.
name|createBucket
argument_list|(
name|range
operator|.
name|key
argument_list|,
name|range
operator|.
name|from
argument_list|,
name|range
operator|.
name|to
argument_list|,
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|keyed
argument_list|,
name|format
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
comment|// value source can be null in the case of unmapped fields
return|return
name|rangeFactory
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|format
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
name|InternalAggregations
name|subAggs
init|=
name|buildEmptySubAggregations
argument_list|()
decl_stmt|;
name|List
argument_list|<
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
name|Range
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ranges
operator|.
name|length
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
name|ranges
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Range
name|range
init|=
name|ranges
index|[
name|i
index|]
decl_stmt|;
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
name|Range
operator|.
name|Bucket
name|bucket
init|=
name|rangeFactory
operator|.
name|createBucket
argument_list|(
name|range
operator|.
name|key
argument_list|,
name|range
operator|.
name|from
argument_list|,
name|range
operator|.
name|to
argument_list|,
literal|0
argument_list|,
name|subAggs
argument_list|,
name|keyed
argument_list|,
name|format
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
comment|// value source can be null in the case of unmapped fields
return|return
name|rangeFactory
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|format
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|sortRanges
specifier|private
specifier|static
specifier|final
name|void
name|sortRanges
parameter_list|(
specifier|final
name|Range
index|[]
name|ranges
parameter_list|)
block|{
operator|new
name|InPlaceMergeSorter
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|Range
name|tmp
init|=
name|ranges
index|[
name|i
index|]
decl_stmt|;
name|ranges
index|[
name|i
index|]
operator|=
name|ranges
index|[
name|j
index|]
expr_stmt|;
name|ranges
index|[
name|j
index|]
operator|=
name|tmp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
name|int
name|cmp
init|=
name|Double
operator|.
name|compare
argument_list|(
name|ranges
index|[
name|i
index|]
operator|.
name|from
argument_list|,
name|ranges
index|[
name|j
index|]
operator|.
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|cmp
operator|=
name|Double
operator|.
name|compare
argument_list|(
name|ranges
index|[
name|i
index|]
operator|.
name|to
argument_list|,
name|ranges
index|[
name|j
index|]
operator|.
name|to
argument_list|)
expr_stmt|;
block|}
return|return
name|cmp
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|ranges
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|class|Unmapped
specifier|public
specifier|static
class|class
name|Unmapped
parameter_list|<
name|R
extends|extends
name|RangeAggregator
operator|.
name|Range
parameter_list|>
extends|extends
name|NonCollectingAggregator
block|{
DECL|field|ranges
specifier|private
specifier|final
name|List
argument_list|<
name|R
argument_list|>
name|ranges
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|factory
specifier|private
specifier|final
name|InternalRange
operator|.
name|Factory
name|factory
decl_stmt|;
DECL|field|format
specifier|private
specifier|final
name|DocValueFormat
name|format
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|Unmapped
specifier|public
name|Unmapped
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|R
argument_list|>
name|ranges
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|InternalRange
operator|.
name|Factory
name|factory
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
name|super
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|ranges
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|R
name|range
range|:
name|ranges
control|)
block|{
name|this
operator|.
name|ranges
operator|.
name|add
argument_list|(
operator|(
name|R
operator|)
name|range
operator|.
name|process
argument_list|(
name|format
argument_list|,
name|context
operator|.
name|searchContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
name|this
operator|.
name|factory
operator|=
name|factory
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
name|InternalAggregations
name|subAggs
init|=
name|buildEmptySubAggregations
argument_list|()
decl_stmt|;
name|List
argument_list|<
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
name|Range
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ranges
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|RangeAggregator
operator|.
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|buckets
operator|.
name|add
argument_list|(
name|factory
operator|.
name|createBucket
argument_list|(
name|range
operator|.
name|key
argument_list|,
name|range
operator|.
name|from
argument_list|,
name|range
operator|.
name|to
argument_list|,
literal|0
argument_list|,
name|subAggs
argument_list|,
name|keyed
argument_list|,
name|format
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|format
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

