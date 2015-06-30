begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mock
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mock
package|;
end_package

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|InOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|MockSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|MockingDetails
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|ReturnValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|DeprecatedOngoingStubbing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|OngoingStubbing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Stubber
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|VoidMethodStubbable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|verification
operator|.
name|VerificationMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|verification
operator|.
name|VerificationWithTimeout
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_comment
comment|/**  * Wraps Mockito API with calls to AccessController.  *<p>  * This is useful if you want to mock in a securitymanager environment,  * but contain the permissions to only mocking test libraries.  *<p>  * Instead of:  *<pre>  * grant {  *   permission java.lang.RuntimePermission "reflectionFactoryAccess";  * };  *</pre>  * You can just change maven dependencies to use securemock.jar, and then:  *<pre>  * grant codeBase "/url/to/securemock.jar" {  *   permission java.lang.RuntimePermission "reflectionFactoryAccess";  * };  *</pre>  */
end_comment

begin_class
DECL|class|Mockito
specifier|public
class|class
name|Mockito
extends|extends
name|Matchers
block|{
DECL|field|RETURNS_DEFAULTS
specifier|public
specifier|static
specifier|final
name|Answer
argument_list|<
name|Object
argument_list|>
name|RETURNS_DEFAULTS
init|=
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|RETURNS_DEFAULTS
decl_stmt|;
DECL|field|RETURNS_SMART_NULLS
specifier|public
specifier|static
specifier|final
name|Answer
argument_list|<
name|Object
argument_list|>
name|RETURNS_SMART_NULLS
init|=
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|RETURNS_SMART_NULLS
decl_stmt|;
DECL|field|RETURNS_MOCKS
specifier|public
specifier|static
specifier|final
name|Answer
argument_list|<
name|Object
argument_list|>
name|RETURNS_MOCKS
init|=
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|RETURNS_MOCKS
decl_stmt|;
DECL|field|RETURNS_DEEP_STUBS
specifier|public
specifier|static
specifier|final
name|Answer
argument_list|<
name|Object
argument_list|>
name|RETURNS_DEEP_STUBS
init|=
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|RETURNS_DEEP_STUBS
decl_stmt|;
DECL|field|CALLS_REAL_METHODS
specifier|public
specifier|static
specifier|final
name|Answer
argument_list|<
name|Object
argument_list|>
name|CALLS_REAL_METHODS
init|=
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|CALLS_REAL_METHODS
decl_stmt|;
DECL|method|mock
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|mock
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|classToMock
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
argument_list|(
name|classToMock
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|mock
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|mock
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|classToMock
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
argument_list|(
name|classToMock
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|mockingDetails
specifier|public
specifier|static
name|MockingDetails
name|mockingDetails
parameter_list|(
specifier|final
name|Object
name|toInspect
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|MockingDetails
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MockingDetails
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mockingDetails
argument_list|(
name|toInspect
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
DECL|method|mock
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|mock
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|classToMock
parameter_list|,
specifier|final
name|ReturnValues
name|returnValues
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
argument_list|(
name|classToMock
argument_list|,
name|returnValues
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|mock
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|mock
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|classToMock
parameter_list|,
specifier|final
name|Answer
name|defaultAnswer
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
argument_list|(
name|classToMock
argument_list|,
name|defaultAnswer
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|mock
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|mock
parameter_list|(
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|classToMock
parameter_list|,
specifier|final
name|MockSettings
name|mockSettings
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
argument_list|(
name|classToMock
argument_list|,
name|mockSettings
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|spy
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|spy
parameter_list|(
specifier|final
name|T
name|object
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|spy
argument_list|(
name|object
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|stub
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|DeprecatedOngoingStubbing
argument_list|<
name|T
argument_list|>
name|stub
parameter_list|(
specifier|final
name|T
name|methodCall
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|DeprecatedOngoingStubbing
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DeprecatedOngoingStubbing
argument_list|<
name|T
argument_list|>
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|stub
argument_list|(
name|methodCall
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|when
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|OngoingStubbing
argument_list|<
name|T
argument_list|>
name|when
parameter_list|(
specifier|final
name|T
name|methodCall
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|OngoingStubbing
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|OngoingStubbing
argument_list|<
name|T
argument_list|>
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
argument_list|(
name|methodCall
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|verify
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|verify
parameter_list|(
specifier|final
name|T
name|mock
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
argument_list|(
name|mock
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|verify
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|verify
parameter_list|(
specifier|final
name|T
name|mock
parameter_list|,
specifier|final
name|VerificationMode
name|mode
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
argument_list|(
name|mock
argument_list|,
name|mode
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|reset
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|void
name|reset
parameter_list|(
specifier|final
name|T
modifier|...
name|mocks
parameter_list|)
block|{
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|reset
argument_list|(
name|mocks
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|verifyNoMoreInteractions
specifier|public
specifier|static
name|void
name|verifyNoMoreInteractions
parameter_list|(
specifier|final
name|Object
modifier|...
name|mocks
parameter_list|)
block|{
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyNoMoreInteractions
argument_list|(
name|mocks
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|verifyZeroInteractions
specifier|public
specifier|static
name|void
name|verifyZeroInteractions
parameter_list|(
specifier|final
name|Object
modifier|...
name|mocks
parameter_list|)
block|{
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyZeroInteractions
argument_list|(
name|mocks
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Deprecated
DECL|method|stubVoid
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|VoidMethodStubbable
argument_list|<
name|T
argument_list|>
name|stubVoid
parameter_list|(
specifier|final
name|T
name|mock
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VoidMethodStubbable
argument_list|<
name|T
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VoidMethodStubbable
argument_list|<
name|T
argument_list|>
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|stubVoid
argument_list|(
name|mock
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doThrow
specifier|public
specifier|static
name|Stubber
name|doThrow
parameter_list|(
specifier|final
name|Throwable
name|toBeThrown
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doThrow
argument_list|(
name|toBeThrown
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doThrow
specifier|public
specifier|static
name|Stubber
name|doThrow
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|toBeThrown
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doThrow
argument_list|(
name|toBeThrown
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doCallRealMethod
specifier|public
specifier|static
name|Stubber
name|doCallRealMethod
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doCallRealMethod
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doAnswer
specifier|public
specifier|static
name|Stubber
name|doAnswer
parameter_list|(
specifier|final
name|Answer
name|answer
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
argument_list|(
name|answer
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doNothing
specifier|public
specifier|static
name|Stubber
name|doNothing
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doNothing
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|doReturn
specifier|public
specifier|static
name|Stubber
name|doReturn
parameter_list|(
specifier|final
name|Object
name|toBeReturned
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Stubber
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Stubber
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doReturn
argument_list|(
name|toBeReturned
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|inOrder
specifier|public
specifier|static
name|InOrder
name|inOrder
parameter_list|(
specifier|final
name|Object
modifier|...
name|mocks
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|InOrder
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InOrder
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|inOrder
argument_list|(
name|mocks
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|ignoreStubs
specifier|public
specifier|static
name|Object
index|[]
name|ignoreStubs
parameter_list|(
specifier|final
name|Object
modifier|...
name|mocks
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|ignoreStubs
argument_list|(
name|mocks
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|times
specifier|public
specifier|static
name|VerificationMode
name|times
parameter_list|(
specifier|final
name|int
name|wantedNumberOfInvocations
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
argument_list|(
name|wantedNumberOfInvocations
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|never
specifier|public
specifier|static
name|VerificationMode
name|never
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|atLeastOnce
specifier|public
specifier|static
name|VerificationMode
name|atLeastOnce
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|atLeastOnce
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|atLeast
specifier|public
specifier|static
name|VerificationMode
name|atLeast
parameter_list|(
specifier|final
name|int
name|minNumberOfInvocations
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|atLeast
argument_list|(
name|minNumberOfInvocations
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|atMost
specifier|public
specifier|static
name|VerificationMode
name|atMost
parameter_list|(
specifier|final
name|int
name|maxNumberOfInvocations
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|atMost
argument_list|(
name|maxNumberOfInvocations
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|calls
specifier|public
specifier|static
name|VerificationMode
name|calls
parameter_list|(
specifier|final
name|int
name|wantedNumberOfInvocations
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|calls
argument_list|(
name|wantedNumberOfInvocations
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|only
specifier|public
specifier|static
name|VerificationMode
name|only
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationMode
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationMode
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|only
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|timeout
specifier|public
specifier|static
name|VerificationWithTimeout
name|timeout
parameter_list|(
specifier|final
name|int
name|millis
parameter_list|)
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|VerificationWithTimeout
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|VerificationWithTimeout
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|timeout
argument_list|(
name|millis
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|validateMockitoUsage
specifier|public
specifier|static
name|void
name|validateMockitoUsage
parameter_list|()
block|{
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|validateMockitoUsage
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|withSettings
specifier|public
specifier|static
name|MockSettings
name|withSettings
parameter_list|()
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|MockSettings
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MockSettings
name|run
parameter_list|()
block|{
return|return
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|withSettings
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
end_class

end_unit
