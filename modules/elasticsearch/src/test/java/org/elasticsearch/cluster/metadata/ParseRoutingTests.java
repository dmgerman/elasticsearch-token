begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|compress
operator|.
name|CompressedString
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
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|*
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
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ParseRoutingTests
specifier|public
class|class
name|ParseRoutingTests
block|{
DECL|method|testParseRouting
annotation|@
name|Test
specifier|public
name|void
name|testParseRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|MappingMetaData
name|md
init|=
operator|new
name|MappingMetaData
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedString
argument_list|(
literal|""
argument_list|)
argument_list|,
operator|new
name|MappingMetaData
operator|.
name|Routing
argument_list|(
literal|true
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|array
argument_list|(
literal|"arr1"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"obj1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"ob1_field"
argument_list|,
literal|"obj1_value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseRoutingWithPath
annotation|@
name|Test
specifier|public
name|void
name|testParseRoutingWithPath
parameter_list|()
throws|throws
name|Exception
block|{
name|MappingMetaData
name|md
init|=
operator|new
name|MappingMetaData
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedString
argument_list|(
literal|""
argument_list|)
argument_list|,
operator|new
name|MappingMetaData
operator|.
name|Routing
argument_list|(
literal|true
argument_list|,
literal|"obj1.field2"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|array
argument_list|(
literal|"arr1"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"obj1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseRoutingWithRepeatedField
annotation|@
name|Test
specifier|public
name|void
name|testParseRoutingWithRepeatedField
parameter_list|()
throws|throws
name|Exception
block|{
name|MappingMetaData
name|md
init|=
operator|new
name|MappingMetaData
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedString
argument_list|(
literal|""
argument_list|)
argument_list|,
operator|new
name|MappingMetaData
operator|.
name|Routing
argument_list|(
literal|true
argument_list|,
literal|"field1.field1"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|array
argument_list|(
literal|"arr1"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseRoutingWithRepeatedFieldAndObject
annotation|@
name|Test
specifier|public
name|void
name|testParseRoutingWithRepeatedFieldAndObject
parameter_list|()
throws|throws
name|Exception
block|{
name|MappingMetaData
name|md
init|=
operator|new
name|MappingMetaData
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedString
argument_list|(
literal|""
argument_list|)
argument_list|,
operator|new
name|MappingMetaData
operator|.
name|Routing
argument_list|(
literal|true
argument_list|,
literal|"field1.field1.field2"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|array
argument_list|(
literal|"arr1"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseRoutingWithRepeatedFieldAndValidRouting
annotation|@
name|Test
specifier|public
name|void
name|testParseRoutingWithRepeatedFieldAndValidRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|MappingMetaData
name|md
init|=
operator|new
name|MappingMetaData
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedString
argument_list|(
literal|""
argument_list|)
argument_list|,
operator|new
name|MappingMetaData
operator|.
name|Routing
argument_list|(
literal|true
argument_list|,
literal|"field1.field2"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"aaa"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|array
argument_list|(
literal|"arr1"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"zzz"
argument_list|,
literal|"wr"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|md
operator|.
name|parseRouting
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

