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
name|action
operator|.
name|ActionListener
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

begin_class
DECL|class|ContextPreservingActionListenerTests
specifier|public
class|class
name|ContextPreservingActionListenerTests
extends|extends
name|ESTestCase
block|{
DECL|method|testOriginalContextIsPreservedAfterOnResponse
specifier|public
name|void
name|testOriginalContextIsPreservedAfterOnResponse
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
specifier|final
name|boolean
name|nonEmptyContext
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|nonEmptyContext
condition|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"not empty"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|ContextPreservingActionListener
argument_list|<
name|Void
argument_list|>
name|actionListener
decl_stmt|;
try|try
init|(
name|ThreadContext
operator|.
name|StoredContext
name|ignore
init|=
name|threadContext
operator|.
name|stashContext
argument_list|()
init|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|actionListener
operator|=
operator|new
name|ContextPreservingActionListener
argument_list|<>
argument_list|(
name|threadContext
operator|.
name|newRestorableContext
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Void
name|aVoid
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"onFailure shouldn't be called"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
name|actionListener
operator|.
name|onResponse
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testOriginalContextIsPreservedAfterOnFailure
specifier|public
name|void
name|testOriginalContextIsPreservedAfterOnFailure
parameter_list|()
throws|throws
name|Exception
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
specifier|final
name|boolean
name|nonEmptyContext
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|nonEmptyContext
condition|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"not empty"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|ContextPreservingActionListener
argument_list|<
name|Void
argument_list|>
name|actionListener
decl_stmt|;
try|try
init|(
name|ThreadContext
operator|.
name|StoredContext
name|ignore
init|=
name|threadContext
operator|.
name|stashContext
argument_list|()
init|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|actionListener
operator|=
operator|new
name|ContextPreservingActionListener
argument_list|<>
argument_list|(
name|threadContext
operator|.
name|newRestorableContext
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Void
name|aVoid
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"onResponse shouldn't be called"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
name|actionListener
operator|.
name|onFailure
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testOriginalContextIsWhenListenerThrows
specifier|public
name|void
name|testOriginalContextIsWhenListenerThrows
parameter_list|()
throws|throws
name|Exception
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
specifier|final
name|boolean
name|nonEmptyContext
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|nonEmptyContext
condition|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"not empty"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|ContextPreservingActionListener
argument_list|<
name|Void
argument_list|>
name|actionListener
decl_stmt|;
try|try
init|(
name|ThreadContext
operator|.
name|StoredContext
name|ignore
init|=
name|threadContext
operator|.
name|stashContext
argument_list|()
init|)
block|{
name|threadContext
operator|.
name|putHeader
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|actionListener
operator|=
operator|new
name|ContextPreservingActionListener
argument_list|<>
argument_list|(
name|threadContext
operator|.
name|newRestorableContext
argument_list|(
literal|true
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Void
name|aVoid
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"onResponse called"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"onFailure called"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
name|RuntimeException
name|e
init|=
name|expectThrows
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|actionListener
operator|.
name|onResponse
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"onResponse called"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|actionListener
operator|.
name|onFailure
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"onFailure called"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nonEmptyContext
condition|?
literal|"value"
else|:
literal|null
argument_list|,
name|threadContext
operator|.
name|getHeader
argument_list|(
literal|"not empty"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

