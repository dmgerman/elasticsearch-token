begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.date
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
name|range
operator|.
name|date
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
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|range
operator|.
name|RangeAggregator
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
name|range
operator|.
name|RangeAggregator
operator|.
name|AbstractFactory
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
name|range
operator|.
name|RangeAggregator
operator|.
name|Range
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
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
DECL|class|DateRangeAggregatorFactory
specifier|public
class|class
name|DateRangeAggregatorFactory
extends|extends
name|AbstractFactory
argument_list|<
name|DateRangeAggregatorFactory
argument_list|,
name|RangeAggregator
operator|.
name|Range
argument_list|>
block|{
DECL|method|DateRangeAggregatorFactory
specifier|public
name|DateRangeAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalDateRange
operator|.
name|FACTORY
argument_list|)
expr_stmt|;
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
name|InternalDateRange
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the dates, inclusive      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but the key will be      * automatically generated based on<code>from</code> and<code>to</code>.      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no lower bound.      *      * @param key      *            the key to use for this range in the response      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, String)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|String
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no upper bound.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the distances, inclusive      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, String)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|String
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the dates, inclusive      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addRange(String, double, double)} but the key will be      * automatically generated based on<code>from</code> and<code>to</code>.      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no lower bound.      *      * @param key      *            the key to use for this range in the response      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, double)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|double
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no upper bound.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the distances, inclusive      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, double)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|double
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the dates, inclusive      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|DateTime
name|from
parameter_list|,
name|DateTime
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|convertDateTime
argument_list|(
name|from
argument_list|)
argument_list|,
name|convertDateTime
argument_list|(
name|to
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|convertDateTime
specifier|private
name|Double
name|convertDateTime
parameter_list|(
name|DateTime
name|dateTime
parameter_list|)
block|{
if|if
condition|(
name|dateTime
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
operator|(
name|double
operator|)
name|dateTime
operator|.
name|getMillis
argument_list|()
return|;
block|}
block|}
comment|/**      * Same as {@link #addRange(String, DateTime, DateTime)} but the key will be      * automatically generated based on<code>from</code> and<code>to</code>.      */
DECL|method|addRange
specifier|public
name|DateRangeAggregatorFactory
name|addRange
parameter_list|(
name|DateTime
name|from
parameter_list|,
name|DateTime
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no lower bound.      *      * @param key      *            the key to use for this range in the response      * @param to      *            the upper bound on the dates, exclusive      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|DateTime
name|to
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|convertDateTime
argument_list|(
name|to
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, DateTime)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedTo
parameter_list|(
name|DateTime
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no upper bound.      *      * @param key      *            the key to use for this range in the response      * @param from      *            the lower bound on the distances, inclusive      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|DateTime
name|from
parameter_list|)
block|{
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|convertDateTime
argument_list|(
name|from
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, DateTime)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|DateRangeAggregatorFactory
name|addUnboundedFrom
parameter_list|(
name|DateTime
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createFactoryFromStream
specifier|protected
name|DateRangeAggregatorFactory
name|createFactoryFromStream
parameter_list|(
name|String
name|name
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|DateRangeAggregatorFactory
name|factory
init|=
operator|new
name|DateRangeAggregatorFactory
argument_list|(
name|name
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|factory
operator|.
name|addRange
argument_list|(
name|Range
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

