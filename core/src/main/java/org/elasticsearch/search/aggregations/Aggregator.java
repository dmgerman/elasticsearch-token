begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|lease
operator|.
name|Releasable
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
name|bucket
operator|.
name|BucketsAggregator
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
name|AggregationContext
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
comment|/**  * An Aggregator.  */
end_comment

begin_comment
comment|// IMPORTANT: DO NOT add methods to this class unless strictly required.
end_comment

begin_comment
comment|// On the other hand, if you can remove methods from it, you are highly welcome!
end_comment

begin_class
DECL|class|Aggregator
specifier|public
specifier|abstract
class|class
name|Aggregator
extends|extends
name|BucketCollector
implements|implements
name|Releasable
block|{
comment|/**      * Parses the aggregation request and creates the appropriate aggregator factory for it.      *      * @see AggregatorBuilder      */
annotation|@
name|FunctionalInterface
DECL|interface|Parser
specifier|public
interface|interface
name|Parser
block|{
comment|/**          * @return The aggregation type this parser is associated with.          */
DECL|method|type
specifier|default
name|String
name|type
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
comment|// NORELEASE remove before 5.0.0GA
block|}
comment|/**          * Returns the aggregator factory with which this parser is associated, may return {@code null} indicating the          * aggregation should be skipped (e.g. when trying to aggregate on unmapped fields).          *          * @param aggregationName   The name of the aggregation          * @param parser            The xcontent parser          * @param context           The search context          * @return                  The resolved aggregator factory or {@code null} in case the aggregation should be skipped          * @throws java.io.IOException      When parsing fails          */
DECL|method|parse
name|AggregatorBuilder
argument_list|<
name|?
argument_list|>
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
function_decl|;
comment|/**          * @return an empty {@link AggregatorBuilder} instance for this parser          *         that can be used for deserialization          */
DECL|method|getFactoryPrototypes
specifier|default
name|AggregatorBuilder
argument_list|<
name|?
argument_list|>
name|getFactoryPrototypes
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
comment|// NORELEASE remove before 5.0.0GA
block|}
block|}
comment|/**      * Returns whether one of the parents is a {@link BucketsAggregator}.      */
DECL|method|descendsFromBucketAggregator
specifier|public
specifier|static
name|boolean
name|descendsFromBucketAggregator
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
while|while
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parent
operator|instanceof
name|BucketsAggregator
condition|)
block|{
return|return
literal|true
return|;
block|}
name|parent
operator|=
name|parent
operator|.
name|parent
argument_list|()
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Return the name of this aggregator.      */
DECL|method|name
specifier|public
specifier|abstract
name|String
name|name
parameter_list|()
function_decl|;
comment|/**      * Return the {@link AggregationContext} attached with this {@link Aggregator}.      */
DECL|method|context
specifier|public
specifier|abstract
name|AggregationContext
name|context
parameter_list|()
function_decl|;
comment|/**      * Return the parent aggregator.      */
DECL|method|parent
specifier|public
specifier|abstract
name|Aggregator
name|parent
parameter_list|()
function_decl|;
comment|/**      * Return the sub aggregator with the provided name.      */
DECL|method|subAggregator
specifier|public
specifier|abstract
name|Aggregator
name|subAggregator
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
comment|/**      * Build an aggregation for data that has been collected into {@code bucket}.      */
DECL|method|buildAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Build an empty aggregation.      */
DECL|method|buildEmptyAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
function_decl|;
comment|/** Aggregation mode for sub aggregations. */
DECL|enum|SubAggCollectionMode
specifier|public
enum|enum
name|SubAggCollectionMode
implements|implements
name|Writeable
argument_list|<
name|SubAggCollectionMode
argument_list|>
block|{
comment|/**          * Creates buckets and delegates to child aggregators in a single pass over          * the matching documents          */
DECL|enum constant|DEPTH_FIRST
name|DEPTH_FIRST
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"depth_first"
argument_list|)
argument_list|)
block|,
comment|/**          * Creates buckets for all matching docs and then prunes to top-scoring buckets          * before a second pass over the data when child aggregators are called          * but only for docs from the top-scoring buckets          */
DECL|enum constant|BREADTH_FIRST
name|BREADTH_FIRST
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"breadth_first"
argument_list|)
argument_list|)
block|;
DECL|field|KEY
specifier|public
specifier|static
specifier|final
name|ParseField
name|KEY
init|=
operator|new
name|ParseField
argument_list|(
literal|"collect_mode"
argument_list|)
decl_stmt|;
DECL|field|parseField
specifier|private
specifier|final
name|ParseField
name|parseField
decl_stmt|;
DECL|method|SubAggCollectionMode
name|SubAggCollectionMode
parameter_list|(
name|ParseField
name|parseField
parameter_list|)
block|{
name|this
operator|.
name|parseField
operator|=
name|parseField
expr_stmt|;
block|}
DECL|method|parseField
specifier|public
name|ParseField
name|parseField
parameter_list|()
block|{
return|return
name|parseField
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|SubAggCollectionMode
name|parse
parameter_list|(
name|String
name|value
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
name|SubAggCollectionMode
index|[]
name|modes
init|=
name|SubAggCollectionMode
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|SubAggCollectionMode
name|mode
range|:
name|modes
control|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|value
argument_list|,
name|mode
operator|.
name|parseField
argument_list|)
condition|)
block|{
return|return
name|mode
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"no [{}] found for value [{}]"
argument_list|,
name|KEY
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|value
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|SubAggCollectionMode
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|ordinal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|ordinal
operator|<
literal|0
operator|||
name|ordinal
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown SubAggCollectionMode ordinal ["
operator|+
name|ordinal
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|values
argument_list|()
index|[
name|ordinal
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

