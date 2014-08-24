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
name|index
operator|.
name|query
operator|.
name|FilterBuilder
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
name|AggregationBuilder
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
name|builder
operator|.
name|SearchSourceBuilderException
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
name|LinkedHashMap
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
DECL|class|FiltersAggregationBuilder
specifier|public
class|class
name|FiltersAggregationBuilder
extends|extends
name|AggregationBuilder
argument_list|<
name|FiltersAggregationBuilder
argument_list|>
block|{
DECL|field|keyedFilters
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FilterBuilder
argument_list|>
name|keyedFilters
init|=
literal|null
decl_stmt|;
DECL|field|nonKeyedFilters
specifier|private
name|List
argument_list|<
name|FilterBuilder
argument_list|>
name|nonKeyedFilters
init|=
literal|null
decl_stmt|;
DECL|method|FiltersAggregationBuilder
specifier|public
name|FiltersAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalFilters
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|filter
specifier|public
name|FiltersAggregationBuilder
name|filter
parameter_list|(
name|String
name|key
parameter_list|,
name|FilterBuilder
name|filter
parameter_list|)
block|{
if|if
condition|(
name|keyedFilters
operator|==
literal|null
condition|)
block|{
name|keyedFilters
operator|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|keyedFilters
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|filter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filter
specifier|public
name|FiltersAggregationBuilder
name|filter
parameter_list|(
name|FilterBuilder
name|filter
parameter_list|)
block|{
if|if
condition|(
name|nonKeyedFilters
operator|==
literal|null
condition|)
block|{
name|nonKeyedFilters
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|nonKeyedFilters
operator|.
name|add
argument_list|(
name|filter
argument_list|)
expr_stmt|;
return|return
name|this
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
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|keyedFilters
operator|==
literal|null
operator|&&
name|nonKeyedFilters
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"At least one filter must be set on filter aggregation ["
operator|+
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|keyedFilters
operator|!=
literal|null
operator|&&
name|nonKeyedFilters
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"Cannot add both keyed and non-keyed filters to filters aggregation"
argument_list|)
throw|;
block|}
if|if
condition|(
name|keyedFilters
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"filters"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FilterBuilder
argument_list|>
name|entry
range|:
name|keyedFilters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|nonKeyedFilters
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"filters"
argument_list|)
expr_stmt|;
for|for
control|(
name|FilterBuilder
name|filterBuilder
range|:
name|nonKeyedFilters
control|)
block|{
name|filterBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
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
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

