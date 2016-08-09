begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|test
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
name|Level
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
name|Logger
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
name|logging
operator|.
name|Loggers
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|junit
operator|.
name|listeners
operator|.
name|LoggingListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Result
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|LoggingListenerTests
specifier|public
class|class
name|LoggingListenerTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCustomLevelPerMethod
specifier|public
name|void
name|testCustomLevelPerMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|LoggingListener
name|loggingListener
init|=
operator|new
name|LoggingListener
argument_list|()
decl_stmt|;
name|Description
name|suiteDescription
init|=
name|Description
operator|.
name|createSuiteDescription
argument_list|(
name|TestClass
operator|.
name|class
argument_list|)
decl_stmt|;
name|Logger
name|xyzLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"xyz"
argument_list|)
decl_stmt|;
name|Logger
name|abcLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|,
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunStarted
argument_list|(
name|suiteDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|Method
name|method
init|=
name|TestClass
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"annotatedTestMethod"
argument_list|)
decl_stmt|;
name|TestLogging
name|annotation
init|=
name|method
operator|.
name|getAnnotation
argument_list|(
name|TestLogging
operator|.
name|class
argument_list|)
decl_stmt|;
name|Description
name|testDescription
init|=
name|Description
operator|.
name|createTestDescription
argument_list|(
name|LoggingListenerTests
operator|.
name|class
argument_list|,
literal|"annotatedTestMethod"
argument_list|,
name|annotation
argument_list|)
decl_stmt|;
name|loggingListener
operator|.
name|testStarted
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testFinished
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunFinished
argument_list|(
operator|new
name|Result
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCustomLevelPerClass
specifier|public
name|void
name|testCustomLevelPerClass
parameter_list|()
throws|throws
name|Exception
block|{
name|LoggingListener
name|loggingListener
init|=
operator|new
name|LoggingListener
argument_list|()
decl_stmt|;
name|Description
name|suiteDescription
init|=
name|Description
operator|.
name|createSuiteDescription
argument_list|(
name|AnnotatedTestClass
operator|.
name|class
argument_list|)
decl_stmt|;
name|Logger
name|abcLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|Logger
name|xyzLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"xyz"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunStarted
argument_list|(
name|suiteDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|Description
name|testDescription
init|=
name|Description
operator|.
name|createTestDescription
argument_list|(
name|LoggingListenerTests
operator|.
name|class
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|loggingListener
operator|.
name|testStarted
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testFinished
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunFinished
argument_list|(
operator|new
name|Result
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCustomLevelPerClassAndPerMethod
specifier|public
name|void
name|testCustomLevelPerClassAndPerMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|LoggingListener
name|loggingListener
init|=
operator|new
name|LoggingListener
argument_list|()
decl_stmt|;
name|Description
name|suiteDescription
init|=
name|Description
operator|.
name|createSuiteDescription
argument_list|(
name|AnnotatedTestClass
operator|.
name|class
argument_list|)
decl_stmt|;
name|Logger
name|abcLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|Logger
name|xyzLogger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
literal|"xyz"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunStarted
argument_list|(
name|suiteDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|Method
name|method
init|=
name|TestClass
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"annotatedTestMethod"
argument_list|)
decl_stmt|;
name|TestLogging
name|annotation
init|=
name|method
operator|.
name|getAnnotation
argument_list|(
name|TestLogging
operator|.
name|class
argument_list|)
decl_stmt|;
name|Description
name|testDescription
init|=
name|Description
operator|.
name|createTestDescription
argument_list|(
name|LoggingListenerTests
operator|.
name|class
argument_list|,
literal|"annotatedTestMethod"
argument_list|,
name|annotation
argument_list|)
decl_stmt|;
name|loggingListener
operator|.
name|testStarted
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testFinished
argument_list|(
name|testDescription
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|Method
name|method2
init|=
name|TestClass
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"annotatedTestMethod2"
argument_list|)
decl_stmt|;
name|TestLogging
name|annotation2
init|=
name|method2
operator|.
name|getAnnotation
argument_list|(
name|TestLogging
operator|.
name|class
argument_list|)
decl_stmt|;
name|Description
name|testDescription2
init|=
name|Description
operator|.
name|createTestDescription
argument_list|(
name|LoggingListenerTests
operator|.
name|class
argument_list|,
literal|"annotatedTestMethod2"
argument_list|,
name|annotation2
argument_list|)
decl_stmt|;
name|loggingListener
operator|.
name|testStarted
argument_list|(
name|testDescription2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|TRACE
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testFinished
argument_list|(
name|testDescription2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|WARN
argument_list|)
argument_list|)
expr_stmt|;
name|loggingListener
operator|.
name|testRunFinished
argument_list|(
operator|new
name|Result
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|xyzLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|abcLogger
operator|.
name|getLevel
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * dummy class used to create a junit suite description that has the @TestLogging annotation      */
annotation|@
name|TestLogging
argument_list|(
literal|"abc:WARN"
argument_list|)
DECL|class|AnnotatedTestClass
specifier|public
specifier|static
class|class
name|AnnotatedTestClass
block|{      }
comment|/**      * dummy class used to create a junit suite description that doesn't have the @TestLogging annotation, but its test methods have it      */
DECL|class|TestClass
specifier|public
specifier|static
class|class
name|TestClass
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
annotation|@
name|TestLogging
argument_list|(
literal|"xyz:TRACE"
argument_list|)
DECL|method|annotatedTestMethod
specifier|public
name|void
name|annotatedTestMethod
parameter_list|()
block|{}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
annotation|@
name|TestLogging
argument_list|(
literal|"abc:TRACE,xyz:DEBUG"
argument_list|)
DECL|method|annotatedTestMethod2
specifier|public
name|void
name|annotatedTestMethod2
parameter_list|()
block|{}
block|}
block|}
end_class

end_unit

