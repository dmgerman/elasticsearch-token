begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|mapper
operator|.
name|ParentFieldMapper
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
name|mapper
operator|.
name|RoutingFieldMapper
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
name|mapper
operator|.
name|UidFieldMapper
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
name|elasticsearch
operator|.
name|test
operator|.
name|RandomObjects
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
name|Arrays
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
DECL|class|GetFieldTests
specifier|public
class|class
name|GetFieldTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|GetField
name|getField
init|=
operator|new
name|GetField
argument_list|(
literal|"field"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"value1"
argument_list|,
literal|"value2"
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
name|getField
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"field\":[\"value1\",\"value2\"]}"
argument_list|,
name|output
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
name|randomGetField
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|v1
argument_list|()
argument_list|,
name|GetFieldTests
operator|::
name|copyGetField
argument_list|,
name|GetFieldTests
operator|::
name|mutateGetField
argument_list|)
expr_stmt|;
block|}
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
name|GetField
argument_list|,
name|GetField
argument_list|>
name|tuple
init|=
name|randomGetField
argument_list|(
name|xContentType
argument_list|)
decl_stmt|;
name|GetField
name|getField
init|=
name|tuple
operator|.
name|v1
argument_list|()
decl_stmt|;
name|GetField
name|expectedGetField
init|=
name|tuple
operator|.
name|v2
argument_list|()
decl_stmt|;
name|BytesReference
name|originalBytes
init|=
name|toXContent
argument_list|(
name|getField
argument_list|,
name|xContentType
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|//test that we can parse what we print out
name|GetField
name|parsedGetField
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
comment|//we need to move to the next token, the start object one that we manually added is not expected
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|parsedGetField
operator|=
name|GetField
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
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
name|expectedGetField
argument_list|,
name|parsedGetField
argument_list|)
expr_stmt|;
name|BytesReference
name|finalBytes
init|=
name|toXContent
argument_list|(
name|parsedGetField
argument_list|,
name|xContentType
argument_list|,
literal|true
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
block|}
DECL|method|copyGetField
specifier|private
specifier|static
name|GetField
name|copyGetField
parameter_list|(
name|GetField
name|getField
parameter_list|)
block|{
return|return
operator|new
name|GetField
argument_list|(
name|getField
operator|.
name|getName
argument_list|()
argument_list|,
name|getField
operator|.
name|getValues
argument_list|()
argument_list|)
return|;
block|}
DECL|method|mutateGetField
specifier|private
specifier|static
name|GetField
name|mutateGetField
parameter_list|(
name|GetField
name|getField
parameter_list|)
block|{
name|List
argument_list|<
name|Supplier
argument_list|<
name|GetField
argument_list|>
argument_list|>
name|mutations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|mutations
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
operator|new
name|GetField
argument_list|(
name|randomUnicodeOfCodepointLength
argument_list|(
literal|15
argument_list|)
argument_list|,
name|getField
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
operator|new
name|GetField
argument_list|(
name|getField
operator|.
name|getName
argument_list|()
argument_list|,
name|randomGetField
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|v1
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|randomFrom
argument_list|(
name|mutations
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|randomGetField
specifier|public
specifier|static
name|Tuple
argument_list|<
name|GetField
argument_list|,
name|GetField
argument_list|>
name|randomGetField
parameter_list|(
name|XContentType
name|xContentType
parameter_list|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|String
name|fieldName
init|=
name|randomFrom
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|,
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|GetField
name|getField
init|=
operator|new
name|GetField
argument_list|(
name|fieldName
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|getField
argument_list|,
name|getField
argument_list|)
return|;
block|}
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Tuple
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|tuple
init|=
name|RandomObjects
operator|.
name|randomStoredFieldValues
argument_list|(
name|random
argument_list|()
argument_list|,
name|xContentType
argument_list|)
decl_stmt|;
name|GetField
name|input
init|=
operator|new
name|GetField
argument_list|(
name|fieldName
argument_list|,
name|tuple
operator|.
name|v1
argument_list|()
argument_list|)
decl_stmt|;
name|GetField
name|expected
init|=
operator|new
name|GetField
argument_list|(
name|fieldName
argument_list|,
name|tuple
operator|.
name|v2
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|input
argument_list|,
name|expected
argument_list|)
return|;
block|}
block|}
end_class

end_unit

