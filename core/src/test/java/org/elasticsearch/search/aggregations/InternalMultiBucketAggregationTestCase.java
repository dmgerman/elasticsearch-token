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
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Iterator
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_class
DECL|class|InternalMultiBucketAggregationTestCase
specifier|public
specifier|abstract
class|class
name|InternalMultiBucketAggregationTestCase
parameter_list|<
name|T
extends|extends
name|InternalAggregation
operator|&
name|MultiBucketsAggregation
parameter_list|>
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|T
argument_list|>
block|{
DECL|field|hasSubAggregations
specifier|private
name|boolean
name|hasSubAggregations
decl_stmt|;
annotation|@
name|Before
DECL|method|initHasSubAggregations
specifier|public
name|void
name|initHasSubAggregations
parameter_list|()
block|{
name|hasSubAggregations
operator|=
name|randomBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
specifier|final
name|T
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
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|internal
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasSubAggregations
condition|)
block|{
specifier|final
name|int
name|numAggregations
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|3
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
name|numAggregations
condition|;
name|i
operator|++
control|)
block|{
name|internal
operator|.
name|add
argument_list|(
name|createTestInstance
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|pipelineAggregators
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|InternalAggregations
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|createTestInstance
argument_list|(
name|name
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|,
operator|new
name|InternalAggregations
argument_list|(
name|internal
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createTestInstance
specifier|protected
specifier|abstract
name|T
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
parameter_list|)
function_decl|;
DECL|method|implementationClass
specifier|protected
specifier|abstract
name|Class
argument_list|<
name|?
extends|extends
name|ParsedMultiBucketAggregation
argument_list|>
name|implementationClass
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|assertFromXContent
specifier|protected
specifier|final
name|void
name|assertFromXContent
parameter_list|(
name|T
name|aggregation
parameter_list|,
name|ParsedAggregation
name|parsedAggregation
parameter_list|)
block|{
name|assertMultiBucketsAggregation
argument_list|(
name|aggregation
argument_list|,
name|parsedAggregation
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testIterators
specifier|public
name|void
name|testIterators
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|T
name|aggregation
init|=
name|createTestInstance
argument_list|()
decl_stmt|;
name|assertMultiBucketsAggregation
argument_list|(
name|aggregation
argument_list|,
name|parseAndAssert
argument_list|(
name|aggregation
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|assertMultiBucketsAggregation
specifier|private
name|void
name|assertMultiBucketsAggregation
parameter_list|(
name|Aggregation
name|expected
parameter_list|,
name|Aggregation
name|actual
parameter_list|,
name|boolean
name|checkOrder
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|expected
operator|instanceof
name|MultiBucketsAggregation
argument_list|)
expr_stmt|;
name|MultiBucketsAggregation
name|expectedMultiBucketsAggregation
init|=
operator|(
name|MultiBucketsAggregation
operator|)
name|expected
decl_stmt|;
name|assertTrue
argument_list|(
name|actual
operator|instanceof
name|MultiBucketsAggregation
argument_list|)
expr_stmt|;
name|MultiBucketsAggregation
name|actualMultiBucketsAggregation
init|=
operator|(
name|MultiBucketsAggregation
operator|)
name|actual
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|ParsedMultiBucketAggregation
argument_list|>
name|parsedClass
init|=
name|implementationClass
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|parsedClass
operator|!=
literal|null
operator|&&
name|parsedClass
operator|.
name|isInstance
argument_list|(
name|actual
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|instanceof
name|InternalAggregation
operator|&&
name|actual
operator|instanceof
name|ParsedAggregation
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getName
argument_list|()
argument_list|,
name|actual
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getMetaData
argument_list|()
argument_list|,
name|actual
operator|.
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|InternalAggregation
operator|)
name|expected
operator|)
operator|.
name|getType
argument_list|()
argument_list|,
operator|(
operator|(
name|ParsedAggregation
operator|)
name|actual
operator|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
argument_list|>
name|expectedBuckets
init|=
name|expectedMultiBucketsAggregation
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
argument_list|>
name|actualBuckets
init|=
name|actualMultiBucketsAggregation
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedBuckets
operator|.
name|size
argument_list|()
argument_list|,
name|actualBuckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkOrder
condition|)
block|{
name|Iterator
argument_list|<
name|?
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
argument_list|>
name|expectedIt
init|=
name|expectedBuckets
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|?
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
argument_list|>
name|actualIt
init|=
name|actualBuckets
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|expectedIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MultiBucketsAggregation
operator|.
name|Bucket
name|expectedBucket
init|=
name|expectedIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|MultiBucketsAggregation
operator|.
name|Bucket
name|actualBucket
init|=
name|actualIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertBucket
argument_list|(
name|expectedBucket
argument_list|,
name|actualBucket
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|MultiBucketsAggregation
operator|.
name|Bucket
name|expectedBucket
range|:
name|expectedBuckets
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MultiBucketsAggregation
operator|.
name|Bucket
name|actualBucket
range|:
name|actualBuckets
control|)
block|{
if|if
condition|(
name|actualBucket
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|expectedBucket
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
name|assertBucket
argument_list|(
name|expectedBucket
argument_list|,
name|actualBucket
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Failed to find bucket with key ["
operator|+
name|expectedBucket
operator|.
name|getKey
argument_list|()
operator|+
literal|"]"
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|assertBucket
specifier|private
name|void
name|assertBucket
parameter_list|(
name|MultiBucketsAggregation
operator|.
name|Bucket
name|expected
parameter_list|,
name|MultiBucketsAggregation
operator|.
name|Bucket
name|actual
parameter_list|,
name|boolean
name|checkOrder
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|expected
operator|instanceof
name|InternalMultiBucketAggregation
operator|.
name|InternalBucket
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|actual
operator|instanceof
name|ParsedMultiBucketAggregation
operator|.
name|ParsedBucket
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getKey
argument_list|()
argument_list|,
name|actual
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getKeyAsString
argument_list|()
argument_list|,
name|actual
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|actual
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
name|Aggregations
name|expectedAggregations
init|=
name|expected
operator|.
name|getAggregations
argument_list|()
decl_stmt|;
name|Aggregations
name|actualAggregations
init|=
name|actual
operator|.
name|getAggregations
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedAggregations
operator|.
name|asList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|actualAggregations
operator|.
name|asList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkOrder
condition|)
block|{
name|Iterator
argument_list|<
name|Aggregation
argument_list|>
name|expectedIt
init|=
name|expectedAggregations
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Aggregation
argument_list|>
name|actualIt
init|=
name|actualAggregations
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|expectedIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Aggregation
name|expectedAggregation
init|=
name|expectedIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|Aggregation
name|actualAggregation
init|=
name|actualIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertMultiBucketsAggregation
argument_list|(
name|expectedAggregation
argument_list|,
name|actualAggregation
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|Aggregation
name|expectedAggregation
range|:
name|expectedAggregations
control|)
block|{
name|Aggregation
name|actualAggregation
init|=
name|actualAggregations
operator|.
name|get
argument_list|(
name|expectedAggregation
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|actualAggregation
argument_list|)
expr_stmt|;
name|assertMultiBucketsAggregation
argument_list|(
name|expectedAggregation
argument_list|,
name|actualAggregation
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

