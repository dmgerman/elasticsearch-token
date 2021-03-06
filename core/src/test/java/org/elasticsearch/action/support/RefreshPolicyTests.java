begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
DECL|class|RefreshPolicyTests
specifier|public
class|class
name|RefreshPolicyTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|WriteRequest
operator|.
name|RefreshPolicy
name|refreshPolicy
init|=
name|randomFrom
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|values
argument_list|()
argument_list|)
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
name|refreshPolicy
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
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
init|)
block|{
name|WriteRequest
operator|.
name|RefreshPolicy
name|deserializedRefreshPolicy
init|=
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|refreshPolicy
argument_list|,
name|deserializedRefreshPolicy
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testParse
specifier|public
name|void
name|testParse
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|refreshPolicyValue
init|=
name|randomFrom
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|refreshPolicyValue
argument_list|,
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|parse
argument_list|(
name|refreshPolicyValue
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseEmpty
specifier|public
name|void
name|testParseEmpty
parameter_list|()
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|,
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|parse
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseUnknown
specifier|public
name|void
name|testParseUnknown
parameter_list|()
throws|throws
name|IOException
block|{
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
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|parse
argument_list|(
literal|"unknown"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unknown value for refresh: [unknown]."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

