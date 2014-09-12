begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|Name
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ParametersFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|settings
operator|.
name|ImmutableSettings
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
name|test
operator|.
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|rest
operator|.
name|client
operator|.
name|RestException
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
name|rest
operator|.
name|parser
operator|.
name|RestTestParseException
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
name|rest
operator|.
name|parser
operator|.
name|RestTestSuiteParser
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
name|rest
operator|.
name|section
operator|.
name|*
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
name|rest
operator|.
name|spec
operator|.
name|RestSpec
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
name|rest
operator|.
name|support
operator|.
name|FileUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|nio
operator|.
name|file
operator|.
name|FileSystems
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
name|PathMatcher
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
name|Paths
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

begin_comment
comment|/**  * Runs the clients test suite against an elasticsearch cluster.  */
end_comment

begin_comment
comment|//tests distribution disabled for now since it causes reporting problems,
end_comment

begin_comment
comment|// due to the non unique suite name
end_comment

begin_comment
comment|//@ReplicateOnEachVm
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|randomDynamicTemplates
operator|=
literal|false
argument_list|)
DECL|class|ElasticsearchRestTests
specifier|public
class|class
name|ElasticsearchRestTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
comment|/**      * Property that allows to control whether the REST tests need to be run (default) or not (false)      */
DECL|field|REST_TESTS
specifier|public
specifier|static
specifier|final
name|String
name|REST_TESTS
init|=
literal|"tests.rest"
decl_stmt|;
comment|/**      * Property that allows to control which REST tests get run. Supports comma separated list of tests      * or directories that contain tests e.g. -Dtests.rest.suite=index,get,create/10_with_id      */
DECL|field|REST_TESTS_SUITE
specifier|public
specifier|static
specifier|final
name|String
name|REST_TESTS_SUITE
init|=
literal|"tests.rest.suite"
decl_stmt|;
comment|/**      * Property that allows to blacklist some of the REST tests based on a comma separated list of globs      * e.g. -Dtests.rest.blacklist=get/10_basic/*      */
DECL|field|REST_TESTS_BLACKLIST
specifier|public
specifier|static
specifier|final
name|String
name|REST_TESTS_BLACKLIST
init|=
literal|"tests.rest.blacklist"
decl_stmt|;
comment|/**      * Property that allows to control where the REST spec files need to be loaded from      */
DECL|field|REST_TESTS_SPEC
specifier|public
specifier|static
specifier|final
name|String
name|REST_TESTS_SPEC
init|=
literal|"tests.rest.spec"
decl_stmt|;
DECL|field|DEFAULT_TESTS_PATH
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_TESTS_PATH
init|=
literal|"/rest-api-spec/test"
decl_stmt|;
DECL|field|DEFAULT_SPEC_PATH
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_SPEC_PATH
init|=
literal|"/rest-api-spec/api"
decl_stmt|;
DECL|field|PATHS_SEPARATOR
specifier|private
specifier|static
specifier|final
name|String
name|PATHS_SEPARATOR
init|=
literal|","
decl_stmt|;
DECL|field|blacklistPathMatchers
specifier|private
specifier|final
name|PathMatcher
index|[]
name|blacklistPathMatchers
decl_stmt|;
DECL|field|restTestExecutionContext
specifier|private
specifier|static
name|RestTestExecutionContext
name|restTestExecutionContext
decl_stmt|;
comment|//private static final int JVM_COUNT = systemPropertyAsInt(SysGlobals.CHILDVM_SYSPROP_JVM_COUNT, 1);
comment|//private static final int CURRENT_JVM_ID = systemPropertyAsInt(SysGlobals.CHILDVM_SYSPROP_JVM_ID, 0);
DECL|field|testCandidate
specifier|private
specifier|final
name|RestTestCandidate
name|testCandidate
decl_stmt|;
DECL|method|ElasticsearchRestTests
specifier|public
name|ElasticsearchRestTests
parameter_list|(
annotation|@
name|Name
argument_list|(
literal|"yaml"
argument_list|)
name|RestTestCandidate
name|testCandidate
parameter_list|)
block|{
name|this
operator|.
name|testCandidate
operator|=
name|testCandidate
expr_stmt|;
name|String
index|[]
name|blacklist
init|=
name|resolvePathsProperty
argument_list|(
name|REST_TESTS_BLACKLIST
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|blacklist
operator|!=
literal|null
condition|)
block|{
name|blacklistPathMatchers
operator|=
operator|new
name|PathMatcher
index|[
name|blacklist
operator|.
name|length
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|glob
range|:
name|blacklist
control|)
block|{
name|blacklistPathMatchers
index|[
name|i
operator|++
index|]
operator|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
operator|.
name|getPathMatcher
argument_list|(
literal|"glob:"
operator|+
name|glob
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|blacklistPathMatchers
operator|=
operator|new
name|PathMatcher
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
annotation|@
name|ParametersFactory
DECL|method|parameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
throws|throws
name|IOException
throws|,
name|RestTestParseException
block|{
name|List
argument_list|<
name|RestTestCandidate
argument_list|>
name|restTestCandidates
init|=
name|collectTestCandidates
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|objects
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|RestTestCandidate
name|restTestCandidate
range|:
name|restTestCandidates
control|)
block|{
name|objects
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|restTestCandidate
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|objects
return|;
block|}
DECL|method|collectTestCandidates
specifier|private
specifier|static
name|List
argument_list|<
name|RestTestCandidate
argument_list|>
name|collectTestCandidates
parameter_list|()
throws|throws
name|RestTestParseException
throws|,
name|IOException
block|{
name|String
index|[]
name|paths
init|=
name|resolvePathsProperty
argument_list|(
name|REST_TESTS_SUITE
argument_list|,
name|DEFAULT_TESTS_PATH
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|File
argument_list|>
argument_list|>
name|yamlSuites
init|=
name|FileUtils
operator|.
name|findYamlSuites
argument_list|(
name|DEFAULT_TESTS_PATH
argument_list|,
name|paths
argument_list|)
decl_stmt|;
comment|//yaml suites are grouped by directory (effectively by api)
name|List
argument_list|<
name|String
argument_list|>
name|apis
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|yamlSuites
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RestTestCandidate
argument_list|>
name|testCandidates
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|RestTestSuiteParser
name|restTestSuiteParser
init|=
operator|new
name|RestTestSuiteParser
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|api
range|:
name|apis
control|)
block|{
name|List
argument_list|<
name|File
argument_list|>
name|yamlFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|yamlSuites
operator|.
name|get
argument_list|(
name|api
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|yamlFile
range|:
name|yamlFiles
control|)
block|{
comment|//tests distribution disabled for now since it causes reporting problems,
comment|// due to the non unique suite name
comment|//if (mustExecute(yamlFile.getAbsolutePath())) {
name|RestTestSuite
name|restTestSuite
init|=
name|restTestSuiteParser
operator|.
name|parse
argument_list|(
name|api
argument_list|,
name|yamlFile
argument_list|)
decl_stmt|;
for|for
control|(
name|TestSection
name|testSection
range|:
name|restTestSuite
operator|.
name|getTestSections
argument_list|()
control|)
block|{
name|testCandidates
operator|.
name|add
argument_list|(
operator|new
name|RestTestCandidate
argument_list|(
name|restTestSuite
argument_list|,
name|testSection
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//}
block|}
block|}
return|return
name|testCandidates
return|;
block|}
comment|/*private static boolean mustExecute(String test) {         //we distribute the tests across the forked jvms if> 1         if (JVM_COUNT> 1) {             int jvmId = MathUtils.mod(DjbHashFunction.DJB_HASH(test), JVM_COUNT);             if (jvmId != CURRENT_JVM_ID) {                 return false;             }         }         return true;     }*/
DECL|method|resolvePathsProperty
specifier|private
specifier|static
name|String
index|[]
name|resolvePathsProperty
parameter_list|(
name|String
name|propertyName
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
name|String
name|property
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasLength
argument_list|(
name|property
argument_list|)
condition|)
block|{
return|return
name|defaultValue
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|String
index|[]
block|{
name|defaultValue
block|}
return|;
block|}
else|else
block|{
return|return
name|property
operator|.
name|split
argument_list|(
name|PATHS_SEPARATOR
argument_list|)
return|;
block|}
block|}
annotation|@
name|BeforeClass
DECL|method|initExecutionContext
specifier|public
specifier|static
name|void
name|initExecutionContext
parameter_list|()
throws|throws
name|IOException
throws|,
name|RestException
block|{
comment|//skip REST tests if disabled through -Dtests.rest=false
name|assumeTrue
argument_list|(
name|systemPropertyAsBoolean
argument_list|(
name|REST_TESTS
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
index|[]
name|specPaths
init|=
name|resolvePathsProperty
argument_list|(
name|REST_TESTS_SPEC
argument_list|,
name|DEFAULT_SPEC_PATH
argument_list|)
decl_stmt|;
name|RestSpec
name|restSpec
init|=
name|RestSpec
operator|.
name|parseFrom
argument_list|(
name|DEFAULT_SPEC_PATH
argument_list|,
name|specPaths
argument_list|)
decl_stmt|;
assert|assert
name|restTestExecutionContext
operator|==
literal|null
assert|;
name|restTestExecutionContext
operator|=
operator|new
name|RestTestExecutionContext
argument_list|(
name|restSpec
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|()
block|{
name|restTestExecutionContext
operator|.
name|close
argument_list|()
expr_stmt|;
name|restTestExecutionContext
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Used to obtain settings for the REST client that is used to send REST requests.      */
DECL|method|restClientSettings
specifier|protected
name|Settings
name|restClientSettings
parameter_list|()
block|{
return|return
name|ImmutableSettings
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Before
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
throws|,
name|RestException
block|{
comment|//skip test if it matches one of the blacklist globs
for|for
control|(
name|PathMatcher
name|blacklistedPathMatcher
range|:
name|blacklistPathMatchers
control|)
block|{
comment|//we need to replace a few characters otherwise the test section name can't be parsed as a path on windows
name|String
name|testSection
init|=
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|replace
argument_list|(
literal|"*"
argument_list|,
literal|""
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\\"
argument_list|,
literal|"/"
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"\\s+/"
argument_list|,
literal|"/"
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
name|testPath
init|=
name|testCandidate
operator|.
name|getSuitePath
argument_list|()
operator|+
literal|"/"
operator|+
name|testSection
decl_stmt|;
name|assumeFalse
argument_list|(
literal|"["
operator|+
name|testCandidate
operator|.
name|getTestPath
argument_list|()
operator|+
literal|"] skipped, reason: blacklisted"
argument_list|,
name|blacklistedPathMatcher
operator|.
name|matches
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|testPath
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|restTestExecutionContext
operator|.
name|resetClient
argument_list|(
name|cluster
argument_list|()
operator|.
name|httpAddresses
argument_list|()
argument_list|,
name|restClientSettings
argument_list|()
argument_list|)
expr_stmt|;
name|restTestExecutionContext
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|//skip test if the whole suite (yaml file) is disabled
name|assumeFalse
argument_list|(
name|buildSkipMessage
argument_list|(
name|testCandidate
operator|.
name|getSuitePath
argument_list|()
argument_list|,
name|testCandidate
operator|.
name|getSetupSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
argument_list|)
argument_list|,
name|testCandidate
operator|.
name|getSetupSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|skip
argument_list|(
name|restTestExecutionContext
operator|.
name|esVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//skip test if test section is disabled
name|assumeFalse
argument_list|(
name|buildSkipMessage
argument_list|(
name|testCandidate
operator|.
name|getTestPath
argument_list|()
argument_list|,
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
argument_list|)
argument_list|,
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|skip
argument_list|(
name|restTestExecutionContext
operator|.
name|esVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildSkipMessage
specifier|private
specifier|static
name|String
name|buildSkipMessage
parameter_list|(
name|String
name|description
parameter_list|,
name|SkipSection
name|skipSection
parameter_list|)
block|{
name|StringBuilder
name|messageBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|skipSection
operator|.
name|isVersionCheck
argument_list|()
condition|)
block|{
name|messageBuilder
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|description
argument_list|)
operator|.
name|append
argument_list|(
literal|"] skipped, reason: ["
argument_list|)
operator|.
name|append
argument_list|(
name|skipSection
operator|.
name|getReason
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|messageBuilder
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|description
argument_list|)
operator|.
name|append
argument_list|(
literal|"] skipped, reason: features "
argument_list|)
operator|.
name|append
argument_list|(
name|skipSection
operator|.
name|getFeatures
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" not supported"
argument_list|)
expr_stmt|;
block|}
return|return
name|messageBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|randomizeNumberOfShardsAndReplicas
specifier|protected
name|boolean
name|randomizeNumberOfShardsAndReplicas
parameter_list|()
block|{
return|return
name|compatibilityVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_2_0
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IOException
block|{
comment|//let's check that there is something to run, otherwise there might be a problem with the test section
if|if
condition|(
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getExecutableSections
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No executable sections loaded for ["
operator|+
name|testCandidate
operator|.
name|getTestPath
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|testCandidate
operator|.
name|getSetupSection
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"start setup test [{}]"
argument_list|,
name|testCandidate
operator|.
name|getTestPath
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|DoSection
name|doSection
range|:
name|testCandidate
operator|.
name|getSetupSection
argument_list|()
operator|.
name|getDoSections
argument_list|()
control|)
block|{
name|doSection
operator|.
name|execute
argument_list|(
name|restTestExecutionContext
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"end setup test [{}]"
argument_list|,
name|testCandidate
operator|.
name|getTestPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|restTestExecutionContext
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|ExecutableSection
name|executableSection
range|:
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getExecutableSections
argument_list|()
control|)
block|{
name|executableSection
operator|.
name|execute
argument_list|(
name|restTestExecutionContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

