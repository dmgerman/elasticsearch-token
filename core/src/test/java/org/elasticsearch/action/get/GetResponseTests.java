begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
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
name|Strings
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
name|BytesArray
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
name|collect
operator|.
name|Tuple
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
name|index
operator|.
name|get
operator|.
name|GetField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
operator|.
name|GetResult
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
name|util
operator|.
name|Collections
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
name|XContentHelper
operator|.
name|toXContent
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
operator|.
name|GetResultTests
operator|.
name|copyGetResult
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
operator|.
name|GetResultTests
operator|.
name|mutateGetResult
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|get
operator|.
name|GetResultTests
operator|.
name|randomGetResult
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|EqualsHashCodeTestUtils
operator|.
name|checkEqualsAndHashCode
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertToXContentEquivalent
import|;
end_import

begin_class
DECL|class|GetResponseTests
specifier|public
class|class
name|GetResponseTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToAndFromXContent
specifier|public
name|void
name|testToAndFromXContent
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentType
name|xContentType
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Tuple
argument_list|<
name|GetResult
argument_list|,
name|GetResult
argument_list|>
name|tuple
init|=
name|randomGetResult
argument_list|(
name|xContentType
argument_list|)
decl_stmt|;
name|GetResponse
name|getResponse
init|=
operator|new
name|GetResponse
argument_list|(
name|tuple
operator|.
name|v1
argument_list|()
argument_list|)
decl_stmt|;
name|GetResponse
name|expectedGetResponse
init|=
operator|new
name|GetResponse
argument_list|(
name|tuple
operator|.
name|v2
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|humanReadable
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|BytesReference
name|originalBytes
init|=
name|toShuffledXContent
argument_list|(
name|getResponse
argument_list|,
name|xContentType
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|,
name|humanReadable
argument_list|,
literal|"_source"
argument_list|)
decl_stmt|;
comment|//test that we can parse what we print out
name|GetResponse
name|parsedGetResponse
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContentType
operator|.
name|xContent
argument_list|()
argument_list|,
name|originalBytes
argument_list|)
init|)
block|{
name|parsedGetResponse
operator|=
name|GetResponse
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedGetResponse
argument_list|,
name|parsedGetResponse
argument_list|)
expr_stmt|;
comment|//print the parsed object out and test that the output is the same as the original output
name|BytesReference
name|finalBytes
init|=
name|toXContent
argument_list|(
name|parsedGetResponse
argument_list|,
name|xContentType
argument_list|,
name|humanReadable
argument_list|)
decl_stmt|;
name|assertToXContentEquivalent
argument_list|(
name|originalBytes
argument_list|,
name|finalBytes
argument_list|,
name|xContentType
argument_list|)
expr_stmt|;
comment|//check that the source stays unchanged, no shuffling of keys nor anything like that
name|assertEquals
argument_list|(
name|expectedGetResponse
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|parsedGetResponse
operator|.
name|getSourceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
block|{
block|{
name|GetResponse
name|getResponse
init|=
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{ \"field1\" : "
operator|+
literal|"\"value1\", \"field2\":\"value2\"}"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"field1"
argument_list|,
operator|new
name|GetField
argument_list|(
literal|"field1"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|output
init|=
name|Strings
operator|.
name|toString
argument_list|(
name|getResponse
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":1,\"found\":true,\"_source\":{ \"field1\" "
operator|+
literal|": \"value1\", \"field2\":\"value2\"},\"fields\":{\"field1\":[\"value1\"]}}"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|{
name|GetResponse
name|getResponse
init|=
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|output
init|=
name|Strings
operator|.
name|toString
argument_list|(
name|getResponse
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"found\":false}"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|GetResponse
name|getResponse
init|=
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{ \"field1\" : "
operator|+
literal|"\"value1\", \"field2\":\"value2\"}"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"field1"
argument_list|,
operator|new
name|GetField
argument_list|(
literal|"field1"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":1,\"found\":true,\"_source\":{ \"field1\" "
operator|+
literal|": \"value1\", \"field2\":\"value2\"},\"fields\":{\"field1\":[\"value1\"]}}"
argument_list|,
name|getResponse
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEqualsAndHashcode
specifier|public
name|void
name|testEqualsAndHashcode
parameter_list|()
block|{
name|checkEqualsAndHashCode
argument_list|(
operator|new
name|GetResponse
argument_list|(
name|randomGetResult
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|v1
argument_list|()
argument_list|)
argument_list|,
name|GetResponseTests
operator|::
name|copyGetResponse
argument_list|,
name|GetResponseTests
operator|::
name|mutateGetResponse
argument_list|)
expr_stmt|;
block|}
DECL|method|copyGetResponse
specifier|private
specifier|static
name|GetResponse
name|copyGetResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
return|return
operator|new
name|GetResponse
argument_list|(
name|copyGetResult
argument_list|(
name|getResponse
operator|.
name|getResult
argument_list|)
argument_list|)
return|;
block|}
DECL|method|mutateGetResponse
specifier|private
specifier|static
name|GetResponse
name|mutateGetResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
return|return
operator|new
name|GetResponse
argument_list|(
name|mutateGetResult
argument_list|(
name|getResponse
operator|.
name|getResult
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

