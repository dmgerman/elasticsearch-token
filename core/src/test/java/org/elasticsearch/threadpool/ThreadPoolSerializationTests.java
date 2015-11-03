begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
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
name|common
operator|.
name|settings
operator|.
name|Settings
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
name|unit
operator|.
name|SizeValue
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
name|XContentFactory
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
name|test
operator|.
name|ESTestCase
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
name|Map
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
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|hasKey
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
name|is
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ThreadPoolSerializationTests
specifier|public
class|class
name|ThreadPoolSerializationTests
extends|extends
name|ESTestCase
block|{
DECL|field|output
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
DECL|field|threadPoolType
specifier|private
name|ThreadPool
operator|.
name|ThreadPoolType
name|threadPoolType
decl_stmt|;
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|threadPoolType
operator|=
name|randomFrom
argument_list|(
name|ThreadPool
operator|.
name|ThreadPoolType
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatQueueSizeSerializationWorks
specifier|public
name|void
name|testThatQueueSizeSerializationWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
operator|.
name|Info
name|info
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|(
literal|"foo"
argument_list|,
name|threadPoolType
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|3000
argument_list|)
argument_list|,
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"10k"
argument_list|)
argument_list|)
decl_stmt|;
name|output
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|info
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|StreamInput
name|input
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadPool
operator|.
name|Info
name|newInfo
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|()
decl_stmt|;
name|newInfo
operator|.
name|readFrom
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newInfo
operator|.
name|getQueueSize
argument_list|()
operator|.
name|singles
argument_list|()
argument_list|,
name|is
argument_list|(
literal|10000l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatNegativeQueueSizesCanBeSerialized
specifier|public
name|void
name|testThatNegativeQueueSizesCanBeSerialized
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
operator|.
name|Info
name|info
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|(
literal|"foo"
argument_list|,
name|threadPoolType
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|3000
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|output
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|info
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|StreamInput
name|input
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadPool
operator|.
name|Info
name|newInfo
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|()
decl_stmt|;
name|newInfo
operator|.
name|readFrom
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newInfo
operator|.
name|getQueueSize
argument_list|()
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatToXContentWritesOutUnboundedCorrectly
specifier|public
name|void
name|testThatToXContentWritesOutUnboundedCorrectly
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
operator|.
name|Info
name|info
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|(
literal|"foo"
argument_list|,
name|threadPoolType
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|3000
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|info
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|BytesReference
name|bytesReference
init|=
name|builder
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytesReference
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytesReference
argument_list|)
init|)
block|{
name|map
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|map
argument_list|,
name|hasKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
argument_list|,
name|hasKey
argument_list|(
literal|"queue_size"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"queue_size"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"-1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatNegativeSettingAllowsToStart
specifier|public
name|void
name|testThatNegativeSettingAllowsToStart
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"index"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.index.queue_size"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|threadPool
operator|.
name|info
argument_list|(
literal|"index"
argument_list|)
operator|.
name|getQueueSize
argument_list|()
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatToXContentWritesInteger
specifier|public
name|void
name|testThatToXContentWritesInteger
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
operator|.
name|Info
name|info
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|(
literal|"foo"
argument_list|,
name|threadPoolType
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|3000
argument_list|)
argument_list|,
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"1k"
argument_list|)
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|info
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|BytesReference
name|bytesReference
init|=
name|builder
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytesReference
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytesReference
argument_list|)
init|)
block|{
name|map
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|map
argument_list|,
name|hasKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
argument_list|,
name|hasKey
argument_list|(
literal|"queue_size"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"queue_size"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"1000"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatThreadPoolTypeIsSerializedCorrectly
specifier|public
name|void
name|testThatThreadPoolTypeIsSerializedCorrectly
parameter_list|()
throws|throws
name|IOException
block|{
name|ThreadPool
operator|.
name|Info
name|info
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|(
literal|"foo"
argument_list|,
name|threadPoolType
argument_list|)
decl_stmt|;
name|output
operator|.
name|setVersion
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|info
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|StreamInput
name|input
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadPool
operator|.
name|Info
name|newInfo
init|=
operator|new
name|ThreadPool
operator|.
name|Info
argument_list|()
decl_stmt|;
name|newInfo
operator|.
name|readFrom
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newInfo
operator|.
name|getThreadPoolType
argument_list|()
argument_list|,
name|is
argument_list|(
name|threadPoolType
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

