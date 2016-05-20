begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.terms
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
name|terms
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
name|ParseFieldMatcher
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
operator|.
name|SubAggCollectionMode
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
name|bucket
operator|.
name|terms
operator|.
name|Terms
operator|.
name|Order
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
name|bucket
operator|.
name|terms
operator|.
name|TermsAggregator
operator|.
name|BucketCountThresholds
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
name|bucket
operator|.
name|terms
operator|.
name|support
operator|.
name|IncludeExclude
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
name|support
operator|.
name|ValuesSourceType
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
name|Collections
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
comment|/**  *  */
end_comment

begin_class
DECL|class|TermsParser
specifier|public
class|class
name|TermsParser
extends|extends
name|AbstractTermsParser
block|{
annotation|@
name|Override
DECL|method|doCreateFactory
specifier|protected
name|TermsAggregationBuilder
name|doCreateFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|BucketCountThresholds
name|bucketCountThresholds
parameter_list|,
name|SubAggCollectionMode
name|collectMode
parameter_list|,
name|String
name|executionHint
parameter_list|,
name|IncludeExclude
name|incExc
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
block|{
name|TermsAggregationBuilder
name|factory
init|=
operator|new
name|TermsAggregationBuilder
argument_list|(
name|aggregationName
argument_list|,
name|targetValueType
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|OrderElement
argument_list|>
name|orderElements
init|=
operator|(
name|List
argument_list|<
name|OrderElement
argument_list|>
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|TermsAggregationBuilder
operator|.
name|ORDER_FIELD
argument_list|)
decl_stmt|;
if|if
condition|(
name|orderElements
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Terms
operator|.
name|Order
argument_list|>
name|orders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|orderElements
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|OrderElement
name|orderElement
range|:
name|orderElements
control|)
block|{
name|orders
operator|.
name|add
argument_list|(
name|resolveOrder
argument_list|(
name|orderElement
operator|.
name|key
argument_list|()
argument_list|,
name|orderElement
operator|.
name|asc
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|order
argument_list|(
name|orders
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bucketCountThresholds
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|bucketCountThresholds
argument_list|(
name|bucketCountThresholds
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|collectMode
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|collectMode
argument_list|(
name|collectMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|executionHint
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|executionHint
argument_list|(
name|executionHint
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|incExc
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|includeExclude
argument_list|(
name|incExc
argument_list|)
expr_stmt|;
block|}
name|Boolean
name|showTermDocCountError
init|=
operator|(
name|Boolean
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|TermsAggregationBuilder
operator|.
name|SHOW_TERM_DOC_COUNT_ERROR
argument_list|)
decl_stmt|;
if|if
condition|(
name|showTermDocCountError
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|showTermDocCountError
argument_list|(
name|showTermDocCountError
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|parseSpecial
specifier|public
name|boolean
name|parseSpecial
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|Token
name|token
parameter_list|,
name|String
name|currentFieldName
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
throws|throws
name|IOException
block|{
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
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TermsAggregationBuilder
operator|.
name|ORDER_FIELD
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|TermsAggregationBuilder
operator|.
name|ORDER_FIELD
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|parseOrderParam
argument_list|(
name|aggregationName
argument_list|,
name|parser
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TermsAggregationBuilder
operator|.
name|ORDER_FIELD
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|OrderElement
argument_list|>
name|orderElements
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
name|OrderElement
name|orderParam
init|=
name|parseOrderParam
argument_list|(
name|aggregationName
argument_list|,
name|parser
argument_list|)
decl_stmt|;
name|orderElements
operator|.
name|add
argument_list|(
name|orderParam
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
literal|"Order elements must be of type object in ["
operator|+
name|aggregationName
operator|+
literal|"] found token of type ["
operator|+
name|token
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
name|otherOptions
operator|.
name|put
argument_list|(
name|TermsAggregationBuilder
operator|.
name|ORDER_FIELD
argument_list|,
name|orderElements
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|TermsAggregationBuilder
operator|.
name|SHOW_TERM_DOC_COUNT_ERROR
argument_list|)
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|TermsAggregationBuilder
operator|.
name|SHOW_TERM_DOC_COUNT_ERROR
argument_list|,
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|parseOrderParam
specifier|private
name|OrderElement
name|parseOrderParam
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|OrderElement
name|orderParam
init|=
literal|null
decl_stmt|;
name|String
name|orderKey
init|=
literal|null
decl_stmt|;
name|boolean
name|orderAsc
init|=
literal|false
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
name|orderKey
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
name|String
name|dir
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"asc"
operator|.
name|equalsIgnoreCase
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|orderAsc
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"desc"
operator|.
name|equalsIgnoreCase
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|orderAsc
operator|=
literal|false
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
literal|"Unknown terms order direction ["
operator|+
name|dir
operator|+
literal|"] in terms aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
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
literal|" for [order] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|orderKey
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
literal|"Must specify at least one field for [order] in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
else|else
block|{
name|orderParam
operator|=
operator|new
name|OrderElement
argument_list|(
name|orderKey
argument_list|,
name|orderAsc
argument_list|)
expr_stmt|;
block|}
return|return
name|orderParam
return|;
block|}
DECL|class|OrderElement
specifier|static
class|class
name|OrderElement
block|{
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|asc
specifier|private
specifier|final
name|boolean
name|asc
decl_stmt|;
DECL|method|OrderElement
specifier|public
name|OrderElement
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|asc
operator|=
name|asc
expr_stmt|;
block|}
DECL|method|key
specifier|public
name|String
name|key
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|asc
specifier|public
name|boolean
name|asc
parameter_list|()
block|{
return|return
name|asc
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getDefaultBucketCountThresholds
specifier|public
name|TermsAggregator
operator|.
name|BucketCountThresholds
name|getDefaultBucketCountThresholds
parameter_list|()
block|{
return|return
operator|new
name|TermsAggregator
operator|.
name|BucketCountThresholds
argument_list|(
name|TermsAggregationBuilder
operator|.
name|DEFAULT_BUCKET_COUNT_THRESHOLDS
argument_list|)
return|;
block|}
DECL|method|resolveOrder
specifier|static
name|Terms
operator|.
name|Order
name|resolveOrder
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
if|if
condition|(
literal|"_term"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|Order
operator|.
name|term
argument_list|(
name|asc
argument_list|)
return|;
block|}
if|if
condition|(
literal|"_count"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|Order
operator|.
name|count
argument_list|(
name|asc
argument_list|)
return|;
block|}
return|return
name|Order
operator|.
name|aggregation
argument_list|(
name|key
argument_list|,
name|asc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

