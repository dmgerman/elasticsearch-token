begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.sampler
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
name|sampler
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
name|SearchParseException
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
name|support
operator|.
name|ValuesSourceParser
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SamplerParser
specifier|public
class|class
name|SamplerParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|DEFAULT_SHARD_SAMPLE_SIZE
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_SHARD_SAMPLE_SIZE
init|=
literal|100
decl_stmt|;
DECL|field|SHARD_SIZE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|SHARD_SIZE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"shard_size"
argument_list|)
decl_stmt|;
DECL|field|MAX_DOCS_PER_VALUE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|MAX_DOCS_PER_VALUE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"max_docs_per_value"
argument_list|)
decl_stmt|;
DECL|field|EXECUTION_HINT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|EXECUTION_HINT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"execution_hint"
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_USE_GLOBAL_ORDINALS
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_USE_GLOBAL_ORDINALS
init|=
literal|false
decl_stmt|;
DECL|field|MAX_DOCS_PER_VALUE_DEFAULT
specifier|public
specifier|static
specifier|final
name|int
name|MAX_DOCS_PER_VALUE_DEFAULT
init|=
literal|1
decl_stmt|;
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalSampler
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
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
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
name|executionHint
init|=
literal|null
decl_stmt|;
name|int
name|shardSize
init|=
name|DEFAULT_SHARD_SAMPLE_SIZE
decl_stmt|;
name|int
name|maxDocsPerValue
init|=
name|MAX_DOCS_PER_VALUE_DEFAULT
decl_stmt|;
name|ValuesSourceParser
name|vsParser
init|=
literal|null
decl_stmt|;
name|boolean
name|diversityChoiceMade
init|=
literal|false
decl_stmt|;
name|vsParser
operator|=
name|ValuesSourceParser
operator|.
name|any
argument_list|(
name|aggregationName
argument_list|,
name|InternalSampler
operator|.
name|TYPE
argument_list|,
name|context
argument_list|)
operator|.
name|scriptable
argument_list|(
literal|true
argument_list|)
operator|.
name|formattable
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
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
name|vsParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|)
condition|)
block|{
continue|continue;
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
name|SHARD_SIZE_FIELD
argument_list|)
condition|)
block|{
name|shardSize
operator|=
name|parser
operator|.
name|intValue
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
name|MAX_DOCS_PER_VALUE_FIELD
argument_list|)
condition|)
block|{
name|diversityChoiceMade
operator|=
literal|true
expr_stmt|;
name|maxDocsPerValue
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unsupported property \""
operator|+
name|currentFieldName
operator|+
literal|"\" for aggregation \""
operator|+
name|aggregationName
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|vsParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|)
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
name|EXECUTION_HINT_FIELD
argument_list|)
condition|)
block|{
name|executionHint
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
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unsupported property \""
operator|+
name|currentFieldName
operator|+
literal|"\" for aggregation \""
operator|+
name|aggregationName
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|ValuesSourceParser
operator|.
name|Input
name|vsInput
init|=
name|vsParser
operator|.
name|input
argument_list|()
decl_stmt|;
if|if
condition|(
name|vsInput
operator|.
name|valid
argument_list|()
condition|)
block|{
return|return
operator|new
name|SamplerAggregator
operator|.
name|DiversifiedFactory
argument_list|(
name|aggregationName
argument_list|,
name|shardSize
argument_list|,
name|executionHint
argument_list|,
name|vsInput
argument_list|,
name|maxDocsPerValue
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|diversityChoiceMade
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Sampler aggregation has "
operator|+
name|MAX_DOCS_PER_VALUE_FIELD
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" setting but no \"field\" or \"script\" setting to provide values for aggregation \""
operator|+
name|aggregationName
operator|+
literal|"\""
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|SamplerAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|shardSize
argument_list|)
return|;
block|}
block|}
comment|// NORELEASE implement this method when refactoring this aggregation
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|AggregatorFactory
index|[]
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

