begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.avg
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
name|avg
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
name|ParsedSingleValueNumericMetricsAggregation
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
DECL|class|ParsedAvg
specifier|public
class|class
name|ParsedAvg
extends|extends
name|ParsedSingleValueNumericMetricsAggregation
implements|implements
name|Avg
block|{
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|double
name|getValue
parameter_list|()
block|{
return|return
name|value
argument_list|()
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
name|AvgAggregationBuilder
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
comment|// InternalAvg renders value only if the avg normalizer (count) is not 0.
comment|// We parse back `null` as Double.POSITIVE_INFINITY so we check for that value here to get the same xContent output
name|boolean
name|hasValue
init|=
name|value
operator|!=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|VALUE
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|hasValue
condition|?
name|value
else|:
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasValue
operator|&&
name|valueAsString
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|VALUE_AS_STRING
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|valueAsString
argument_list|)
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
name|ParsedAvg
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedAvg
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedAvg
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
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedAvg
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
name|ParsedAvg
name|avg
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
name|avg
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|avg
return|;
block|}
block|}
end_class

end_unit

