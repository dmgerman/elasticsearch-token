begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.tasks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|NamedWriteableAwareStreamInput
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
name|NamedWriteableRegistry
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
name|XContentHelper
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|Collections
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
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * Round trip tests for {@link PersistedTaskInfo} and those classes that it includes like {@link TaskInfo} and {@link RawTaskStatus}.  */
end_comment

begin_class
DECL|class|PersistedTaskInfoTests
specifier|public
class|class
name|PersistedTaskInfoTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBinaryRoundTrip
specifier|public
name|void
name|testBinaryRoundTrip
parameter_list|()
throws|throws
name|IOException
block|{
name|NamedWriteableRegistry
name|registry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|NamedWriteableRegistry
operator|.
name|Entry
argument_list|(
name|Task
operator|.
name|Status
operator|.
name|class
argument_list|,
name|RawTaskStatus
operator|.
name|NAME
argument_list|,
name|RawTaskStatus
operator|::
operator|new
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|PersistedTaskInfo
name|result
init|=
name|randomTaskResult
argument_list|()
decl_stmt|;
name|PersistedTaskInfo
name|read
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|result
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|,
name|registry
argument_list|)
init|)
block|{
name|read
operator|=
operator|new
name|PersistedTaskInfo
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error processing ["
operator|+
name|result
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
name|result
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
DECL|method|testXContentRoundTrip
specifier|public
name|void
name|testXContentRoundTrip
parameter_list|()
throws|throws
name|IOException
block|{
comment|/*          * Note that this round trip isn't 100% perfect - status will always be read as RawTaskStatus. Since this test uses RawTaskStatus          * as the status we randomly generate then we can assert the round trip with .equals.          */
name|PersistedTaskInfo
name|result
init|=
name|randomTaskResult
argument_list|()
decl_stmt|;
name|PersistedTaskInfo
name|read
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
name|result
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
try|try
init|(
name|XContentBuilder
name|shuffled
init|=
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
init|;
name|XContentParser
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|shuffled
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|read
operator|=
name|PersistedTaskInfo
operator|.
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
parameter_list|()
lambda|->
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error processing ["
operator|+
name|result
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
name|result
argument_list|,
name|read
argument_list|)
expr_stmt|;
block|}
DECL|method|randomTaskResult
specifier|private
specifier|static
name|PersistedTaskInfo
name|randomTaskResult
parameter_list|()
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|between
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|PersistedTaskInfo
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
name|randomTaskInfo
argument_list|()
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|new
name|PersistedTaskInfo
argument_list|(
name|randomTaskInfo
argument_list|()
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|new
name|PersistedTaskInfo
argument_list|(
name|randomTaskInfo
argument_list|()
argument_list|,
name|randomTaskResponse
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported random TaskResult constructor"
argument_list|)
throw|;
block|}
block|}
DECL|method|randomTaskInfo
specifier|private
specifier|static
name|TaskInfo
name|randomTaskInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|TaskId
name|taskId
init|=
name|randomTaskId
argument_list|()
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|String
name|action
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|Task
operator|.
name|Status
name|status
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomRawTaskStatus
argument_list|()
else|:
literal|null
decl_stmt|;
name|String
name|description
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
else|:
literal|null
decl_stmt|;
name|long
name|startTime
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|long
name|runningTimeNanos
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|boolean
name|cancellable
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|TaskId
name|parentTaskId
init|=
name|randomBoolean
argument_list|()
condition|?
name|TaskId
operator|.
name|EMPTY_TASK_ID
else|:
name|randomTaskId
argument_list|()
decl_stmt|;
return|return
operator|new
name|TaskInfo
argument_list|(
name|taskId
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|status
argument_list|,
name|startTime
argument_list|,
name|runningTimeNanos
argument_list|,
name|cancellable
argument_list|,
name|parentTaskId
argument_list|)
return|;
block|}
DECL|method|randomTaskId
specifier|private
specifier|static
name|TaskId
name|randomTaskId
parameter_list|()
block|{
return|return
operator|new
name|TaskId
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomLong
argument_list|()
argument_list|)
return|;
block|}
DECL|method|randomRawTaskStatus
specifier|private
specifier|static
name|RawTaskStatus
name|randomRawTaskStatus
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|Requests
operator|.
name|INDEX_CONTENT_TYPE
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|int
name|fields
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|fields
condition|;
name|f
operator|++
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|RawTaskStatus
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|randomTaskResponse
specifier|private
specifier|static
name|ToXContent
name|randomTaskResponse
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|result
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|fields
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|fields
condition|;
name|f
operator|++
control|)
block|{
name|result
operator|.
name|put
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ToXContent
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Responses in Elasticsearch never output a leading startObject. There isn't really a good reason, they just don't.
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|result
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

