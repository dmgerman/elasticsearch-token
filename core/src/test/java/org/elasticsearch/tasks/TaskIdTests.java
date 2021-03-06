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
name|common
operator|.
name|UUIDs
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
name|bytes
operator|.
name|BytesReference
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|TaskIdTests
specifier|public
class|class
name|TaskIdTests
extends|extends
name|ESTestCase
block|{
DECL|field|ROUNDS
specifier|private
specifier|static
specifier|final
name|int
name|ROUNDS
init|=
literal|30
decl_stmt|;
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
comment|/*          * The size of the serialized representation of the TaskId doesn't really matter that much because most requests don't contain a          * full TaskId.          */
name|int
name|expectedSize
init|=
literal|31
decl_stmt|;
comment|// 8 for the task number, 1 for the string length of the uuid, 22 for the actual uuid
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ROUNDS
condition|;
name|i
operator|++
control|)
block|{
name|TaskId
name|taskId
init|=
operator|new
name|TaskId
argument_list|(
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|randomInt
argument_list|()
argument_list|)
decl_stmt|;
name|TaskId
name|roundTripped
init|=
name|roundTrip
argument_list|(
name|taskId
argument_list|,
name|expectedSize
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|taskId
argument_list|,
name|roundTripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|taskId
argument_list|,
name|roundTripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|taskId
operator|.
name|hashCode
argument_list|()
argument_list|,
name|roundTripped
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSerializationOfEmpty
specifier|public
name|void
name|testSerializationOfEmpty
parameter_list|()
throws|throws
name|IOException
block|{
comment|//The size of the serialized representation of the EMPTY_TASK_ID matters a lot because many requests contain it.
name|int
name|expectedSize
init|=
literal|1
decl_stmt|;
name|TaskId
name|roundTripped
init|=
name|roundTrip
argument_list|(
name|TaskId
operator|.
name|EMPTY_TASK_ID
argument_list|,
name|expectedSize
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|TaskId
operator|.
name|EMPTY_TASK_ID
argument_list|,
name|roundTripped
argument_list|)
expr_stmt|;
block|}
DECL|method|roundTrip
specifier|private
name|TaskId
name|roundTrip
parameter_list|(
name|TaskId
name|taskId
parameter_list|,
name|int
name|expectedSize
parameter_list|)
throws|throws
name|IOException
block|{
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
name|taskId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|BytesReference
name|bytes
init|=
name|out
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedSize
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
name|bytes
operator|.
name|streamInput
argument_list|()
init|)
block|{
return|return
name|TaskId
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

