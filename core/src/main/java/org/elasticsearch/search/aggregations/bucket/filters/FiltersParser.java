begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.filters
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
name|filters
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
name|QueryBuilder
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
name|QueryBuilders
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
name|indices
operator|.
name|query
operator|.
name|IndicesQueriesRegistry
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FiltersParser
specifier|public
class|class
name|FiltersParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|FILTERS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FILTERS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"filters"
argument_list|)
decl_stmt|;
DECL|field|OTHER_BUCKET_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|OTHER_BUCKET_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"other_bucket"
argument_list|)
decl_stmt|;
DECL|field|OTHER_BUCKET_KEY_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|OTHER_BUCKET_KEY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"other_bucket_key"
argument_list|)
decl_stmt|;
DECL|field|queriesRegistry
specifier|private
specifier|final
name|IndicesQueriesRegistry
name|queriesRegistry
decl_stmt|;
annotation|@
name|Inject
DECL|method|FiltersParser
specifier|public
name|FiltersParser
parameter_list|(
name|IndicesQueriesRegistry
name|queriesRegistry
parameter_list|)
block|{
name|this
operator|.
name|queriesRegistry
operator|=
name|queriesRegistry
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
name|InternalFilters
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
name|FiltersAggregatorBuilder
name|parse
parameter_list|(
name|String
name|aggregationName
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
name|List
argument_list|<
name|FiltersAggregator
operator|.
name|KeyedFilter
argument_list|>
name|keyedFilters
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|QueryBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|nonKeyedFilters
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|String
name|otherBucketKey
init|=
literal|null
decl_stmt|;
name|Boolean
name|otherBucket
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
name|OTHER_BUCKET_FIELD
argument_list|)
condition|)
block|{
name|otherBucket
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
name|aggregationName
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
name|OTHER_BUCKET_KEY_FIELD
argument_list|)
condition|)
block|{
name|otherBucketKey
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
name|aggregationName
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
name|FILTERS_FIELD
argument_list|)
condition|)
block|{
name|keyedFilters
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|String
name|key
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
name|key
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|QueryParseContext
name|queryParseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|)
decl_stmt|;
name|queryParseContext
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|queryParseContext
operator|.
name|parseFieldMatcher
argument_list|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
init|=
name|queryParseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|keyedFilters
operator|.
name|add
argument_list|(
operator|new
name|FiltersAggregator
operator|.
name|KeyedFilter
argument_list|(
name|key
argument_list|,
name|filter
operator|==
literal|null
condition|?
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
else|:
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|aggregationName
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
name|FILTERS_FIELD
argument_list|)
condition|)
block|{
name|nonKeyedFilters
operator|=
operator|new
name|ArrayList
argument_list|<>
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
name|END_ARRAY
condition|)
block|{
name|QueryParseContext
name|queryParseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|)
decl_stmt|;
name|queryParseContext
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|queryParseContext
operator|.
name|parseFieldMatcher
argument_list|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
init|=
name|queryParseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|nonKeyedFilters
operator|.
name|add
argument_list|(
name|filter
operator|==
literal|null
condition|?
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
else|:
name|filter
argument_list|)
expr_stmt|;
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
name|aggregationName
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
name|aggregationName
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
if|if
condition|(
name|otherBucket
operator|&&
name|otherBucketKey
operator|==
literal|null
condition|)
block|{
name|otherBucketKey
operator|=
literal|"_other_"
expr_stmt|;
block|}
name|FiltersAggregatorBuilder
name|factory
decl_stmt|;
if|if
condition|(
name|keyedFilters
operator|!=
literal|null
condition|)
block|{
name|factory
operator|=
operator|new
name|FiltersAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|,
name|keyedFilters
operator|.
name|toArray
argument_list|(
operator|new
name|FiltersAggregator
operator|.
name|KeyedFilter
index|[
name|keyedFilters
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|factory
operator|=
operator|new
name|FiltersAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|,
name|nonKeyedFilters
operator|.
name|toArray
argument_list|(
operator|new
name|QueryBuilder
argument_list|<
name|?
argument_list|>
index|[
name|nonKeyedFilters
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|otherBucket
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|otherBucket
argument_list|(
name|otherBucket
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|otherBucketKey
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|otherBucketKey
argument_list|(
name|otherBucketKey
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|FiltersAggregatorBuilder
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
name|FiltersAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

