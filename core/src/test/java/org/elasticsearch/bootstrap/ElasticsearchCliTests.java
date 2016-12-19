begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|ExitCodes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|function
operator|.
name|Consumer
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
name|containsString
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|allOf
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
name|hasEntry
import|;
end_import

begin_class
DECL|class|ElasticsearchCliTests
specifier|public
class|class
name|ElasticsearchCliTests
extends|extends
name|ESElasticsearchCliTestCase
block|{
DECL|method|testVersion
specifier|public
name|void
name|testVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"-V"
argument_list|,
literal|"-d"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"-V"
argument_list|,
literal|"--daemonize"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"-V"
argument_list|,
literal|"-p"
argument_list|,
literal|"/tmp/pid"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"-V"
argument_list|,
literal|"--pidfile"
argument_list|,
literal|"/tmp/pid"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"-d"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"--daemonize"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"-p"
argument_list|,
literal|"/tmp/pid"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"--pidfile"
argument_list|,
literal|"/tmp/pid"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"-q"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
argument_list|(
literal|"--version"
argument_list|,
literal|"--quiet"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsReturned
argument_list|(
literal|"-V"
argument_list|)
expr_stmt|;
name|runTestThatVersionIsReturned
argument_list|(
literal|"--version"
argument_list|)
expr_stmt|;
block|}
DECL|method|runTestThatVersionIsMutuallyExclusiveToOtherOptions
specifier|private
name|void
name|runTestThatVersionIsMutuallyExclusiveToOtherOptions
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTestVersion
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|allOf
argument_list|(
name|containsString
argument_list|(
literal|"ERROR:"
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"are unavailable given other options on the command line"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|runTestThatVersionIsReturned
specifier|private
name|void
name|runTestThatVersionIsReturned
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTestVersion
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
name|output
lambda|->
block|{
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Version: "
operator|+
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Build: "
operator|+
name|Build
operator|.
name|CURRENT
operator|.
name|shortHash
argument_list|()
operator|+
literal|"/"
operator|+
name|Build
operator|.
name|CURRENT
operator|.
name|date
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"JVM: "
operator|+
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|version
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|runTestVersion
specifier|private
name|void
name|runTestVersion
parameter_list|(
name|int
name|expectedStatus
parameter_list|,
name|Consumer
argument_list|<
name|String
argument_list|>
name|outputConsumer
parameter_list|,
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|expectedStatus
argument_list|,
literal|false
argument_list|,
name|outputConsumer
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|testPositionalArgs
specifier|public
name|void
name|testPositionalArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Positional arguments not allowed, found [foo]"
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Positional arguments not allowed, found [foo, bar]"
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Positional arguments not allowed, found [foo]"
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
literal|"-E"
argument_list|,
literal|"foo=bar"
argument_list|,
literal|"foo"
argument_list|,
literal|"-E"
argument_list|,
literal|"baz=qux"
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatPidFileCanBeConfigured
specifier|public
name|void
name|testThatPidFileCanBeConfigured
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|tmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|pidFile
init|=
name|tmpDir
operator|.
name|resolve
argument_list|(
literal|"pid"
argument_list|)
decl_stmt|;
name|runPidFileTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Option p/pidfile requires an argument"
argument_list|)
argument_list|)
argument_list|,
name|pidFile
argument_list|,
literal|"-p"
argument_list|)
expr_stmt|;
name|runPidFileTest
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
literal|true
argument_list|,
name|output
lambda|->
block|{}
argument_list|,
name|pidFile
argument_list|,
literal|"-p"
argument_list|,
name|pidFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|runPidFileTest
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
literal|true
argument_list|,
name|output
lambda|->
block|{}
argument_list|,
name|pidFile
argument_list|,
literal|"--pidfile"
argument_list|,
name|tmpDir
operator|.
name|toString
argument_list|()
operator|+
literal|"/pid"
argument_list|)
expr_stmt|;
block|}
DECL|method|runPidFileTest
specifier|private
name|void
name|runPidFileTest
parameter_list|(
specifier|final
name|int
name|expectedStatus
parameter_list|,
specifier|final
name|boolean
name|expectedInit
parameter_list|,
name|Consumer
argument_list|<
name|String
argument_list|>
name|outputConsumer
parameter_list|,
name|Path
name|expectedPidFile
parameter_list|,
specifier|final
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|expectedStatus
argument_list|,
name|expectedInit
argument_list|,
name|outputConsumer
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
name|assertThat
argument_list|(
name|pidFile
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedPidFile
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatParsingDaemonizeWorks
specifier|public
name|void
name|testThatParsingDaemonizeWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|runDaemonizeTest
argument_list|(
literal|true
argument_list|,
literal|"-d"
argument_list|)
expr_stmt|;
name|runDaemonizeTest
argument_list|(
literal|true
argument_list|,
literal|"--daemonize"
argument_list|)
expr_stmt|;
name|runDaemonizeTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|runDaemonizeTest
specifier|private
name|void
name|runDaemonizeTest
parameter_list|(
specifier|final
name|boolean
name|expectedDaemonize
parameter_list|,
specifier|final
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
literal|true
argument_list|,
name|output
lambda|->
block|{}
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
name|assertThat
argument_list|(
name|foreground
argument_list|,
name|equalTo
argument_list|(
operator|!
name|expectedDaemonize
argument_list|)
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatParsingQuietOptionWorks
specifier|public
name|void
name|testThatParsingQuietOptionWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|runQuietTest
argument_list|(
literal|true
argument_list|,
literal|"-q"
argument_list|)
expr_stmt|;
name|runQuietTest
argument_list|(
literal|true
argument_list|,
literal|"--quiet"
argument_list|)
expr_stmt|;
name|runQuietTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|runQuietTest
specifier|private
name|void
name|runQuietTest
parameter_list|(
specifier|final
name|boolean
name|expectedQuiet
parameter_list|,
specifier|final
name|String
modifier|...
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
literal|true
argument_list|,
name|output
lambda|->
block|{}
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
name|assertThat
argument_list|(
name|quiet
argument_list|,
name|equalTo
argument_list|(
name|expectedQuiet
argument_list|)
argument_list|)
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|testElasticsearchSettings
specifier|public
name|void
name|testElasticsearchSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
literal|true
argument_list|,
name|output
lambda|->
block|{}
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|settings
init|=
name|esSettings
operator|.
name|getAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|settings
argument_list|,
name|hasEntry
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|settings
argument_list|,
name|hasEntry
argument_list|(
literal|"baz"
argument_list|,
literal|"qux"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
literal|"-Efoo=bar"
argument_list|,
literal|"-E"
argument_list|,
literal|"baz=qux"
argument_list|)
expr_stmt|;
block|}
DECL|method|testElasticsearchSettingCanNotBeEmpty
specifier|public
name|void
name|testElasticsearchSettingCanNotBeEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"Setting [foo] must not be empty"
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
literal|"-E"
argument_list|,
literal|"foo="
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnknownOption
specifier|public
name|void
name|testUnknownOption
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|false
argument_list|,
name|output
lambda|->
name|assertThat
argument_list|(
name|output
argument_list|,
name|containsString
argument_list|(
literal|"network.host is not a recognized option"
argument_list|)
argument_list|)
argument_list|,
parameter_list|(
name|foreground
parameter_list|,
name|pidFile
parameter_list|,
name|quiet
parameter_list|,
name|esSettings
parameter_list|)
lambda|->
block|{}
argument_list|,
literal|"--network.host"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

