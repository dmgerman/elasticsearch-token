begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.serialdiff
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
name|serialdiff
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
name|PipelineAggregatorBuilder
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

begin_import
import|import static
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
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
import|;
end_import

begin_import
import|import static
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
operator|.
name|Parser
operator|.
name|FORMAT
import|;
end_import

begin_class
DECL|class|SerialDiffPipelineAggregatorBuilder
specifier|public
class|class
name|SerialDiffPipelineAggregatorBuilder
extends|extends
name|PipelineAggregatorBuilder
argument_list|<
name|SerialDiffPipelineAggregatorBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|SerialDiffPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
decl_stmt|;
DECL|field|AGGREGATION_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|AGGREGATION_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
DECL|field|GAP_POLICY
specifier|private
specifier|static
specifier|final
name|ParseField
name|GAP_POLICY
init|=
operator|new
name|ParseField
argument_list|(
literal|"gap_policy"
argument_list|)
decl_stmt|;
DECL|field|LAG
specifier|private
specifier|static
specifier|final
name|ParseField
name|LAG
init|=
operator|new
name|ParseField
argument_list|(
literal|"lag"
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
DECL|field|lag
specifier|private
name|int
name|lag
init|=
literal|1
decl_stmt|;
DECL|method|SerialDiffPipelineAggregatorBuilder
specifier|public
name|SerialDiffPipelineAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|SerialDiffPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
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
comment|/**      * Read from a stream.      */
DECL|method|SerialDiffPipelineAggregatorBuilder
specifier|public
name|SerialDiffPipelineAggregatorBuilder
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
name|SerialDiffPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|format
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|lag
operator|=
name|in
operator|.
name|readVInt
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
name|gapPolicy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|lag
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the lag to use when calculating the serial difference.      */
DECL|method|lag
specifier|public
name|SerialDiffPipelineAggregatorBuilder
name|lag
parameter_list|(
name|int
name|lag
parameter_list|)
block|{
if|if
condition|(
name|lag
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[lag] must be a positive integer: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|lag
operator|=
name|lag
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the lag to use when calculating the serial difference.      */
DECL|method|lag
specifier|public
name|int
name|lag
parameter_list|()
block|{
return|return
name|lag
return|;
block|}
comment|/**      * Sets the format to use on the output of this aggregation.      */
DECL|method|format
specifier|public
name|SerialDiffPipelineAggregatorBuilder
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
comment|/**      * Gets the format to use on the output of this aggregation.      */
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
comment|/**      * Sets the GapPolicy to use on the output of this aggregation.      */
DECL|method|gapPolicy
specifier|public
name|SerialDiffPipelineAggregatorBuilder
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
comment|/**      * Gets the GapPolicy to use on the output of this aggregation.      */
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
DECL|method|formatter
specifier|protected
name|DocValueFormat
name|formatter
parameter_list|()
block|{
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|DocValueFormat
operator|.
name|Decimal
argument_list|(
name|format
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|DocValueFormat
operator|.
name|RAW
return|;
block|}
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
return|return
operator|new
name|SerialDiffPipelineAggregator
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|formatter
argument_list|()
argument_list|,
name|gapPolicy
argument_list|,
name|lag
argument_list|,
name|metaData
argument_list|)
return|;
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
name|FORMAT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|GAP_POLICY
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
name|builder
operator|.
name|field
argument_list|(
name|LAG
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|lag
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|SerialDiffPipelineAggregatorBuilder
name|parse
parameter_list|(
name|String
name|reducerName
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
name|Integer
name|lag
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
name|getParseFieldMatcher
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
name|getParseFieldMatcher
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
name|reducerName
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
name|VALUE_NUMBER
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
name|LAG
argument_list|)
condition|)
block|{
name|lag
operator|=
name|parser
operator|.
name|intValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|lag
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
literal|"Lag must be a positive, non-zero integer.  Value supplied was"
operator|+
name|lag
operator|+
literal|" in ["
operator|+
name|reducerName
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
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|reducerName
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
name|reducerName
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
name|reducerName
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
literal|"] for derivative aggregation ["
operator|+
name|reducerName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|SerialDiffPipelineAggregatorBuilder
name|factory
init|=
operator|new
name|SerialDiffPipelineAggregatorBuilder
argument_list|(
name|reducerName
argument_list|,
name|bucketsPaths
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|lag
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|lag
argument_list|(
name|lag
argument_list|)
expr_stmt|;
block|}
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
return|return
name|factory
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
name|lag
argument_list|)
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
name|SerialDiffPipelineAggregatorBuilder
name|other
init|=
operator|(
name|SerialDiffPipelineAggregatorBuilder
operator|)
name|obj
decl_stmt|;
return|return
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
operator|&&
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
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|lag
argument_list|,
name|other
operator|.
name|lag
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

