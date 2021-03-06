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
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
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
name|ToXContent
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
name|XContentBuilder
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
name|json
operator|.
name|JsonXContent
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
name|Arrays
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
name|min
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
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMillis
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
name|timeValueNanos
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
name|containsString
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
name|hasItem
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
name|not
import|;
end_import

begin_class
DECL|class|BulkByScrollTaskTests
specifier|public
class|class
name|BulkByScrollTaskTests
extends|extends
name|ESTestCase
block|{
DECL|method|testStatusHatesNegatives
specifier|public
name|void
name|testStatusHatesNegatives
parameter_list|()
block|{
name|checkStatusNegatives
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"sliceId"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"total"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"updated"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"created"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"deleted"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"batches"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"versionConflicts"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"noops"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|,
literal|"bulkRetries"
argument_list|)
expr_stmt|;
name|checkStatusNegatives
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|"searchRetries"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Build a task status with only some values. Used for testing negative values.      */
DECL|method|checkStatusNegatives
specifier|private
name|void
name|checkStatusNegatives
parameter_list|(
name|Integer
name|sliceId
parameter_list|,
name|long
name|total
parameter_list|,
name|long
name|updated
parameter_list|,
name|long
name|created
parameter_list|,
name|long
name|deleted
parameter_list|,
name|int
name|batches
parameter_list|,
name|long
name|versionConflicts
parameter_list|,
name|long
name|noops
parameter_list|,
name|long
name|bulkRetries
parameter_list|,
name|long
name|searchRetries
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
name|TimeValue
name|throttle
init|=
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|TimeValue
name|throttledUntil
init|=
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
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
name|throttle
argument_list|,
literal|0f
argument_list|,
literal|null
argument_list|,
name|throttledUntil
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|fieldName
operator|+
literal|" must be greater than 0 but was [-1]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContentRepresentationOfUnlimitedRequestsPerSecond
specifier|public
name|void
name|testXContentRepresentationOfUnlimitedRequestsPerSecond
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|status
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|,
literal|null
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|status
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"requests_per_second\":-1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContentRepresentationOfUnfinishedSlices
specifier|public
name|void
name|testXContentRepresentationOfUnfinishedSlices
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|completedStatus
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
literal|2
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|,
literal|null
argument_list|,
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|status
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|(
name|completedStatus
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|status
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"slices\":[null,null,{\"slice_id\":2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContentRepresentationOfSliceFailures
specifier|public
name|void
name|testXContentRepresentationOfSliceFailures
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
operator|new
name|Exception
argument_list|()
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|status
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|(
name|e
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|status
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"slices\":[null,null,{\"type\":\"exception\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMergeStatuses
specifier|public
name|void
name|testMergeStatuses
parameter_list|()
block|{
name|BulkByScrollTask
operator|.
name|StatusOrException
index|[]
name|statuses
init|=
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
index|[
name|between
argument_list|(
literal|2
argument_list|,
literal|100
argument_list|)
index|]
decl_stmt|;
name|boolean
name|containsNullStatuses
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|int
name|mergedTotal
init|=
literal|0
decl_stmt|;
name|int
name|mergedUpdated
init|=
literal|0
decl_stmt|;
name|int
name|mergedCreated
init|=
literal|0
decl_stmt|;
name|int
name|mergedDeleted
init|=
literal|0
decl_stmt|;
name|int
name|mergedBatches
init|=
literal|0
decl_stmt|;
name|int
name|mergedVersionConflicts
init|=
literal|0
decl_stmt|;
name|int
name|mergedNoops
init|=
literal|0
decl_stmt|;
name|int
name|mergedBulkRetries
init|=
literal|0
decl_stmt|;
name|int
name|mergedSearchRetries
init|=
literal|0
decl_stmt|;
name|TimeValue
name|mergedThrottled
init|=
name|timeValueNanos
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|float
name|mergedRequestsPerSecond
init|=
literal|0
decl_stmt|;
name|TimeValue
name|mergedThrottledUntil
init|=
name|timeValueNanos
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
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
name|statuses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|containsNullStatuses
operator|&&
name|rarely
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|int
name|total
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10000
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
name|batches
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|int
name|versionConflicts
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100
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
name|bulkRetries
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|searchRetries
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|TimeValue
name|throttled
init|=
name|timeValueNanos
argument_list|(
name|between
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
argument_list|)
decl_stmt|;
name|float
name|requestsPerSecond
init|=
name|randomValueOtherThanMany
argument_list|(
name|r
lambda|->
name|r
operator|<=
literal|0
argument_list|,
parameter_list|()
lambda|->
name|randomFloat
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|reasonCancelled
init|=
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
literal|"test"
decl_stmt|;
name|TimeValue
name|throttledUntil
init|=
name|timeValueNanos
argument_list|(
name|between
argument_list|(
literal|0
argument_list|,
literal|1000
argument_list|)
argument_list|)
decl_stmt|;
name|statuses
index|[
name|i
index|]
operator|=
operator|new
name|BulkByScrollTask
operator|.
name|StatusOrException
argument_list|(
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|i
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
name|throttled
argument_list|,
name|requestsPerSecond
argument_list|,
name|reasonCancelled
argument_list|,
name|throttledUntil
argument_list|)
argument_list|)
expr_stmt|;
name|mergedTotal
operator|+=
name|total
expr_stmt|;
name|mergedUpdated
operator|+=
name|updated
expr_stmt|;
name|mergedCreated
operator|+=
name|created
expr_stmt|;
name|mergedDeleted
operator|+=
name|deleted
expr_stmt|;
name|mergedBatches
operator|+=
name|batches
expr_stmt|;
name|mergedVersionConflicts
operator|+=
name|versionConflicts
expr_stmt|;
name|mergedNoops
operator|+=
name|noops
expr_stmt|;
name|mergedBulkRetries
operator|+=
name|bulkRetries
expr_stmt|;
name|mergedSearchRetries
operator|+=
name|searchRetries
expr_stmt|;
name|mergedThrottled
operator|=
name|timeValueNanos
argument_list|(
name|mergedThrottled
operator|.
name|nanos
argument_list|()
operator|+
name|throttled
operator|.
name|nanos
argument_list|()
argument_list|)
expr_stmt|;
name|mergedRequestsPerSecond
operator|+=
name|requestsPerSecond
expr_stmt|;
name|mergedThrottledUntil
operator|=
name|timeValueNanos
argument_list|(
name|min
argument_list|(
name|mergedThrottledUntil
operator|.
name|nanos
argument_list|()
argument_list|,
name|throttledUntil
operator|.
name|nanos
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|reasonCancelled
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAlphaOfLength
argument_list|(
literal|10
argument_list|)
else|:
literal|null
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|merged
init|=
operator|new
name|BulkByScrollTask
operator|.
name|Status
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|statuses
argument_list|)
argument_list|,
name|reasonCancelled
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|mergedTotal
argument_list|,
name|merged
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedUpdated
argument_list|,
name|merged
operator|.
name|getUpdated
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedCreated
argument_list|,
name|merged
operator|.
name|getCreated
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedDeleted
argument_list|,
name|merged
operator|.
name|getDeleted
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedBatches
argument_list|,
name|merged
operator|.
name|getBatches
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedVersionConflicts
argument_list|,
name|merged
operator|.
name|getVersionConflicts
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedNoops
argument_list|,
name|merged
operator|.
name|getNoops
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedBulkRetries
argument_list|,
name|merged
operator|.
name|getBulkRetries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedSearchRetries
argument_list|,
name|merged
operator|.
name|getSearchRetries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedThrottled
argument_list|,
name|merged
operator|.
name|getThrottled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedRequestsPerSecond
argument_list|,
name|merged
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
literal|0.0001f
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mergedThrottledUntil
argument_list|,
name|merged
operator|.
name|getThrottledUntil
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|reasonCancelled
argument_list|,
name|merged
operator|.
name|getReasonCancelled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

