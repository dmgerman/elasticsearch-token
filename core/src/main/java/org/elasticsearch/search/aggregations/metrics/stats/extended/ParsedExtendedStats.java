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
name|collect
operator|.
name|Tuple
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
name|ConstructingObjectParser
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
name|ObjectParser
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
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|stats
operator|.
name|ParsedStats
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
name|stats
operator|.
name|extended
operator|.
name|InternalExtendedStats
operator|.
name|Fields
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ConstructingObjectParser
operator|.
name|constructorArg
import|;
end_import

begin_class
DECL|class|ParsedExtendedStats
specifier|public
class|class
name|ParsedExtendedStats
extends|extends
name|ParsedStats
implements|implements
name|ExtendedStats
block|{
DECL|field|sumOfSquares
specifier|protected
name|double
name|sumOfSquares
decl_stmt|;
DECL|field|variance
specifier|protected
name|double
name|variance
decl_stmt|;
DECL|field|stdDeviation
specifier|protected
name|double
name|stdDeviation
decl_stmt|;
DECL|field|stdDeviationBoundUpper
specifier|protected
name|double
name|stdDeviationBoundUpper
decl_stmt|;
DECL|field|stdDeviationBoundLower
specifier|protected
name|double
name|stdDeviationBoundLower
decl_stmt|;
DECL|field|sum
specifier|protected
name|double
name|sum
decl_stmt|;
DECL|field|avg
specifier|protected
name|double
name|avg
decl_stmt|;
annotation|@
name|Override
DECL|method|getType
specifier|protected
name|String
name|getType
parameter_list|()
block|{
return|return
name|ExtendedStatsAggregationBuilder
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getSumOfSquares
specifier|public
name|double
name|getSumOfSquares
parameter_list|()
block|{
return|return
name|sumOfSquares
return|;
block|}
annotation|@
name|Override
DECL|method|getVariance
specifier|public
name|double
name|getVariance
parameter_list|()
block|{
return|return
name|variance
return|;
block|}
annotation|@
name|Override
DECL|method|getStdDeviation
specifier|public
name|double
name|getStdDeviation
parameter_list|()
block|{
return|return
name|stdDeviation
return|;
block|}
DECL|method|setStdDeviationBounds
specifier|private
name|void
name|setStdDeviationBounds
parameter_list|(
name|Tuple
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|bounds
parameter_list|)
block|{
name|this
operator|.
name|stdDeviationBoundLower
operator|=
name|bounds
operator|.
name|v1
argument_list|()
expr_stmt|;
name|this
operator|.
name|stdDeviationBoundUpper
operator|=
name|bounds
operator|.
name|v2
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getStdDeviationBound
specifier|public
name|double
name|getStdDeviationBound
parameter_list|(
name|Bounds
name|bound
parameter_list|)
block|{
return|return
operator|(
name|bound
operator|.
name|equals
argument_list|(
name|Bounds
operator|.
name|LOWER
argument_list|)
operator|)
condition|?
name|stdDeviationBoundLower
else|:
name|stdDeviationBoundUpper
return|;
block|}
annotation|@
name|Override
DECL|method|getStdDeviationAsString
specifier|public
name|String
name|getStdDeviationAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|stdDeviation
argument_list|)
argument_list|)
return|;
block|}
DECL|method|setStdDeviationBoundsAsString
specifier|private
name|void
name|setStdDeviationBoundsAsString
parameter_list|(
name|Tuple
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|boundsAsString
parameter_list|)
block|{
name|this
operator|.
name|valueAsString
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
operator|+
literal|"_lower"
argument_list|,
name|boundsAsString
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueAsString
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
operator|+
literal|"_upper"
argument_list|,
name|boundsAsString
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getStdDeviationBoundAsString
specifier|public
name|String
name|getStdDeviationBoundAsString
parameter_list|(
name|Bounds
name|bound
parameter_list|)
block|{
if|if
condition|(
name|bound
operator|.
name|equals
argument_list|(
name|Bounds
operator|.
name|LOWER
argument_list|)
condition|)
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
operator|+
literal|"_lower"
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|stdDeviationBoundLower
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
operator|+
literal|"_upper"
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|stdDeviationBoundUpper
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getSumOfSquaresAsString
specifier|public
name|String
name|getSumOfSquaresAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|sumOfSquares
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getVarianceAsString
specifier|public
name|String
name|getVarianceAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|VARIANCE_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|variance
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|otherStatsToXContent
specifier|protected
name|XContentBuilder
name|otherStatsToXContent
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
name|count
operator|!=
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS
argument_list|,
name|sumOfSquares
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|VARIANCE
argument_list|,
name|getVariance
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION
argument_list|,
name|getStdDeviation
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UPPER
argument_list|,
name|getStdDeviationBound
argument_list|(
name|Bounds
operator|.
name|UPPER
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|LOWER
argument_list|,
name|getStdDeviationBound
argument_list|(
name|Bounds
operator|.
name|LOWER
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|valueAsString
operator|.
name|containsKey
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS_AS_STRING
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS_AS_STRING
argument_list|,
name|getSumOfSquaresAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|VARIANCE_AS_STRING
argument_list|,
name|getVarianceAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_AS_STRING
argument_list|,
name|getStdDeviationAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UPPER
argument_list|,
name|getStdDeviationBoundAsString
argument_list|(
name|Bounds
operator|.
name|UPPER
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|LOWER
argument_list|,
name|getStdDeviationBoundAsString
argument_list|(
name|Bounds
operator|.
name|LOWER
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|VARIANCE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|UPPER
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|LOWER
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|ParsedExtendedStats
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedExtendedStats
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedExtendedStats
operator|::
operator|new
argument_list|)
decl_stmt|;
DECL|field|STD_BOUNDS_PARSER
specifier|private
specifier|static
specifier|final
name|ConstructingObjectParser
argument_list|<
name|Tuple
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
argument_list|,
name|Void
argument_list|>
name|STD_BOUNDS_PARSER
init|=
operator|new
name|ConstructingObjectParser
argument_list|<>
argument_list|(
name|ParsedExtendedStats
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"_STD_BOUNDS"
argument_list|,
literal|true
argument_list|,
name|args
lambda|->
operator|new
name|Tuple
argument_list|<>
argument_list|(
operator|(
name|Double
operator|)
name|args
index|[
literal|0
index|]
argument_list|,
operator|(
name|Double
operator|)
name|args
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
static|static
block|{
name|STD_BOUNDS_PARSER
operator|.
name|declareField
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parseDouble
argument_list|(
name|parser
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|LOWER
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
name|STD_BOUNDS_PARSER
operator|.
name|declareField
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parseDouble
argument_list|(
name|parser
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|UPPER
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
block|}
DECL|field|STD_BOUNDS_AS_STRING_PARSER
specifier|private
specifier|static
specifier|final
name|ConstructingObjectParser
argument_list|<
name|Tuple
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|,
name|Void
argument_list|>
name|STD_BOUNDS_AS_STRING_PARSER
init|=
operator|new
name|ConstructingObjectParser
argument_list|<>
argument_list|(
name|ParsedExtendedStats
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"_STD_BOUNDS_AS_STRING"
argument_list|,
literal|true
argument_list|,
name|args
lambda|->
operator|new
name|Tuple
argument_list|<>
argument_list|(
operator|(
name|String
operator|)
name|args
index|[
literal|0
index|]
argument_list|,
operator|(
name|String
operator|)
name|args
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
static|static
block|{
name|STD_BOUNDS_AS_STRING_PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|LOWER
argument_list|)
argument_list|)
expr_stmt|;
name|STD_BOUNDS_AS_STRING_PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|UPPER
argument_list|)
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|declareAggregationFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
name|declareStatsFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|sumOfSquares
operator|=
name|value
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parseDouble
argument_list|(
name|parser
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|variance
operator|=
name|value
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parseDouble
argument_list|(
name|parser
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|VARIANCE
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|stdDeviation
operator|=
name|value
argument_list|,
parameter_list|(
name|parser
parameter_list|,
name|context
parameter_list|)
lambda|->
name|parseDouble
argument_list|(
name|parser
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObject
argument_list|(
name|ParsedExtendedStats
operator|::
name|setStdDeviationBounds
argument_list|,
name|STD_BOUNDS_PARSER
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|valueAsString
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|SUM_OF_SQRS_AS_STRING
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|valueAsString
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|VARIANCE_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|VARIANCE_AS_STRING
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|valueAsString
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_AS_STRING
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObject
argument_list|(
name|ParsedExtendedStats
operator|::
name|setStdDeviationBoundsAsString
argument_list|,
name|STD_BOUNDS_AS_STRING_PARSER
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|STD_DEVIATION_BOUNDS_AS_STRING
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedExtendedStats
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
name|ParsedExtendedStats
name|parsedStats
init|=
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|parsedStats
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|parsedStats
return|;
block|}
block|}
end_class

end_unit

