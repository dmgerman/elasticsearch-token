begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|IndicesOptions
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
name|util
operator|.
name|ArrayUtils
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
name|List
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

begin_class
DECL|class|SearchRequestTests
specifier|public
class|class
name|SearchRequestTests
extends|extends
name|AbstractSearchTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchRequest
name|searchRequest
init|=
name|createSearchRequest
argument_list|()
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|searchRequest
operator|.
name|writeTo
argument_list|(
name|output
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
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|SearchRequest
name|deserializedRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|deserializedRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
argument_list|,
name|searchRequest
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedRequest
operator|.
name|hashCode
argument_list|()
argument_list|,
name|searchRequest
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|deserializedRequest
argument_list|,
name|searchRequest
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testIllegalArguments
specifier|public
name|void
name|testIllegalArguments
parameter_list|()
block|{
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|searchRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|searchRequest
operator|.
name|searchType
argument_list|()
argument_list|)
expr_stmt|;
name|NullPointerException
name|e
init|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|indices
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"indices must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|indices
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"indicesOptions must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|types
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"types must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|types
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|searchType
argument_list|(
operator|(
name|SearchType
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"searchType must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|source
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"source must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|searchRequest
operator|.
name|scroll
argument_list|(
operator|(
name|TimeValue
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"keepAlive must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEqualsAndHashcode
specifier|public
name|void
name|testEqualsAndHashcode
parameter_list|()
throws|throws
name|IOException
block|{
name|checkEqualsAndHashCode
argument_list|(
name|createSearchRequest
argument_list|()
argument_list|,
name|SearchRequestTests
operator|::
name|copyRequest
argument_list|,
name|this
operator|::
name|mutate
argument_list|)
expr_stmt|;
block|}
DECL|method|mutate
specifier|private
name|SearchRequest
name|mutate
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|)
throws|throws
name|IOException
block|{
name|SearchRequest
name|mutation
init|=
name|copyRequest
argument_list|(
name|searchRequest
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Runnable
argument_list|>
name|mutators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|indices
argument_list|(
name|ArrayUtils
operator|.
name|concat
argument_list|(
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|indicesOptions
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|types
argument_list|(
name|ArrayUtils
operator|.
name|concat
argument_list|(
name|searchRequest
operator|.
name|types
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|preference
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|preference
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|routing
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|routing
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|requestCache
argument_list|(
operator|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|requestCache
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomBoolean
argument_list|()
argument_list|)
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|scroll
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|scroll
argument_list|()
argument_list|,
parameter_list|()
lambda|->
operator|new
name|Scroll
argument_list|(
operator|new
name|TimeValue
argument_list|(
name|randomNonNegativeLong
argument_list|()
operator|%
literal|100000
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|searchType
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|searchType
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SearchType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mutators
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|mutation
operator|.
name|source
argument_list|(
name|randomValueOtherThan
argument_list|(
name|searchRequest
operator|.
name|source
argument_list|()
argument_list|,
name|this
operator|::
name|createSearchSourceBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|randomFrom
argument_list|(
name|mutators
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|mutation
return|;
block|}
DECL|method|copyRequest
specifier|private
specifier|static
name|SearchRequest
name|copyRequest
parameter_list|(
name|SearchRequest
name|searchRequest
parameter_list|)
throws|throws
name|IOException
block|{
name|SearchRequest
name|result
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|result
operator|.
name|indices
argument_list|(
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|indicesOptions
argument_list|(
name|searchRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|types
argument_list|(
name|searchRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|searchType
argument_list|(
name|searchRequest
operator|.
name|searchType
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|preference
argument_list|(
name|searchRequest
operator|.
name|preference
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|routing
argument_list|(
name|searchRequest
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|requestCache
argument_list|(
name|searchRequest
operator|.
name|requestCache
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|scroll
argument_list|(
name|searchRequest
operator|.
name|scroll
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchRequest
operator|.
name|source
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|source
argument_list|(
name|searchRequest
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

