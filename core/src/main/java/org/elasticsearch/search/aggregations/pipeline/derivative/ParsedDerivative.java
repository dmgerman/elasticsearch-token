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
name|pipeline
operator|.
name|ParsedSimpleValue
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

begin_class
DECL|class|ParsedDerivative
specifier|public
class|class
name|ParsedDerivative
extends|extends
name|ParsedSimpleValue
implements|implements
name|Derivative
block|{
DECL|field|normalizedValue
specifier|private
name|double
name|normalizedValue
decl_stmt|;
DECL|field|normalizedAsString
specifier|private
name|String
name|normalizedAsString
decl_stmt|;
DECL|field|hasNormalizationFactor
specifier|private
name|boolean
name|hasNormalizationFactor
decl_stmt|;
DECL|field|NORMALIZED_AS_STRING
specifier|private
specifier|static
specifier|final
name|ParseField
name|NORMALIZED_AS_STRING
init|=
operator|new
name|ParseField
argument_list|(
literal|"normalized_value_as_string"
argument_list|)
decl_stmt|;
DECL|field|NORMALIZED
specifier|private
specifier|static
specifier|final
name|ParseField
name|NORMALIZED
init|=
operator|new
name|ParseField
argument_list|(
literal|"normalized_value"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|normalizedValue
specifier|public
name|double
name|normalizedValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|normalizedValue
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|DerivativePipelineAggregationBuilder
operator|.
name|NAME
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|ParsedDerivative
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedDerivative
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedDerivative
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareSingleValueFields
argument_list|(
name|PARSER
argument_list|,
name|Double
operator|.
name|NaN
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|agg
parameter_list|,
name|normalized
parameter_list|)
lambda|->
block|{
name|agg
operator|.
name|normalizedValue
operator|=
name|normalized
expr_stmt|;
name|agg
operator|.
name|hasNormalizationFactor
operator|=
literal|true
expr_stmt|;
block|}
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
name|NaN
argument_list|)
argument_list|,
name|NORMALIZED
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
name|normalAsString
parameter_list|)
lambda|->
name|agg
operator|.
name|normalizedAsString
operator|=
name|normalAsString
argument_list|,
name|NORMALIZED_AS_STRING
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedDerivative
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
name|ParsedDerivative
name|derivative
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
name|derivative
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|derivative
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
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasNormalizationFactor
condition|)
block|{
name|boolean
name|hasValue
init|=
name|Double
operator|.
name|isNaN
argument_list|(
name|normalizedValue
argument_list|)
operator|==
literal|false
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|NORMALIZED
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|hasValue
condition|?
name|normalizedValue
else|:
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasValue
operator|&&
name|normalizedAsString
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|NORMALIZED_AS_STRING
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|normalizedAsString
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

