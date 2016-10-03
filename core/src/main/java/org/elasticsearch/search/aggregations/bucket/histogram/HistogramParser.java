begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
name|histogram
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
name|ParseFieldMatcherSupplier
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
name|ParsingException
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
name|ObjectParser
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
name|XContentParseContext
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
comment|/**  * A parser for date histograms. This translates json into an  * {@link HistogramAggregationBuilder} instance.  */
end_comment

begin_class
DECL|class|HistogramParser
specifier|public
class|class
name|HistogramParser
extends|extends
name|NumericValuesSourceParser
block|{
DECL|field|EXTENDED_BOUNDS_PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|double
index|[]
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|EXTENDED_BOUNDS_PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|Histogram
operator|.
name|EXTENDED_BOUNDS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
parameter_list|()
lambda|->
operator|new
name|double
index|[]
block|{
name|Double
operator|.
name|POSITIVE_INFINITY
block|,
name|Double
operator|.
name|NEGATIVE_INFINITY
block|}
argument_list|)
decl_stmt|;
static|static
block|{
name|EXTENDED_BOUNDS_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|bounds
parameter_list|,
name|d
parameter_list|)
lambda|->
name|bounds
index|[
literal|0
index|]
operator|=
name|d
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"min"
argument_list|)
argument_list|)
expr_stmt|;
name|EXTENDED_BOUNDS_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|bounds
parameter_list|,
name|d
parameter_list|)
lambda|->
name|bounds
index|[
literal|1
index|]
operator|=
name|d
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|HistogramParser
specifier|public
name|HistogramParser
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
DECL|method|createFactory
specifier|protected
name|HistogramAggregationBuilder
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
name|HistogramAggregationBuilder
name|factory
init|=
operator|new
name|HistogramAggregationBuilder
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
name|Double
name|interval
init|=
operator|(
name|Double
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|Histogram
operator|.
name|INTERVAL_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|interval
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
literal|null
argument_list|,
literal|"Missing required field [interval] for histogram aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
name|factory
operator|.
name|interval
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
name|Double
name|offset
init|=
operator|(
name|Double
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|Histogram
operator|.
name|OFFSET_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|offset
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
name|double
index|[]
name|extendedBounds
init|=
operator|(
name|double
index|[]
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|Histogram
operator|.
name|EXTENDED_BOUNDS_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|extendedBounds
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|extendedBounds
argument_list|(
name|extendedBounds
index|[
literal|0
index|]
argument_list|,
name|extendedBounds
index|[
literal|1
index|]
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
name|Histogram
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
name|Long
name|minDocCount
init|=
operator|(
name|Long
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|Histogram
operator|.
name|MIN_DOC_COUNT_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|minDocCount
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|minDocCount
argument_list|(
name|minDocCount
argument_list|)
expr_stmt|;
block|}
name|InternalOrder
name|order
init|=
operator|(
name|InternalOrder
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|Histogram
operator|.
name|ORDER_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|order
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|order
argument_list|(
name|order
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
name|XContentParseContext
name|context
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
name|XContentParser
name|parser
init|=
name|context
operator|.
name|getParser
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|INTERVAL_FIELD
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|INTERVAL_FIELD
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
elseif|else
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|MIN_DOC_COUNT_FIELD
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|MIN_DOC_COUNT_FIELD
argument_list|,
name|parser
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|KEYED_FIELD
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|KEYED_FIELD
argument_list|,
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|OFFSET_FIELD
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|OFFSET_FIELD
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
else|else
block|{
return|return
literal|false
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
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|ORDER_FIELD
argument_list|)
condition|)
block|{
name|InternalOrder
name|order
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
name|VALUE_STRING
condition|)
block|{
name|String
name|dir
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|boolean
name|asc
init|=
literal|"asc"
operator|.
name|equals
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|asc
operator|&&
operator|!
literal|"desc"
operator|.
name|equals
argument_list|(
name|dir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown order direction in aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|dir
operator|+
literal|"]. Should be either [asc] or [desc]"
argument_list|)
throw|;
block|}
name|order
operator|=
name|resolveOrder
argument_list|(
name|currentFieldName
argument_list|,
name|asc
argument_list|)
expr_stmt|;
block|}
block|}
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|ORDER_FIELD
argument_list|,
name|order
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|Histogram
operator|.
name|EXTENDED_BOUNDS_FIELD
argument_list|)
condition|)
block|{
name|double
index|[]
name|bounds
init|=
name|EXTENDED_BOUNDS_PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
name|context
operator|::
name|getParseFieldMatcher
argument_list|)
decl_stmt|;
name|otherOptions
operator|.
name|put
argument_list|(
name|Histogram
operator|.
name|EXTENDED_BOUNDS_FIELD
argument_list|,
name|bounds
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|method|resolveOrder
specifier|static
name|InternalOrder
name|resolveOrder
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
if|if
condition|(
literal|"_key"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
call|(
name|InternalOrder
call|)
argument_list|(
name|asc
condition|?
name|InternalOrder
operator|.
name|KEY_ASC
else|:
name|InternalOrder
operator|.
name|KEY_DESC
argument_list|)
return|;
block|}
if|if
condition|(
literal|"_count"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
call|(
name|InternalOrder
call|)
argument_list|(
name|asc
condition|?
name|InternalOrder
operator|.
name|COUNT_ASC
else|:
name|InternalOrder
operator|.
name|COUNT_DESC
argument_list|)
return|;
block|}
return|return
operator|new
name|InternalOrder
operator|.
name|Aggregation
argument_list|(
name|key
argument_list|,
name|asc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

