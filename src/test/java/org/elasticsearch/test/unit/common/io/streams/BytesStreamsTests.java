begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.common.io.streams
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|common
operator|.
name|io
operator|.
name|streams
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamInput
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
name|CachedStreamOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
name|closeTo
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|Test
DECL|class|BytesStreamsTests
specifier|public
class|class
name|BytesStreamsTests
block|{
annotation|@
name|Test
DECL|method|testSimpleStreams
specifier|public
name|void
name|testSimpleStreams
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesStreamOutput
name|out
init|=
name|CachedStreamOutput
operator|.
name|popEntry
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeShort
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
operator|-
literal|3
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
literal|1.1f
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
literal|2.2
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
literal|"hello"
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
literal|"goodbye"
argument_list|)
expr_stmt|;
name|BytesStreamInput
name|in
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readShort
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readVInt
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
operator|-
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readVLong
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|in
operator|.
name|readFloat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|1.1
argument_list|,
literal|0.0001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|2.2
argument_list|,
literal|0.0001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"hello"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"goodbye"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

