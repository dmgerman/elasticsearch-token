begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
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
name|text
operator|.
name|Text
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
name|util
operator|.
name|Comparators
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
name|Aggregation
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
name|Aggregations
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
name|HasAggregations
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
name|OrderPath
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  * An aggregation that returns multiple buckets  */
end_comment

begin_interface
DECL|interface|MultiBucketsAggregation
specifier|public
interface|interface
name|MultiBucketsAggregation
extends|extends
name|Aggregation
block|{
comment|/**      * A bucket represents a criteria to which all documents that fall in it adhere to. It is also uniquely identified      * by a key, and can potentially hold sub-aggregations computed over all documents in it.      */
DECL|interface|Bucket
specifier|public
interface|interface
name|Bucket
extends|extends
name|HasAggregations
block|{
comment|/**          * @return  The key associated with the bucket as a string          */
DECL|method|getKey
name|String
name|getKey
parameter_list|()
function_decl|;
comment|/**          * @return  The key associated with the bucket as text (ideal for further streaming this instance)          */
DECL|method|getKeyAsText
name|Text
name|getKeyAsText
parameter_list|()
function_decl|;
comment|/**          * @return The number of documents that fall within this bucket          */
DECL|method|getDocCount
name|long
name|getDocCount
parameter_list|()
function_decl|;
comment|/**          * @return  The sub-aggregations of this bucket          */
DECL|method|getAggregations
name|Aggregations
name|getAggregations
parameter_list|()
function_decl|;
DECL|class|SubAggregationComparator
specifier|static
class|class
name|SubAggregationComparator
parameter_list|<
name|B
extends|extends
name|Bucket
parameter_list|>
implements|implements
name|java
operator|.
name|util
operator|.
name|Comparator
argument_list|<
name|B
argument_list|>
block|{
DECL|field|path
specifier|private
specifier|final
name|OrderPath
name|path
decl_stmt|;
DECL|field|asc
specifier|private
specifier|final
name|boolean
name|asc
decl_stmt|;
DECL|method|SubAggregationComparator
specifier|public
name|SubAggregationComparator
parameter_list|(
name|String
name|expression
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
name|this
operator|.
name|asc
operator|=
name|asc
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|OrderPath
operator|.
name|parse
argument_list|(
name|expression
argument_list|)
expr_stmt|;
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
DECL|method|path
specifier|public
name|OrderPath
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|B
name|b1
parameter_list|,
name|B
name|b2
parameter_list|)
block|{
name|double
name|v1
init|=
name|path
operator|.
name|resolveValue
argument_list|(
name|b1
argument_list|)
decl_stmt|;
name|double
name|v2
init|=
name|path
operator|.
name|resolveValue
argument_list|(
name|b2
argument_list|)
decl_stmt|;
return|return
name|Comparators
operator|.
name|compareDiscardNaN
argument_list|(
name|v1
argument_list|,
name|v2
argument_list|,
name|asc
argument_list|)
return|;
block|}
block|}
block|}
comment|/**      * @return  The buckets of this aggregation.      */
DECL|method|getBuckets
name|Collection
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
function_decl|;
comment|/**      * The bucket that is associated with the given key.      *      * @param key   The key of the requested bucket.      * @return      The bucket      */
DECL|method|getBucketByKey
parameter_list|<
name|B
extends|extends
name|Bucket
parameter_list|>
name|B
name|getBucketByKey
parameter_list|(
name|String
name|key
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

