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
name|InternalMultiBucketAggregation
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
DECL|class|Bucket
specifier|static
specifier|abstract
class|class
name|Bucket
extends|extends
name|InternalMultiBucketAggregation
operator|.
name|InternalBucket
block|{
DECL|field|subsetDf
name|long
name|subsetDf
decl_stmt|;
DECL|field|subsetSize
name|long
name|subsetSize
decl_stmt|;
DECL|field|supersetDf
name|long
name|supersetDf
decl_stmt|;
DECL|field|supersetSize
name|long
name|supersetSize
decl_stmt|;
DECL|method|Bucket
specifier|protected
name|Bucket
parameter_list|(
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetSize
parameter_list|)
block|{
comment|// for serialization
name|this
operator|.
name|subsetSize
operator|=
name|subsetSize
expr_stmt|;
name|this
operator|.
name|supersetSize
operator|=
name|supersetSize
expr_stmt|;
block|}
DECL|method|Bucket
name|Bucket
parameter_list|(
name|long
name|subsetDf
parameter_list|,
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetDf
parameter_list|,
name|long
name|supersetSize
parameter_list|)
block|{
name|this
argument_list|(
name|subsetSize
argument_list|,
name|supersetSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|subsetDf
operator|=
name|subsetDf
expr_stmt|;
name|this
operator|.
name|supersetDf
operator|=
name|supersetDf
expr_stmt|;
block|}
DECL|method|compareTerm
specifier|abstract
name|int
name|compareTerm
parameter_list|(
name|SignificantTerms
operator|.
name|Bucket
name|other
parameter_list|)
function_decl|;
DECL|method|getSignificanceScore
specifier|public
specifier|abstract
name|double
name|getSignificanceScore
parameter_list|()
function_decl|;
DECL|method|getKeyAsNumber
specifier|abstract
name|Number
name|getKeyAsNumber
parameter_list|()
function_decl|;
DECL|method|getSubsetDf
specifier|public
name|long
name|getSubsetDf
parameter_list|()
block|{
return|return
name|subsetDf
return|;
block|}
DECL|method|getSupersetDf
specifier|public
name|long
name|getSupersetDf
parameter_list|()
block|{
return|return
name|supersetDf
return|;
block|}
DECL|method|getSupersetSize
specifier|public
name|long
name|getSupersetSize
parameter_list|()
block|{
return|return
name|supersetSize
return|;
block|}
DECL|method|getSubsetSize
specifier|public
name|long
name|getSubsetSize
parameter_list|()
block|{
return|return
name|subsetSize
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getBuckets
name|List
argument_list|<
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
