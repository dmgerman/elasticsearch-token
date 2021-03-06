begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics
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
name|bucketmetrics
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
name|HashMap
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
comment|/**  * A parser for parsing requests for a {@link BucketMetricsPipelineAggregator}  */
end_comment

begin_class
DECL|class|BucketMetricsParser
specifier|public
specifier|abstract
class|class
name|BucketMetricsParser
implements|implements
name|PipelineAggregator
operator|.
name|Parser
block|{
DECL|field|FORMAT
specifier|public
specifier|static
specifier|final
name|ParseField
name|FORMAT
init|=
operator|new
name|ParseField
argument_list|(
literal|"format"
argument_list|)
decl_stmt|;
DECL|method|BucketMetricsParser
specifier|public
name|BucketMetricsParser
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
specifier|final
name|BucketMetricsPipelineAggregationBuilder
argument_list|<
name|?
argument_list|>
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
name|GapPolicy
name|gapPolicy
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|5
argument_list|)
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
name|FORMAT
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|BUCKETS_PATH
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|GAP_POLICY
operator|.
name|match
argument_list|(
name|currentFieldName
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
else|else
block|{
name|parseToken
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|parser
argument_list|,
name|context
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|params
argument_list|)
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
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|BUCKETS_PATH
operator|.
name|match
argument_list|(
name|currentFieldName
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
name|parseToken
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|parser
argument_list|,
name|context
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|parseToken
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|parser
argument_list|,
name|context
argument_list|,
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|params
argument_list|)
expr_stmt|;
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
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] for aggregation ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|BucketMetricsPipelineAggregationBuilder
argument_list|<
name|?
argument_list|>
name|factory
init|=
name|buildFactory
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|bucketsPaths
index|[
literal|0
index|]
argument_list|,
name|params
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
assert|assert
operator|(
name|factory
operator|!=
literal|null
operator|)
assert|;
return|return
name|factory
return|;
block|}
DECL|method|buildFactory
specifier|protected
specifier|abstract
name|BucketMetricsPipelineAggregationBuilder
argument_list|<
name|?
argument_list|>
name|buildFactory
parameter_list|(
name|String
name|pipelineAggregatorName
parameter_list|,
name|String
name|bucketsPaths
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
function_decl|;
DECL|method|token
specifier|protected
name|boolean
name|token
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
parameter_list|,
name|String
name|field
parameter_list|,
name|XContentParser
operator|.
name|Token
name|token
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
DECL|method|parseToken
specifier|private
name|void
name|parseToken
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
parameter_list|,
name|String
name|currentFieldName
parameter_list|,
name|XContentParser
operator|.
name|Token
name|currentToken
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|token
argument_list|(
name|parser
argument_list|,
name|context
argument_list|,
name|currentFieldName
argument_list|,
name|currentToken
argument_list|,
name|params
argument_list|)
operator|==
literal|false
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
literal|"Unexpected token "
operator|+
name|currentToken
operator|+
literal|" ["
operator|+
name|currentFieldName
operator|+
literal|"] in ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

