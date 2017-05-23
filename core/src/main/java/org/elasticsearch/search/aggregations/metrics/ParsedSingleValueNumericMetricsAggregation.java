begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
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
name|search
operator|.
name|aggregations
operator|.
name|ParsedAggregation
import|;
end_import

begin_class
DECL|class|ParsedSingleValueNumericMetricsAggregation
specifier|public
specifier|abstract
class|class
name|ParsedSingleValueNumericMetricsAggregation
extends|extends
name|ParsedAggregation
implements|implements
name|NumericMetricsAggregation
operator|.
name|SingleValue
block|{
DECL|field|value
specifier|protected
name|double
name|value
decl_stmt|;
DECL|field|valueAsString
specifier|protected
name|String
name|valueAsString
decl_stmt|;
annotation|@
name|Override
DECL|method|getValueAsString
specifier|public
name|String
name|getValueAsString
parameter_list|()
block|{
if|if
condition|(
name|valueAsString
operator|!=
literal|null
condition|)
block|{
return|return
name|valueAsString
return|;
block|}
else|else
block|{
return|return
name|Double
operator|.
name|toString
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|double
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
DECL|method|setValue
specifier|protected
name|void
name|setValue
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|setValueAsString
specifier|protected
name|void
name|setValueAsString
parameter_list|(
name|String
name|valueAsString
parameter_list|)
block|{
name|this
operator|.
name|valueAsString
operator|=
name|valueAsString
expr_stmt|;
block|}
DECL|method|declareSingleValueFields
specifier|protected
specifier|static
name|void
name|declareSingleValueFields
parameter_list|(
name|ObjectParser
argument_list|<
name|?
extends|extends
name|ParsedSingleValueNumericMetricsAggregation
argument_list|,
name|Void
argument_list|>
name|objectParser
parameter_list|,
name|double
name|defaultNullValue
parameter_list|)
block|{
name|declareAggregationFields
argument_list|(
name|objectParser
argument_list|)
expr_stmt|;
name|objectParser
operator|.
name|declareField
argument_list|(
name|ParsedSingleValueNumericMetricsAggregation
operator|::
name|setValue
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
name|defaultNullValue
argument_list|)
argument_list|,
name|CommonFields
operator|.
name|VALUE
argument_list|,
name|ValueType
operator|.
name|DOUBLE_OR_NULL
argument_list|)
expr_stmt|;
name|objectParser
operator|.
name|declareString
argument_list|(
name|ParsedSingleValueNumericMetricsAggregation
operator|::
name|setValueAsString
argument_list|,
name|CommonFields
operator|.
name|VALUE_AS_STRING
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
