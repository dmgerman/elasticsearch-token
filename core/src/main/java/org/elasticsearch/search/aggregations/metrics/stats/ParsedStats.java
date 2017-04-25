begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.stats
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
name|ParsedAggregation
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
name|InternalStats
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
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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

begin_class
DECL|class|ParsedStats
specifier|public
class|class
name|ParsedStats
extends|extends
name|ParsedAggregation
implements|implements
name|Stats
block|{
DECL|field|count
specifier|protected
name|long
name|count
decl_stmt|;
DECL|field|min
specifier|protected
name|double
name|min
decl_stmt|;
DECL|field|max
specifier|protected
name|double
name|max
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
DECL|field|valueAsString
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|valueAsString
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|getCount
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
annotation|@
name|Override
DECL|method|getMin
specifier|public
name|double
name|getMin
parameter_list|()
block|{
return|return
name|min
return|;
block|}
annotation|@
name|Override
DECL|method|getMax
specifier|public
name|double
name|getMax
parameter_list|()
block|{
return|return
name|max
return|;
block|}
annotation|@
name|Override
DECL|method|getAvg
specifier|public
name|double
name|getAvg
parameter_list|()
block|{
return|return
name|avg
return|;
block|}
annotation|@
name|Override
DECL|method|getSum
specifier|public
name|double
name|getSum
parameter_list|()
block|{
return|return
name|sum
return|;
block|}
annotation|@
name|Override
DECL|method|getMinAsString
specifier|public
name|String
name|getMinAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|MIN_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|min
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getMaxAsString
specifier|public
name|String
name|getMaxAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|MAX_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|max
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getAvgAsString
specifier|public
name|String
name|getAvgAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|AVG_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|avg
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getSumAsString
specifier|public
name|String
name|getSumAsString
parameter_list|()
block|{
return|return
name|valueAsString
operator|.
name|getOrDefault
argument_list|(
name|Fields
operator|.
name|SUM_AS_STRING
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|sum
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|protected
name|String
name|getType
parameter_list|()
block|{
return|return
name|StatsAggregationBuilder
operator|.
name|NAME
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
name|Fields
operator|.
name|COUNT
argument_list|,
name|count
argument_list|)
expr_stmt|;
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
name|MIN
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MAX
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|AVG
argument_list|,
name|avg
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUM
argument_list|,
name|sum
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueAsString
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|MIN_AS_STRING
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MIN_AS_STRING
argument_list|,
name|getMinAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MAX_AS_STRING
argument_list|,
name|getMaxAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|AVG_AS_STRING
argument_list|,
name|getAvgAsString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUM_AS_STRING
argument_list|,
name|getSumAsString
argument_list|()
argument_list|)
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
name|MIN
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|MAX
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|AVG
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nullField
argument_list|(
name|Fields
operator|.
name|SUM
argument_list|)
expr_stmt|;
block|}
name|otherStatsToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
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
name|ParsedStats
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedStats
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedStats
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareAggregationFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|value
parameter_list|)
lambda|->
name|agg
operator|.
name|count
operator|=
name|value
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|COUNT
argument_list|)
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
name|min
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
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|MIN
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
name|max
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
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|MAX
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
name|avg
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
name|AVG
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
name|sum
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
name|SUM
argument_list|)
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
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
name|MIN_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|MIN_AS_STRING
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
name|MAX_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|MAX_AS_STRING
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
name|AVG_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|AVG_AS_STRING
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
name|SUM_AS_STRING
argument_list|,
name|value
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|Fields
operator|.
name|SUM_AS_STRING
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedStats
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
name|ParsedStats
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
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

