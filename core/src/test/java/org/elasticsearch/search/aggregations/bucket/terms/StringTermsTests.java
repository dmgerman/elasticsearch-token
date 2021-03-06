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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
operator|.
name|Reader
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
name|DocValueFormat
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
name|BucketOrder
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
name|InternalAggregations
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
name|ParsedMultiBucketAggregation
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
name|pipeline
operator|.
name|PipelineAggregator
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
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|StringTermsTests
specifier|public
class|class
name|StringTermsTests
extends|extends
name|InternalTermsTestCase
block|{
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalTerms
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|createTestInstance
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|showTermDocCountError
parameter_list|,
name|long
name|docCountError
parameter_list|)
block|{
name|BucketOrder
name|order
init|=
name|BucketOrder
operator|.
name|count
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|long
name|minDocCount
init|=
literal|1
decl_stmt|;
name|int
name|requiredSize
init|=
literal|3
decl_stmt|;
name|int
name|shardSize
init|=
name|requiredSize
operator|+
literal|2
decl_stmt|;
name|DocValueFormat
name|format
init|=
name|DocValueFormat
operator|.
name|RAW
decl_stmt|;
name|long
name|otherDocCount
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|StringTerms
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numBuckets
init|=
name|randomNumberOfBuckets
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BytesRef
argument_list|>
name|terms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|numBuckets
condition|;
operator|++
name|i
control|)
block|{
name|BytesRef
name|term
init|=
name|randomValueOtherThanMany
argument_list|(
name|b
lambda|->
name|terms
operator|.
name|add
argument_list|(
name|b
argument_list|)
operator|==
literal|false
argument_list|,
parameter_list|()
lambda|->
operator|new
name|BytesRef
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
operator|new
name|StringTerms
operator|.
name|Bucket
argument_list|(
name|term
argument_list|,
name|docCount
argument_list|,
name|aggregations
argument_list|,
name|showTermDocCountError
argument_list|,
name|docCountError
argument_list|,
name|format
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|StringTerms
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|,
name|format
argument_list|,
name|shardSize
argument_list|,
name|showTermDocCountError
argument_list|,
name|otherDocCount
argument_list|,
name|buckets
argument_list|,
name|docCountError
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Reader
argument_list|<
name|InternalTerms
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|StringTerms
operator|::
operator|new
return|;
block|}
annotation|@
name|Override
DECL|method|implementationClass
specifier|protected
name|Class
argument_list|<
name|?
extends|extends
name|ParsedMultiBucketAggregation
argument_list|>
name|implementationClass
parameter_list|()
block|{
return|return
name|ParsedStringTerms
operator|.
name|class
return|;
block|}
block|}
end_class

end_unit

