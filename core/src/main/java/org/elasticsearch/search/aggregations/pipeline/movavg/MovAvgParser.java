begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.movavg
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
name|movavg
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
name|inject
operator|.
name|Inject
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
name|movavg
operator|.
name|models
operator|.
name|MovAvgModel
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
name|movavg
operator|.
name|models
operator|.
name|MovAvgModelParserMapper
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
name|text
operator|.
name|ParseException
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

begin_class
DECL|class|MovAvgParser
specifier|public
class|class
name|MovAvgParser
implements|implements
name|PipelineAggregator
operator|.
name|Parser
block|{
DECL|field|MODEL
specifier|public
specifier|static
specifier|final
name|ParseField
name|MODEL
init|=
operator|new
name|ParseField
argument_list|(
literal|"model"
argument_list|)
decl_stmt|;
DECL|field|WINDOW
specifier|public
specifier|static
specifier|final
name|ParseField
name|WINDOW
init|=
operator|new
name|ParseField
argument_list|(
literal|"window"
argument_list|)
decl_stmt|;
DECL|field|SETTINGS
specifier|public
specifier|static
specifier|final
name|ParseField
name|SETTINGS
init|=
operator|new
name|ParseField
argument_list|(
literal|"settings"
argument_list|)
decl_stmt|;
DECL|field|PREDICT
specifier|public
specifier|static
specifier|final
name|ParseField
name|PREDICT
init|=
operator|new
name|ParseField
argument_list|(
literal|"predict"
argument_list|)
decl_stmt|;
DECL|field|MINIMIZE
specifier|public
specifier|static
specifier|final
name|ParseField
name|MINIMIZE
init|=
operator|new
name|ParseField
argument_list|(
literal|"minimize"
argument_list|)
decl_stmt|;
DECL|field|movAvgModelParserMapper
specifier|private
specifier|final
name|MovAvgModelParserMapper
name|movAvgModelParserMapper
decl_stmt|;
annotation|@
name|Inject
DECL|method|MovAvgParser
specifier|public
name|MovAvgParser
parameter_list|(
name|MovAvgModelParserMapper
name|movAvgModelParserMapper
parameter_list|)
block|{
name|this
operator|.
name|movAvgModelParserMapper
operator|=
name|movAvgModelParserMapper
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
name|MovAvgPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|MovAvgPipelineAggregator
operator|.
name|MovAvgPipelineAggregatorBuilder
name|parse
parameter_list|(
name|String
name|pipelineAggregatorName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
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
name|Integer
name|window
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
init|=
literal|null
decl_stmt|;
name|String
name|model
init|=
literal|null
decl_stmt|;
name|Integer
name|predict
init|=
literal|null
decl_stmt|;
name|Boolean
name|minimize
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
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|WINDOW
argument_list|)
condition|)
block|{
name|window
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|window
operator|<=
literal|0
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
literal|"["
operator|+
name|currentFieldName
operator|+
literal|"] value must be a positive, "
operator|+
literal|"non-zero integer.  Value supplied was ["
operator|+
name|predict
operator|+
literal|"] in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|PREDICT
argument_list|)
condition|)
block|{
name|predict
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|predict
operator|<=
literal|0
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
literal|"["
operator|+
name|currentFieldName
operator|+
literal|"] value must be a positive integer."
operator|+
literal|"  Value supplied was ["
operator|+
name|predict
operator|+
literal|"] in ["
operator|+
name|pipelineAggregatorName
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
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|FORMAT
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|BUCKETS_PATH
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|GAP_POLICY
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MODEL
argument_list|)
condition|)
block|{
name|model
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|BUCKETS_PATH
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
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SETTINGS
argument_list|)
condition|)
block|{
name|settings
operator|=
name|parser
operator|.
name|map
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
name|VALUE_BOOLEAN
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MINIMIZE
argument_list|)
condition|)
block|{
name|minimize
operator|=
name|parser
operator|.
name|booleanValue
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
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] for movingAvg aggregation ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|MovAvgPipelineAggregator
operator|.
name|MovAvgPipelineAggregatorBuilder
name|factory
init|=
operator|new
name|MovAvgPipelineAggregator
operator|.
name|MovAvgPipelineAggregatorBuilder
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
name|window
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|window
argument_list|(
name|window
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|predict
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|predict
argument_list|(
name|predict
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|model
operator|!=
literal|null
condition|)
block|{
name|MovAvgModel
operator|.
name|AbstractModelParser
name|modelParser
init|=
name|movAvgModelParserMapper
operator|.
name|get
argument_list|(
name|model
argument_list|)
decl_stmt|;
if|if
condition|(
name|modelParser
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
literal|"Unknown model ["
operator|+
name|model
operator|+
literal|"] specified.  Valid options are:"
operator|+
name|movAvgModelParserMapper
operator|.
name|getAllNames
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|MovAvgModel
name|movAvgModel
decl_stmt|;
try|try
block|{
name|movAvgModel
operator|=
name|modelParser
operator|.
name|parse
argument_list|(
name|settings
argument_list|,
name|pipelineAggregatorName
argument_list|,
name|window
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|exception
parameter_list|)
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
literal|"Could not parse settings for model ["
operator|+
name|model
operator|+
literal|"]."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
name|factory
operator|.
name|model
argument_list|(
name|movAvgModel
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minimize
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|minimize
argument_list|(
name|minimize
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototype
specifier|public
name|MovAvgPipelineAggregator
operator|.
name|MovAvgPipelineAggregatorBuilder
name|getFactoryPrototype
parameter_list|()
block|{
return|return
name|MovAvgPipelineAggregator
operator|.
name|MovAvgPipelineAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

