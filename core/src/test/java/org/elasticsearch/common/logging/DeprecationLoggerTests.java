begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.logging
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadContext
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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  * Tests {@link DeprecationLogger}  */
end_comment

begin_class
DECL|class|DeprecationLoggerTests
specifier|public
class|class
name|DeprecationLoggerTests
extends|extends
name|ESTestCase
block|{
DECL|field|logger
specifier|private
specifier|final
name|DeprecationLogger
name|logger
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|testAddsHeaderWithThreadContext
specifier|public
name|void
name|testAddsHeaderWithThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|msg
init|=
literal|"A simple message [{}]"
decl_stmt|;
name|String
name|param
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|String
name|formatted
init|=
name|LoggerMessageFormat
operator|.
name|format
argument_list|(
name|msg
argument_list|,
operator|(
name|Object
operator|)
name|param
argument_list|)
decl_stmt|;
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
name|Set
argument_list|<
name|ThreadContext
argument_list|>
name|threadContexts
init|=
name|Collections
operator|.
name|singleton
argument_list|(
name|threadContext
argument_list|)
decl_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|threadContexts
argument_list|,
name|msg
argument_list|,
name|param
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|responseHeaders
init|=
name|threadContext
operator|.
name|getResponseHeaders
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|responseHeaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|formatted
argument_list|,
name|responseHeaders
operator|.
name|get
argument_list|(
name|DeprecationLogger
operator|.
name|DEPRECATION_HEADER
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAddsCombinedHeaderWithThreadContext
specifier|public
name|void
name|testAddsCombinedHeaderWithThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|msg
init|=
literal|"A simple message [{}]"
decl_stmt|;
name|String
name|param
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|String
name|formatted
init|=
name|LoggerMessageFormat
operator|.
name|format
argument_list|(
name|msg
argument_list|,
operator|(
name|Object
operator|)
name|param
argument_list|)
decl_stmt|;
name|String
name|formatted2
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
name|Set
argument_list|<
name|ThreadContext
argument_list|>
name|threadContexts
init|=
name|Collections
operator|.
name|singleton
argument_list|(
name|threadContext
argument_list|)
decl_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|threadContexts
argument_list|,
name|msg
argument_list|,
name|param
argument_list|)
expr_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|threadContexts
argument_list|,
name|formatted2
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|responseHeaders
init|=
name|threadContext
operator|.
name|getResponseHeaders
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|responseHeaders
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|responses
init|=
name|responseHeaders
operator|.
name|get
argument_list|(
name|DeprecationLogger
operator|.
name|DEPRECATION_HEADER
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|responses
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|formatted
argument_list|,
name|responses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|formatted2
argument_list|,
name|responses
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCanRemoveThreadContext
specifier|public
name|void
name|testCanRemoveThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|expected
init|=
literal|"testCanRemoveThreadContext"
decl_stmt|;
specifier|final
name|String
name|unexpected
init|=
literal|"testCannotRemoveThreadContext"
decl_stmt|;
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
comment|// NOTE: by adding it to the logger, we allow any concurrent test to write to it (from their own threads)
name|DeprecationLogger
operator|.
name|setThreadContext
argument_list|(
name|threadContext
argument_list|)
expr_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|responseHeaders
init|=
name|threadContext
operator|.
name|getResponseHeaders
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|responses
init|=
name|responseHeaders
operator|.
name|get
argument_list|(
name|DeprecationLogger
operator|.
name|DEPRECATION_HEADER
argument_list|)
decl_stmt|;
comment|// ensure it works (note: concurrent tests may be adding to it, but in different threads, so it should have no impact)
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasSize
argument_list|(
name|atLeast
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasItem
argument_list|(
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DeprecationLogger
operator|.
name|removeThreadContext
argument_list|(
name|threadContext
argument_list|)
expr_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|unexpected
argument_list|)
expr_stmt|;
name|responseHeaders
operator|=
name|threadContext
operator|.
name|getResponseHeaders
argument_list|()
expr_stmt|;
name|responses
operator|=
name|responseHeaders
operator|.
name|get
argument_list|(
name|DeprecationLogger
operator|.
name|DEPRECATION_HEADER
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasSize
argument_list|(
name|atLeast
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasItem
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|not
argument_list|(
name|hasItem
argument_list|(
name|unexpected
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIgnoresClosedThreadContext
specifier|public
name|void
name|testIgnoresClosedThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ThreadContext
argument_list|>
name|threadContexts
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|threadContexts
operator|.
name|add
argument_list|(
name|threadContext
argument_list|)
expr_stmt|;
name|threadContext
operator|.
name|close
argument_list|()
expr_stmt|;
name|logger
operator|.
name|deprecated
argument_list|(
name|threadContexts
argument_list|,
literal|"Ignored logger message"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|threadContexts
operator|.
name|contains
argument_list|(
name|threadContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSafeWithoutThreadContext
specifier|public
name|void
name|testSafeWithoutThreadContext
parameter_list|()
block|{
name|logger
operator|.
name|deprecated
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
literal|"Ignored"
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailsWithoutThreadContextSet
specifier|public
name|void
name|testFailsWithoutThreadContextSet
parameter_list|()
block|{
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|logger
operator|.
name|deprecated
argument_list|(
operator|(
name|Set
argument_list|<
name|ThreadContext
argument_list|>
operator|)
literal|null
argument_list|,
literal|"Does not explode"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailsWhenDoubleSettingSameThreadContext
specifier|public
name|void
name|testFailsWhenDoubleSettingSameThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
name|DeprecationLogger
operator|.
name|setThreadContext
argument_list|(
name|threadContext
argument_list|)
expr_stmt|;
try|try
block|{
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|DeprecationLogger
operator|.
name|setThreadContext
argument_list|(
name|threadContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// cleanup after ourselves
name|DeprecationLogger
operator|.
name|removeThreadContext
argument_list|(
name|threadContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testFailsWhenRemovingUnknownThreadContext
specifier|public
name|void
name|testFailsWhenRemovingUnknownThreadContext
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
name|ThreadContext
name|threadContext
init|=
operator|new
name|ThreadContext
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|DeprecationLogger
operator|.
name|removeThreadContext
argument_list|(
name|threadContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
