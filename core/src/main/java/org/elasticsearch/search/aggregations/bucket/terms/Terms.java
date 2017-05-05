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
name|xcontent
operator|.
name|ToXContent
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
name|MultiBucketsAggregation
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
comment|/**  * A {@code terms} aggregation. Defines multiple bucket, each associated with a unique term for a specific field.  * All documents in a bucket has the bucket's term in that field.  */
end_comment

begin_interface
DECL|interface|Terms
specifier|public
interface|interface
name|Terms
extends|extends
name|MultiBucketsAggregation
block|{
comment|/**      * A bucket that is associated with a single term      */
DECL|interface|Bucket
interface|interface
name|Bucket
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
block|{
DECL|method|getKeyAsNumber
name|Number
name|getKeyAsNumber
parameter_list|()
function_decl|;
DECL|method|compareTerm
name|int
name|compareTerm
parameter_list|(
name|Terms
operator|.
name|Bucket
name|other
parameter_list|)
function_decl|;
DECL|method|getDocCountError
name|long
name|getDocCountError
parameter_list|()
function_decl|;
block|}
comment|/**      * Return the sorted list of the buckets in this terms aggregation.      */
annotation|@
name|Override
DECL|method|getBuckets
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
function_decl|;
comment|/**      * Get the bucket for the given term, or null if there is no such bucket.      */
DECL|method|getBucketByKey
name|Bucket
name|getBucketByKey
parameter_list|(
name|String
name|term
parameter_list|)
function_decl|;
comment|/**      * Get an upper bound of the error on document counts in this aggregation.      */
DECL|method|getDocCountError
name|long
name|getDocCountError
parameter_list|()
function_decl|;
comment|/**      * Return the sum of the document counts of all buckets that did not make      * it to the top buckets.      */
DECL|method|getSumOfOtherDocCounts
name|long
name|getSumOfOtherDocCounts
parameter_list|()
function_decl|;
comment|/**      * Determines the order by which the term buckets will be sorted      */
DECL|class|Order
specifier|abstract
class|class
name|Order
implements|implements
name|ToXContent
block|{
comment|/**          * @return a bucket ordering strategy that sorts buckets by their document counts (ascending or descending)          */
DECL|method|count
specifier|public
specifier|static
name|Order
name|count
parameter_list|(
name|boolean
name|asc
parameter_list|)
block|{
return|return
name|asc
condition|?
name|InternalOrder
operator|.
name|COUNT_ASC
else|:
name|InternalOrder
operator|.
name|COUNT_DESC
return|;
block|}
comment|/**          * @return a bucket ordering strategy that sorts buckets by their terms (ascending or descending)          */
DECL|method|term
specifier|public
specifier|static
name|Order
name|term
parameter_list|(
name|boolean
name|asc
parameter_list|)
block|{
return|return
name|asc
condition|?
name|InternalOrder
operator|.
name|TERM_ASC
else|:
name|InternalOrder
operator|.
name|TERM_DESC
return|;
block|}
comment|/**          * Creates a bucket ordering strategy which sorts buckets based on a single-valued calc get          *          * @param   path the name of the get          * @param   asc             The direction of the order (ascending or descending)          */
DECL|method|aggregation
specifier|public
specifier|static
name|Order
name|aggregation
parameter_list|(
name|String
name|path
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
return|return
operator|new
name|InternalOrder
operator|.
name|Aggregation
argument_list|(
name|path
argument_list|,
name|asc
argument_list|)
return|;
block|}
comment|/**          * Creates a bucket ordering strategy which sorts buckets based on a multi-valued calc get          *          * @param   aggregationName the name of the get          * @param   metricName       The name of the value of the multi-value get by which the sorting will be applied          * @param   asc             The direction of the order (ascending or descending)          */
DECL|method|aggregation
specifier|public
specifier|static
name|Order
name|aggregation
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|String
name|metricName
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
return|return
operator|new
name|InternalOrder
operator|.
name|Aggregation
argument_list|(
name|aggregationName
operator|+
literal|"."
operator|+
name|metricName
argument_list|,
name|asc
argument_list|)
return|;
block|}
comment|/**          * Creates a bucket ordering strategy which sorts buckets based multiple criteria          *          * @param   orders a list of {@link Order} objects to sort on, in order of priority          */
DECL|method|compound
specifier|public
specifier|static
name|Order
name|compound
parameter_list|(
name|List
argument_list|<
name|Order
argument_list|>
name|orders
parameter_list|)
block|{
return|return
operator|new
name|InternalOrder
operator|.
name|CompoundOrder
argument_list|(
name|orders
argument_list|)
return|;
block|}
comment|/**          * Creates a bucket ordering strategy which sorts buckets based multiple criteria          *          * @param   orders a list of {@link Order} parameters to sort on, in order of priority          */
DECL|method|compound
specifier|public
specifier|static
name|Order
name|compound
parameter_list|(
name|Order
modifier|...
name|orders
parameter_list|)
block|{
return|return
name|compound
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|orders
argument_list|)
argument_list|)
return|;
block|}
comment|/**          * @return  A comparator for the bucket based on the given terms aggregator. The comparator is used in two phases:          *          *          - aggregation phase, where each shard builds a list of term buckets to be sent to the coordinating node.          *            In this phase, the passed in aggregator will be the terms aggregator that aggregates the buckets on the          *            shard level.          *          *          - reduce phase, where the coordinating node gathers all the buckets from all the shards and reduces them          *            to a final bucket list. In this case, the passed in aggregator will be {@code null}          */
DECL|method|comparator
specifier|protected
specifier|abstract
name|Comparator
argument_list|<
name|Bucket
argument_list|>
name|comparator
parameter_list|(
name|Aggregator
name|aggregator
parameter_list|)
function_decl|;
DECL|method|id
specifier|abstract
name|byte
name|id
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|hashCode
specifier|public
specifier|abstract
name|int
name|hashCode
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|abstract
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
function_decl|;
block|}
block|}
end_interface

end_unit

