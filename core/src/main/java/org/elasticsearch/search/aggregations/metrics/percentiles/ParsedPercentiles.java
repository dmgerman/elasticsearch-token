begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles
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
name|percentiles
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
name|ParsedAggregation
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
DECL|class|ParsedPercentiles
specifier|public
specifier|abstract
class|class
name|ParsedPercentiles
extends|extends
name|ParsedAggregation
implements|implements
name|Iterable
argument_list|<
name|Percentile
argument_list|>
block|{
DECL|field|percentiles
specifier|protected
specifier|final
name|Map
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|percentiles
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|percentilesAsString
specifier|protected
specifier|final
name|Map
argument_list|<
name|Double
argument_list|,
name|String
argument_list|>
name|percentilesAsString
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|method|addPercentile
name|void
name|addPercentile
parameter_list|(
name|Double
name|key
parameter_list|,
name|Double
name|value
parameter_list|)
block|{
name|percentiles
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|addPercentileAsString
name|void
name|addPercentileAsString
parameter_list|(
name|Double
name|key
parameter_list|,
name|String
name|valueAsString
parameter_list|)
block|{
name|percentilesAsString
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|valueAsString
argument_list|)
expr_stmt|;
block|}
DECL|method|getPercentile
specifier|protected
name|Double
name|getPercentile
parameter_list|(
name|double
name|percent
parameter_list|)
block|{
if|if
condition|(
name|percentiles
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Double
operator|.
name|NaN
return|;
block|}
return|return
name|percentiles
operator|.
name|get
argument_list|(
name|percent
argument_list|)
return|;
block|}
DECL|method|getPercentileAsString
specifier|protected
name|String
name|getPercentileAsString
parameter_list|(
name|double
name|percent
parameter_list|)
block|{
name|String
name|valueAsString
init|=
name|percentilesAsString
operator|.
name|get
argument_list|(
name|percent
argument_list|)
decl_stmt|;
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
name|Double
name|value
init|=
name|getPercentile
argument_list|(
name|percent
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
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
return|return
literal|null
return|;
block|}
DECL|method|setKeyed
name|void
name|setKeyed
parameter_list|(
name|boolean
name|keyed
parameter_list|)
block|{
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Percentile
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Percentile
argument_list|>
argument_list|()
block|{
specifier|final
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
argument_list|>
name|iterator
init|=
name|percentiles
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Percentile
name|next
parameter_list|()
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
return|return
operator|new
name|Percentile
argument_list|(
name|next
operator|.
name|getKey
argument_list|()
argument_list|,
name|next
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
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
specifier|final
name|boolean
name|valuesAsString
init|=
operator|(
name|percentilesAsString
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|)
decl_stmt|;
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|CommonFields
operator|.
name|VALUES
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|percentile
range|:
name|percentiles
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Double
name|key
init|=
name|percentile
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|key
argument_list|)
argument_list|,
name|percentile
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|valuesAsString
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|key
operator|+
literal|"_as_string"
argument_list|,
name|getPercentileAsString
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|CommonFields
operator|.
name|VALUES
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|percentile
range|:
name|percentiles
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Double
name|key
init|=
name|percentile
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
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
name|percentile
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|valuesAsString
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
name|getPercentileAsString
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|declarePercentilesFields
specifier|protected
specifier|static
name|void
name|declarePercentilesFields
parameter_list|(
name|ObjectParser
argument_list|<
name|?
extends|extends
name|ParsedPercentiles
argument_list|,
name|Void
argument_list|>
name|objectParser
parameter_list|)
block|{
name|ParsedAggregation
operator|.
name|declareAggregationFields
argument_list|(
name|objectParser
argument_list|)
expr_stmt|;
name|objectParser
operator|.
name|declareField
argument_list|(
parameter_list|(
name|parser
parameter_list|,
name|aggregation
parameter_list|,
name|context
parameter_list|)
lambda|->
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
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
name|aggregation
operator|.
name|setKeyed
argument_list|(
literal|true
argument_list|)
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
operator|.
name|isValue
argument_list|()
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
name|VALUE_NUMBER
condition|)
block|{
name|aggregation
operator|.
name|addPercentile
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
argument_list|,
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
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
name|int
name|i
init|=
name|parser
operator|.
name|currentName
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"_as_string"
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|double
name|key
init|=
name|Double
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|addPercentileAsString
argument_list|(
name|key
argument_list|,
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|aggregation
operator|.
name|addPercentile
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|VALUE_NULL
condition|)
block|{
name|aggregation
operator|.
name|addPercentile
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
argument_list|,
name|Double
operator|.
name|NaN
argument_list|)
expr_stmt|;
block|}
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
name|aggregation
operator|.
name|setKeyed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
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
name|END_ARRAY
condition|)
block|{
name|Double
name|key
init|=
literal|null
decl_stmt|;
name|Double
name|value
init|=
literal|null
decl_stmt|;
name|String
name|valueAsString
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|CommonFields
operator|.
name|KEY
operator|.
name|getPreferredName
argument_list|()
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|key
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|CommonFields
operator|.
name|VALUE
operator|.
name|getPreferredName
argument_list|()
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|value
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|CommonFields
operator|.
name|VALUE_AS_STRING
operator|.
name|getPreferredName
argument_list|()
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|valueAsString
operator|=
name|parser
operator|.
name|text
argument_list|()
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
name|VALUE_NULL
condition|)
block|{
name|value
operator|=
name|Double
operator|.
name|NaN
expr_stmt|;
block|}
block|}
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|aggregation
operator|.
name|addPercentile
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueAsString
operator|!=
literal|null
condition|)
block|{
name|aggregation
operator|.
name|addPercentileAsString
argument_list|(
name|key
argument_list|,
name|valueAsString
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
argument_list|,
name|CommonFields
operator|.
name|VALUES
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT_ARRAY
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
