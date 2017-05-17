begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|Randomness
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
name|BytesStreamOutput
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|abs
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
name|emptyList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|TestUtil
operator|.
name|randomSimpleString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|parseTimeValue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasSize
import|;
end_import

begin_class
DECL|class|BulkByScrollTaskStatusTests
specifier|public
class|class
name|BulkByScrollTaskStatusTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBulkByTaskStatus
specifier|public
name|void
name|testBulkByTaskStatus
parameter_list|()
throws|throws
name|IOException
block|{
name|BulkByScrollTask
operator|.
name|Status
name|status
init|=
name|randomStatus
argument_list|()
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|status
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|tripped
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|)
decl_stmt|;
name|assertTaskStatusEquals
argument_list|(
name|out
operator|.
name|getVersion
argument_list|()
argument_list|,
name|status
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
comment|// Also check round tripping pre-5.1 which is the first version to support parallelized scroll
name|out
operator|=
operator|new
name|BytesStreamOutput
argument_list|()
expr_stmt|;
name|out
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|)
expr_stmt|;
comment|// This can be V_5_0_0
name|status
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
decl_stmt|;
name|in
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|)
expr_stmt|;
name|tripped
operator|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertTaskStatusEquals
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|status
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
block|}
comment|/**      * Assert that two task statuses are equal after serialization.      * @param version the version at which expected was serialized      */
DECL|method|assertTaskStatusEquals
specifier|public
specifier|static
name|void
name|assertTaskStatusEquals
parameter_list|(
name|Version
name|version
parameter_list|,
name|BulkByScrollTask
operator|.
name|Status
name|expected
parameter_list|,
name|BulkByScrollTask
operator|.
name|Status
name|actual
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getTotal
argument_list|()
argument_list|,
name|actual
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getUpdated
argument_list|()
argument_list|,
name|actual
operator|.
name|getUpdated
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getCreated
argument_list|()
argument_list|,
name|actual
operator|.
name|getCreated
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getDeleted
argument_list|()
argument_list|,
name|actual
operator|.
name|getDeleted
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getBatches
argument_list|()
argument_list|,
name|actual
operator|.
name|getBatches
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getVersionConflicts
argument_list|()
argument_list|,
name|actual
operator|.
name|getVersionConflicts
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getNoops
argument_list|()
argument_list|,
name|actual
operator|.
name|getNoops
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getBulkRetries
argument_list|()
argument_list|,
name|actual
operator|.
name|getBulkRetries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getSearchRetries
argument_list|()
argument_list|,
name|actual
operator|.
name|getSearchRetries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getThrottled
argument_list|()
argument_list|,
name|actual
operator|.
name|getThrottled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
name|actual
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
literal|0f
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getReasonCancelled
argument_list|()
argument_list|,
name|actual
operator|.
name|getReasonCancelled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getThrottledUntil
argument_list|()
argument_list|,
name|actual
operator|.
name|getThrottledUntil
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_1_1
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|actual
operator|.
name|getSliceStatuses
argument_list|()
argument_list|,
name|Matchers
operator|.
name|hasSize
argument_list|(
name|expected
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|BulkByScrollTask
operator|.
name|StatusOrException
name|sliceStatus
init|=
name|expected
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|sliceStatus
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|actual
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sliceStatus
operator|.
name|getException
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|actual
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|assertTaskStatusEquals
argument_list|(
name|version
argument_list|,
name|sliceStatus
operator|.
name|getStatus
argument_list|()
argument_list|,
name|actual
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|actual
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
comment|// Just check the message because we're not testing exception serialization in general here.
name|assertEquals
argument_list|(
name|sliceStatus
operator|.
name|getException
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|actual
operator|.
name|getSliceStatuses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getException
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|emptyList
argument_list|()
argument_list|,
name|actual
operator|.
name|getSliceStatuses
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|randomStatus
specifier|public
specifier|static
name|BulkByScrollTask
operator|.
name|Status
name|randomStatus
parameter_list|()
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
return|return
name|randomWorkingStatus
argument_list|(
literal|null
argument_list|)
return|;
block|}
name|boolean
name|canHaveNullStatues
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|>
name|statuses
init|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|between
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
block|{
if|if
condition|(
name|canHaveNullStatues
operator|&&
name|LuceneTestCase
operator|.
name|rarely
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
return|return
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|(
operator|new
name|ElasticsearchException
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
return|return
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|(
name|randomWorkingStatus
argument_list|(
name|i
argument_list|)
argument_list|)
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|toList
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|statuses
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|"test"
else|:
literal|null
argument_list|)
return|;
block|}
DECL|method|randomWorkingStatus
specifier|private
specifier|static
name|BulkByScrollTask
operator|.
name|Status
name|randomWorkingStatus
parameter_list|(
name|Integer
name|sliceId
parameter_list|)
block|{
comment|// These all should be believably small because we sum them if we have multiple workers
name|int
name|total
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10000000
argument_list|)
decl_stmt|;
name|int
name|updated
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|total
argument_list|)
decl_stmt|;
name|int
name|created
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|total
operator|-
name|updated
argument_list|)
decl_stmt|;
name|int
name|deleted
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|total
operator|-
name|updated
operator|-
name|created
argument_list|)
decl_stmt|;
name|int
name|noops
init|=
name|total
operator|-
name|updated
operator|-
name|created
operator|-
name|deleted
decl_stmt|;
name|int
name|batches
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|long
name|versionConflicts
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|total
argument_list|)
decl_stmt|;
name|long
name|bulkRetries
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10000000
argument_list|)
decl_stmt|;
name|long
name|searchRetries
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
return|return
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|sliceId
argument_list|,
name|total
argument_list|,
name|updated
argument_list|,
name|created
argument_list|,
name|deleted
argument_list|,
name|batches
argument_list|,
name|versionConflicts
argument_list|,
name|noops
argument_list|,
name|bulkRetries
argument_list|,
name|searchRetries
argument_list|,
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|,
name|abs
argument_list|(
name|Randomness
operator|.
name|get
argument_list|()
operator|.
name|nextFloat
argument_list|()
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
name|randomSimpleString
argument_list|(
name|Randomness
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

