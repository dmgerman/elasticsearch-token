begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.derivative
package|package
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
name|derivative
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
name|rounding
operator|.
name|DateTimeUnit
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
name|TimeValue
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
name|query
operator|.
name|QueryParseContext
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
name|PipelineAggregationBuilder
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
name|histogram
operator|.
name|DateHistogramAggregatorFactory
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
name|histogram
operator|.
name|DateHistogramInterval
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
name|histogram
operator|.
name|HistogramAggregatorFactory
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
name|AbstractPipelineAggregationBuilder
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
name|BucketHelpers
operator|.
name|GapPolicy
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
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
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

begin_class
DECL|class|DerivativePipelineAggregationBuilder
specifier|public
class|class
name|DerivativePipelineAggregationBuilder
extends|extends
name|AbstractPipelineAggregationBuilder
argument_list|<
name|DerivativePipelineAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"derivative"
decl_stmt|;
DECL|field|FORMAT_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|FORMAT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"format"
argument_list|)
decl_stmt|;
DECL|field|GAP_POLICY_FIELD
specifier|private
specifier|static
specifier|final
name|ParseField
name|GAP_POLICY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"gap_policy"
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
DECL|field|format
specifier|private
name|String
name|format
decl_stmt|;
DECL|field|gapPolicy
specifier|private
name|GapPolicy
name|gapPolicy
init|=
name|GapPolicy
operator|.
name|SKIP
decl_stmt|;
DECL|field|units
specifier|private
name|String
name|units
decl_stmt|;
DECL|method|DerivativePipelineAggregationBuilder
specifier|public
name|DerivativePipelineAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
operator|new
name|String
index|[]
block|{
name|bucketsPath
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|DerivativePipelineAggregationBuilder
specifier|private
name|DerivativePipelineAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|NAME
argument_list|,
name|bucketsPaths
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|DerivativePipelineAggregationBuilder
specifier|public
name|DerivativePipelineAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|NAME
argument_list|)
expr_stmt|;
name|format
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|units
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
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
name|format
argument_list|)
expr_stmt|;
name|boolean
name|hasGapPolicy
init|=
name|gapPolicy
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasGapPolicy
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasGapPolicy
condition|)
block|{
name|gapPolicy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|units
argument_list|)
expr_stmt|;
block|}
DECL|method|format
specifier|public
name|DerivativePipelineAggregationBuilder
name|format
parameter_list|(
name|String
name|format
parameter_list|)
block|{
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[format] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|format
specifier|public
name|String
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
DECL|method|gapPolicy
specifier|public
name|DerivativePipelineAggregationBuilder
name|gapPolicy
parameter_list|(
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
if|if
condition|(
name|gapPolicy
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[gapPolicy] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|gapPolicy
operator|=
name|gapPolicy
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|gapPolicy
specifier|public
name|GapPolicy
name|gapPolicy
parameter_list|()
block|{
return|return
name|gapPolicy
return|;
block|}
DECL|method|unit
specifier|public
name|DerivativePipelineAggregationBuilder
name|unit
parameter_list|(
name|String
name|units
parameter_list|)
block|{
if|if
condition|(
name|units
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[units] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|units
operator|=
name|units
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|unit
specifier|public
name|DerivativePipelineAggregationBuilder
name|unit
parameter_list|(
name|DateHistogramInterval
name|units
parameter_list|)
block|{
if|if
condition|(
name|units
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[units] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|units
operator|=
name|units
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|unit
specifier|public
name|String
name|unit
parameter_list|()
block|{
return|return
name|units
return|;
block|}
annotation|@
name|Override
DECL|method|createInternal
specifier|protected
name|PipelineAggregator
name|createInternal
parameter_list|(
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
name|DocValueFormat
name|formatter
decl_stmt|;
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|formatter
operator|=
operator|new
name|DocValueFormat
operator|.
name|Decimal
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|formatter
operator|=
name|DocValueFormat
operator|.
name|RAW
expr_stmt|;
block|}
name|Long
name|xAxisUnits
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|units
operator|!=
literal|null
condition|)
block|{
name|DateTimeUnit
name|dateTimeUnit
init|=
name|DateHistogramAggregatorFactory
operator|.
name|DATE_FIELD_UNITS
operator|.
name|get
argument_list|(
name|units
argument_list|)
decl_stmt|;
if|if
condition|(
name|dateTimeUnit
operator|!=
literal|null
condition|)
block|{
name|xAxisUnits
operator|=
name|dateTimeUnit
operator|.
name|field
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getDurationField
argument_list|()
operator|.
name|getUnitMillis
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|TimeValue
name|timeValue
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|units
argument_list|,
literal|null
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".unit"
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeValue
operator|!=
literal|null
condition|)
block|{
name|xAxisUnits
operator|=
name|timeValue
operator|.
name|getMillis
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|DerivativePipelineAggregator
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|formatter
argument_list|,
name|gapPolicy
argument_list|,
name|xAxisUnits
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|(
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
index|[]
name|aggFactories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregationBuilder
argument_list|>
name|pipelineAggregatoractories
parameter_list|)
block|{
if|if
condition|(
name|bucketsPaths
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must contain a single entry for aggregation ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|parent
operator|instanceof
name|HistogramAggregatorFactory
condition|)
block|{
name|HistogramAggregatorFactory
name|histoParent
init|=
operator|(
name|HistogramAggregatorFactory
operator|)
name|parent
decl_stmt|;
if|if
condition|(
name|histoParent
operator|.
name|minDocCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"parent histogram of derivative aggregation ["
operator|+
name|name
operator|+
literal|"] must have min_doc_count of 0"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parent
operator|instanceof
name|DateHistogramAggregatorFactory
condition|)
block|{
name|DateHistogramAggregatorFactory
name|histoParent
init|=
operator|(
name|DateHistogramAggregatorFactory
operator|)
name|parent
decl_stmt|;
if|if
condition|(
name|histoParent
operator|.
name|minDocCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"parent histogram of derivative aggregation ["
operator|+
name|name
operator|+
literal|"] must have min_doc_count of 0"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"derivative aggregation ["
operator|+
name|name
operator|+
literal|"] must have a histogram or date_histogram as parent"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FORMAT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gapPolicy
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|GAP_POLICY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|gapPolicy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|units
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|UNIT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|units
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|DerivativePipelineAggregationBuilder
name|parse
parameter_list|(
name|String
name|pipelineAggregatorName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
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
name|String
index|[]
name|bucketsPaths
init|=
literal|null
decl_stmt|;
name|String
name|format
init|=
literal|null
decl_stmt|;
name|String
name|units
init|=
literal|null
decl_stmt|;
name|GapPolicy
name|gapPolicy
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
if|if
condition|(
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|FORMAT_FIELD
argument_list|)
condition|)
block|{
name|format
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
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|BUCKETS_PATH_FIELD
argument_list|)
condition|)
block|{
name|bucketsPaths
operator|=
operator|new
name|String
index|[]
block|{
name|parser
operator|.
name|text
argument_list|()
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|GAP_POLICY_FIELD
argument_list|)
condition|)
block|{
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|parse
argument_list|(
name|context
argument_list|,
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|UNIT_FIELD
argument_list|)
condition|)
block|{
name|units
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
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
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|BUCKETS_PATH_FIELD
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|paths
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
name|path
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|bucketsPaths
operator|=
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
else|else
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
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
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|bucketsPaths
operator|==
literal|null
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
literal|"Missing required field ["
operator|+
name|BUCKETS_PATH_FIELD
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] for derivative aggregation ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|DerivativePipelineAggregationBuilder
name|factory
init|=
operator|new
name|DerivativePipelineAggregationBuilder
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|bucketsPaths
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|format
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gapPolicy
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|gapPolicy
argument_list|(
name|gapPolicy
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|units
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|unit
argument_list|(
name|units
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|DerivativePipelineAggregationBuilder
name|other
init|=
operator|(
name|DerivativePipelineAggregationBuilder
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|format
argument_list|,
name|other
operator|.
name|format
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|gapPolicy
argument_list|,
name|other
operator|.
name|gapPolicy
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Objects
operator|.
name|equals
argument_list|(
name|units
argument_list|,
name|other
operator|.
name|units
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|format
argument_list|,
name|gapPolicy
argument_list|,
name|units
argument_list|)
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
name|NAME
return|;
block|}
block|}
end_class

end_unit
