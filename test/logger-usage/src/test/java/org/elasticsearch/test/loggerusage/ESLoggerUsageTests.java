begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.loggerusage
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|loggerusage
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|SuppressLoggerChecks
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
name|loggerusage
operator|.
name|ESLoggerUsageChecker
operator|.
name|WrongLoggerUsage
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|ESLoggerUsageTests
specifier|public
class|class
name|ESLoggerUsageTests
extends|extends
name|ESTestCase
block|{
annotation|@
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"https://github.com/elastic/elasticsearch/issues/20243"
argument_list|)
DECL|method|testLoggerUsageChecks
specifier|public
name|void
name|testLoggerUsageChecks
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Method
name|method
range|:
name|getClass
argument_list|()
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|equals
argument_list|(
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"check"
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Checking logger usage for method {}"
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|InputStream
name|classInputStream
init|=
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".class"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|WrongLoggerUsage
argument_list|>
name|errors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ESLoggerUsageChecker
operator|.
name|check
argument_list|(
name|errors
operator|::
name|add
argument_list|,
name|classInputStream
argument_list|,
name|Predicate
operator|.
name|isEqual
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"checkFail"
argument_list|)
condition|)
block|{
name|assertFalse
argument_list|(
literal|"Expected "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" to have wrong Logger usage"
argument_list|,
name|errors
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
literal|"Method "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|" has unexpected Logger usage errors: "
operator|+
name|errors
argument_list|,
name|errors
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|assertTrue
argument_list|(
literal|"only allow methods starting with test or check in this class"
argument_list|,
name|method
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"https://github.com/elastic/elasticsearch/issues/20243"
argument_list|)
DECL|method|testLoggerUsageCheckerCompatibilityWithESLogger
specifier|public
name|void
name|testLoggerUsageCheckerCompatibilityWithESLogger
parameter_list|()
throws|throws
name|NoSuchMethodException
block|{
name|assertThat
argument_list|(
name|ESLoggerUsageChecker
operator|.
name|LOGGER_CLASS
argument_list|,
name|equalTo
argument_list|(
name|Logger
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ESLoggerUsageChecker
operator|.
name|THROWABLE_CLASS
argument_list|,
name|equalTo
argument_list|(
name|Throwable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|varargsMethodCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|Logger
operator|.
name|class
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|isVarArgs
argument_list|()
condition|)
block|{
comment|// check that logger usage checks all varargs methods
name|assertThat
argument_list|(
name|ESLoggerUsageChecker
operator|.
name|LOGGER_METHODS
argument_list|,
name|hasItem
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|varargsMethodCount
operator|++
expr_stmt|;
block|}
block|}
comment|// currently we have two overloaded methods for each of debug, info, ...
comment|// if that changes, we might want to have another look at the usage checker
name|assertThat
argument_list|(
name|varargsMethodCount
argument_list|,
name|equalTo
argument_list|(
name|ESLoggerUsageChecker
operator|.
name|LOGGER_METHODS
operator|.
name|size
argument_list|()
operator|*
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// check that signature is same as we expect in the usage checker
for|for
control|(
name|String
name|methodName
range|:
name|ESLoggerUsageChecker
operator|.
name|LOGGER_METHODS
control|)
block|{
name|assertThat
argument_list|(
name|Logger
operator|.
name|class
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|Object
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Logger
operator|.
name|class
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|Throwable
operator|.
name|class
argument_list|,
name|Object
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|checkNumberOfArguments1
specifier|public
name|void
name|checkNumberOfArguments1
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}"
argument_list|,
literal|"world"
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailNumberOfArguments1
specifier|public
name|void
name|checkFailNumberOfArguments1
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressLoggerChecks
argument_list|(
name|reason
operator|=
literal|"test ignore functionality"
argument_list|)
DECL|method|checkIgnoreWhenAnnotationPresent
specifier|public
name|void
name|checkIgnoreWhenAnnotationPresent
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}"
argument_list|)
expr_stmt|;
block|}
DECL|method|checkNumberOfArguments2
specifier|public
name|void
name|checkNumberOfArguments2
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}, {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|2
argument_list|,
literal|"third argument"
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailNumberOfArguments2
specifier|public
name|void
name|checkFailNumberOfArguments2
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|2
argument_list|,
literal|"third argument"
argument_list|)
expr_stmt|;
block|}
DECL|method|checkNumberOfArguments3
specifier|public
name|void
name|checkNumberOfArguments3
parameter_list|()
block|{
comment|// long argument list (> 5), emits different bytecode
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}, {}, {}, {}, {}, {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|2
argument_list|,
literal|"third argument"
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|6
argument_list|,
operator|new
name|String
argument_list|(
literal|"last arg"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailNumberOfArguments3
specifier|public
name|void
name|checkFailNumberOfArguments3
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}, {}, {}, {}, {}, {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|2
argument_list|,
literal|"third argument"
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
literal|6
argument_list|,
literal|7
argument_list|,
operator|new
name|String
argument_list|(
literal|"last arg"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|checkOrderOfExceptionArgument
specifier|public
name|void
name|checkOrderOfExceptionArgument
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello"
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkOrderOfExceptionArgument1
specifier|public
name|void
name|checkOrderOfExceptionArgument1
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"Hello {}"
argument_list|,
literal|"world"
argument_list|)
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailOrderOfExceptionArgument1
specifier|public
name|void
name|checkFailOrderOfExceptionArgument1
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}"
argument_list|,
literal|"world"
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkOrderOfExceptionArgument2
specifier|public
name|void
name|checkOrderOfExceptionArgument2
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"Hello {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|42
argument_list|)
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailOrderOfExceptionArgument2
specifier|public
name|void
name|checkFailOrderOfExceptionArgument2
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hello {}, {}"
argument_list|,
literal|"world"
argument_list|,
literal|42
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkNonConstantMessageWithZeroArguments
specifier|public
name|void
name|checkNonConstantMessageWithZeroArguments
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|b
argument_list|)
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailNonConstantMessageWithArguments
specifier|public
name|void
name|checkFailNonConstantMessageWithArguments
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|b
argument_list|)
argument_list|,
literal|42
argument_list|)
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkComplexUsage
specifier|public
name|void
name|checkComplexUsage
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Hello {}, {}"
decl_stmt|;
name|Object
index|[]
name|args
init|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|42
block|}
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|message
operator|=
literal|"also two args {}{}"
expr_stmt|;
name|args
operator|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|43
block|}
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
name|message
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailComplexUsage1
specifier|public
name|void
name|checkFailComplexUsage1
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Hello {}, {}"
decl_stmt|;
name|Object
index|[]
name|args
init|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|42
block|}
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|message
operator|=
literal|"just one arg {}"
expr_stmt|;
name|args
operator|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|43
block|}
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
name|message
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|checkFailComplexUsage2
specifier|public
name|void
name|checkFailComplexUsage2
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Hello {}, {}"
decl_stmt|;
name|Object
index|[]
name|args
init|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|42
block|}
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|message
operator|=
literal|"also two args {}{}"
expr_stmt|;
name|args
operator|=
operator|new
name|Object
index|[]
block|{
literal|"world"
block|,
literal|43
block|,
literal|"another argument"
block|}
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
name|message
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

