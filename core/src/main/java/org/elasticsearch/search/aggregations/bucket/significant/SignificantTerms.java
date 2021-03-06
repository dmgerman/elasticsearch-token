begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant
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
name|significant
package|;
end_package

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
name|List
import|;
end_import

begin_comment
comment|/**  * An aggregation that collects significant terms in comparison to a background set.  */
end_comment

begin_interface
DECL|interface|SignificantTerms
specifier|public
interface|interface
name|SignificantTerms
extends|extends
name|MultiBucketsAggregation
extends|,
name|Iterable
argument_list|<
name|SignificantTerms
operator|.
name|Bucket
argument_list|>
block|{
DECL|interface|Bucket
interface|interface
name|Bucket
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
block|{
comment|/**          * @return The significant score for the subset          */
DECL|method|getSignificanceScore
name|double
name|getSignificanceScore
parameter_list|()
function_decl|;
comment|/**          * @return The number of docs in the subset containing a particular term.          * This number is equal to the document count of the bucket.          */
DECL|method|getSubsetDf
name|long
name|getSubsetDf
parameter_list|()
function_decl|;
comment|/**          * @return The numbers of docs in the subset (also known as "foreground set").          * This number is equal to the document count of the containing aggregation.          */
DECL|method|getSubsetSize
name|long
name|getSubsetSize
parameter_list|()
function_decl|;
comment|/**          * @return The number of docs in the superset containing a particular term (also          * known as the "background count" of the bucket)          */
DECL|method|getSupersetDf
name|long
name|getSupersetDf
parameter_list|()
function_decl|;
comment|/**          * @return The numbers of docs in the superset (ordinarily the background count          * of the containing aggregation).          */
DECL|method|getSupersetSize
name|long
name|getSupersetSize
parameter_list|()
function_decl|;
comment|/**          * @return The key, expressed as a number          */
DECL|method|getKeyAsNumber
name|Number
name|getKeyAsNumber
parameter_list|()
function_decl|;
block|}
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
block|}
end_interface

end_unit

