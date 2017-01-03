begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
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
name|RandomizedTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
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
name|client
operator|.
name|Response
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|ResponseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|RestClient
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
name|SuppressForbidden
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
name|XContentHelper
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
name|ESRestTestCase
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
name|yaml
operator|.
name|restspec
operator|.
name|ClientYamlSuiteRestApi
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
name|yaml
operator|.
name|restspec
operator|.
name|ClientYamlSuiteRestSpec
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
name|yaml
operator|.
name|section
operator|.
name|ClientYamlTestSection
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
name|yaml
operator|.
name|section
operator|.
name|ClientYamlTestSuite
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
name|yaml
operator|.
name|section
operator|.
name|DoSection
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
name|yaml
operator|.
name|section
operator|.
name|ExecutableSection
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|FileSystem
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
name|Files
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
name|nio
operator|.
name|file
operator|.
name|StandardCopyOption
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
comment|/**  * Runs a suite of yaml tests shared with all the official Elasticsearch clients against against an elasticsearch cluster.  */
end_comment

begin_class
DECL|class|ESClientYamlSuiteTestCase
specifier|public
specifier|abstract
class|class
name|ESClientYamlSuiteTestCase
extends|extends
name|ESRestTestCase
block|{
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
comment|/**      * Property that allows to blacklist some of the REST tests based on a comma separated list of globs      * e.g. "-Dtests.rest.blacklist=get/10_basic/*"      */
DECL|field|REST_TESTS_BLACKLIST
specifier|public
specifier|static
specifier|final
name|String
name|REST_TESTS_BLACKLIST
init|=
literal|"tests.rest.blacklist"
decl_stmt|;
comment|/**      * Property that allows to control whether spec validation is enabled or not (default true).      */
DECL|field|REST_TESTS_VALIDATE_SPEC
specifier|private
specifier|static
specifier|final
name|String
name|REST_TESTS_VALIDATE_SPEC
init|=
literal|"tests.rest.validate_spec"
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
DECL|field|REST_LOAD_PACKAGED_TESTS
specifier|private
specifier|static
specifier|final
name|String
name|REST_LOAD_PACKAGED_TESTS
init|=
literal|"tests.rest.load_packaged"
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
comment|/**      * This separator pattern matches ',' except it is preceded by a '\'.      * This allows us to support ',' within paths when it is escaped with a slash.      *      * For example, the path string "/a/b/c\,d/e/f,/foo/bar,/baz" is separated to "/a/b/c\,d/e/f", "/foo/bar" and "/baz".      *      * For reference, this regular expression feature is known as zero-width negative look-behind.      *      */
DECL|field|PATHS_SEPARATOR
specifier|private
specifier|static
specifier|final
name|String
name|PATHS_SEPARATOR
init|=
literal|"(?<!\\\\),"
decl_stmt|;
DECL|field|blacklistPathMatchers
specifier|private
specifier|static
name|List
argument_list|<
name|BlacklistedPathPatternMatcher
argument_list|>
name|blacklistPathMatchers
decl_stmt|;
DECL|field|restTestExecutionContext
specifier|private
specifier|static
name|ClientYamlTestExecutionContext
name|restTestExecutionContext
decl_stmt|;
DECL|field|adminExecutionContext
specifier|private
specifier|static
name|ClientYamlTestExecutionContext
name|adminExecutionContext
decl_stmt|;
DECL|field|testCandidate
specifier|private
specifier|final
name|ClientYamlTestCandidate
name|testCandidate
decl_stmt|;
DECL|method|ESClientYamlSuiteTestCase
specifier|protected
name|ESClientYamlSuiteTestCase
parameter_list|(
name|ClientYamlTestCandidate
name|testCandidate
parameter_list|)
block|{
name|this
operator|.
name|testCandidate
operator|=
name|testCandidate
expr_stmt|;
block|}
annotation|@
name|Before
DECL|method|initAndResetContext
specifier|public
name|void
name|initAndResetContext
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|restTestExecutionContext
operator|==
literal|null
condition|)
block|{
assert|assert
name|adminExecutionContext
operator|==
literal|null
assert|;
assert|assert
name|blacklistPathMatchers
operator|==
literal|null
assert|;
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
name|blacklistPathMatchers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|entry
range|:
name|blacklist
control|)
block|{
name|blacklistPathMatchers
operator|.
name|add
argument_list|(
operator|new
name|BlacklistedPathPatternMatcher
argument_list|(
name|entry
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|ClientYamlSuiteRestSpec
name|restSpec
init|=
literal|null
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// don't make a try-with, getFileSystem returns null
comment|// ... and you can't close() the default filesystem
try|try
block|{
name|restSpec
operator|=
name|ClientYamlSuiteRestSpec
operator|.
name|parseFrom
argument_list|(
name|fileSystem
argument_list|,
name|DEFAULT_SPEC_PATH
argument_list|,
name|specPaths
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|fileSystem
argument_list|)
expr_stmt|;
block|}
name|validateSpec
argument_list|(
name|restSpec
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HttpHost
argument_list|>
name|hosts
init|=
name|getClusterHosts
argument_list|()
decl_stmt|;
name|RestClient
name|restClient
init|=
name|client
argument_list|()
decl_stmt|;
name|Version
name|esVersion
decl_stmt|;
try|try
block|{
name|Tuple
argument_list|<
name|Version
argument_list|,
name|Version
argument_list|>
name|versionVersionTuple
init|=
name|readVersionsFromCatNodes
argument_list|(
name|restClient
argument_list|)
decl_stmt|;
name|esVersion
operator|=
name|versionVersionTuple
operator|.
name|v1
argument_list|()
expr_stmt|;
name|Version
name|masterVersion
init|=
name|versionVersionTuple
operator|.
name|v2
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"initializing yaml client, minimum es version: [{}] master version: [{}] hosts: {}"
argument_list|,
name|esVersion
argument_list|,
name|masterVersion
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|ex
parameter_list|)
block|{
if|if
condition|(
name|ex
operator|.
name|getResponse
argument_list|()
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
operator|==
literal|403
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Fallback to simple info '/' request, _cat/nodes is not authorized"
argument_list|)
expr_stmt|;
name|esVersion
operator|=
name|readVersionsFromInfo
argument_list|(
name|restClient
argument_list|,
name|hosts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"initializing yaml client, minimum es version: [{}] hosts: {}"
argument_list|,
name|esVersion
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|ex
throw|;
block|}
block|}
name|ClientYamlTestClient
name|clientYamlTestClient
init|=
operator|new
name|ClientYamlTestClient
argument_list|(
name|restSpec
argument_list|,
name|restClient
argument_list|,
name|hosts
argument_list|,
name|esVersion
argument_list|)
decl_stmt|;
name|restTestExecutionContext
operator|=
operator|new
name|ClientYamlTestExecutionContext
argument_list|(
name|clientYamlTestClient
argument_list|)
expr_stmt|;
name|adminExecutionContext
operator|=
operator|new
name|ClientYamlTestExecutionContext
argument_list|(
name|clientYamlTestClient
argument_list|)
expr_stmt|;
block|}
assert|assert
name|restTestExecutionContext
operator|!=
literal|null
assert|;
assert|assert
name|adminExecutionContext
operator|!=
literal|null
assert|;
assert|assert
name|blacklistPathMatchers
operator|!=
literal|null
assert|;
comment|// admin context must be available for @After always, regardless of whether the test was blacklisted
name|adminExecutionContext
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|//skip test if it matches one of the blacklist globs
for|for
control|(
name|BlacklistedPathPatternMatcher
name|blacklistedPathMatcher
range|:
name|blacklistPathMatchers
control|)
block|{
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
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getName
argument_list|()
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
name|isSuffixMatch
argument_list|(
name|testPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|restTestExecutionContext
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|//skip test if the whole suite (yaml file) is disabled
name|assumeFalse
argument_list|(
name|testCandidate
operator|.
name|getSetupSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getSkipMessage
argument_list|(
name|testCandidate
operator|.
name|getSuitePath
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
comment|//skip test if the whole suite (yaml file) is disabled
name|assumeFalse
argument_list|(
name|testCandidate
operator|.
name|getTeardownSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getSkipMessage
argument_list|(
name|testCandidate
operator|.
name|getSuitePath
argument_list|()
argument_list|)
argument_list|,
name|testCandidate
operator|.
name|getTeardownSection
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
name|testCandidate
operator|.
name|getTestSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|getSkipMessage
argument_list|(
name|testCandidate
operator|.
name|getTestPath
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
annotation|@
name|Override
DECL|method|afterIfFailed
specifier|protected
name|void
name|afterIfFailed
parameter_list|(
name|List
argument_list|<
name|Throwable
argument_list|>
name|errors
parameter_list|)
block|{
comment|// Dump the stash on failure. Instead of dumping it in true json we escape `\n`s so stack traces are easier to read
name|logger
operator|.
name|info
argument_list|(
literal|"Stash dump on failure [{}]"
argument_list|,
name|XContentHelper
operator|.
name|toString
argument_list|(
name|restTestExecutionContext
operator|.
name|stash
argument_list|()
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\\n"
argument_list|,
literal|"\n"
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\\r"
argument_list|,
literal|"\r"
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\\t"
argument_list|,
literal|"\t"
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|afterIfFailed
argument_list|(
name|errors
argument_list|)
expr_stmt|;
block|}
DECL|method|createParameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|createParameters
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ClientYamlTestCandidate
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
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ClientYamlTestCandidate
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
name|ClientYamlTestCandidate
argument_list|>
name|collectTestCandidates
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ClientYamlTestCandidate
argument_list|>
name|testCandidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// don't make a try-with, getFileSystem returns null
comment|// ... and you can't close() the default filesystem
try|try
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
name|Path
argument_list|>
argument_list|>
name|yamlSuites
init|=
name|FileUtils
operator|.
name|findYamlSuites
argument_list|(
name|fileSystem
argument_list|,
name|DEFAULT_TESTS_PATH
argument_list|,
name|paths
argument_list|)
decl_stmt|;
comment|//yaml suites are grouped by directory (effectively by api)
for|for
control|(
name|String
name|api
range|:
name|yamlSuites
operator|.
name|keySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|yamlFiles
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|Path
name|yamlFile
range|:
name|yamlFiles
control|)
block|{
name|ClientYamlTestSuite
name|restTestSuite
init|=
name|ClientYamlTestSuite
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
name|ClientYamlTestSection
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
name|ClientYamlTestCandidate
argument_list|(
name|restTestSuite
argument_list|,
name|testSection
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|fileSystem
argument_list|)
expr_stmt|;
block|}
comment|//sort the candidates so they will always be in the same order before being shuffled, for repeatability
name|Collections
operator|.
name|sort
argument_list|(
name|testCandidates
argument_list|,
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|o1
operator|.
name|getTestPath
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getTestPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|testCandidates
return|;
block|}
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
name|Strings
operator|.
name|EMPTY_ARRAY
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
comment|/**      * Returns a new FileSystem to read REST resources, or null if they      * are available from classpath.      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"proper use of URL, hack around a JDK bug"
argument_list|)
DECL|method|getFileSystem
specifier|protected
specifier|static
name|FileSystem
name|getFileSystem
parameter_list|()
throws|throws
name|IOException
block|{
comment|// REST suite handling is currently complicated, with lots of filtering and so on
comment|// For now, to work embedded in a jar, return a ZipFileSystem over the jar contents.
name|URL
name|codeLocation
init|=
name|FileUtils
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|boolean
name|loadPackaged
init|=
name|RandomizedTest
operator|.
name|systemPropertyAsBoolean
argument_list|(
name|REST_LOAD_PACKAGED_TESTS
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|codeLocation
operator|.
name|getFile
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
operator|&&
name|loadPackaged
condition|)
block|{
try|try
block|{
comment|// hack around a bug in the zipfilesystem implementation before java 9,
comment|// its checkWritable was incorrect and it won't work without write permissions.
comment|// if we add the permission, it will open jars r/w, which is too scary! so copy to a safe r-w location.
name|Path
name|tmp
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
literal|null
argument_list|,
literal|".jar"
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|in
init|=
name|codeLocation
operator|.
name|openStream
argument_list|()
init|)
block|{
name|Files
operator|.
name|copy
argument_list|(
name|in
argument_list|,
name|tmp
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
block|}
return|return
name|FileSystems
operator|.
name|newFileSystem
argument_list|(
operator|new
name|URI
argument_list|(
literal|"jar:"
operator|+
name|tmp
operator|.
name|toUri
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"couldn't open zipfilesystem: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getAdminExecutionContext
specifier|protected
name|ClientYamlTestExecutionContext
name|getAdminExecutionContext
parameter_list|()
block|{
return|return
name|adminExecutionContext
return|;
block|}
DECL|method|validateSpec
specifier|private
specifier|static
name|void
name|validateSpec
parameter_list|(
name|ClientYamlSuiteRestSpec
name|restSpec
parameter_list|)
block|{
name|boolean
name|validateSpec
init|=
name|RandomizedTest
operator|.
name|systemPropertyAsBoolean
argument_list|(
name|REST_TESTS_VALIDATE_SPEC
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|validateSpec
condition|)
block|{
name|StringBuilder
name|errorMessage
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ClientYamlSuiteRestApi
name|restApi
range|:
name|restSpec
operator|.
name|getApis
argument_list|()
control|)
block|{
if|if
condition|(
name|restApi
operator|.
name|getMethods
argument_list|()
operator|.
name|contains
argument_list|(
literal|"GET"
argument_list|)
operator|&&
name|restApi
operator|.
name|isBodySupported
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|restApi
operator|.
name|getMethods
argument_list|()
operator|.
name|contains
argument_list|(
literal|"POST"
argument_list|)
condition|)
block|{
name|errorMessage
operator|.
name|append
argument_list|(
literal|"\n- "
argument_list|)
operator|.
name|append
argument_list|(
name|restApi
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" supports GET with a body but doesn't support POST"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|errorMessage
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|errorMessage
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|AfterClass
DECL|method|clearStatic
specifier|public
specifier|static
name|void
name|clearStatic
parameter_list|()
block|{
name|blacklistPathMatchers
operator|=
literal|null
expr_stmt|;
name|restTestExecutionContext
operator|=
literal|null
expr_stmt|;
name|adminExecutionContext
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|readVersionsFromCatNodes
specifier|private
specifier|static
name|Tuple
argument_list|<
name|Version
argument_list|,
name|Version
argument_list|>
name|readVersionsFromCatNodes
parameter_list|(
name|RestClient
name|restClient
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we simply go to the _cat/nodes API and parse all versions in the cluster
name|Response
name|response
init|=
name|restClient
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/_cat/nodes"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"h"
argument_list|,
literal|"version,master"
argument_list|)
argument_list|)
decl_stmt|;
name|ClientYamlTestResponse
name|restTestResponse
init|=
operator|new
name|ClientYamlTestResponse
argument_list|(
name|response
argument_list|)
decl_stmt|;
name|String
name|nodesCatResponse
init|=
name|restTestResponse
operator|.
name|getBodyAsString
argument_list|()
decl_stmt|;
name|String
index|[]
name|split
init|=
name|nodesCatResponse
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|Version
name|version
init|=
literal|null
decl_stmt|;
name|Version
name|masterVersion
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|perNode
range|:
name|split
control|)
block|{
specifier|final
name|String
index|[]
name|versionAndMaster
init|=
name|perNode
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
assert|assert
name|versionAndMaster
operator|.
name|length
operator|==
literal|2
operator|:
literal|"invalid line: "
operator|+
name|perNode
operator|+
literal|" length: "
operator|+
name|versionAndMaster
operator|.
name|length
assert|;
specifier|final
name|Version
name|currentVersion
init|=
name|Version
operator|.
name|fromString
argument_list|(
name|versionAndMaster
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|master
init|=
name|versionAndMaster
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|"*"
argument_list|)
decl_stmt|;
if|if
condition|(
name|master
condition|)
block|{
assert|assert
name|masterVersion
operator|==
literal|null
assert|;
name|masterVersion
operator|=
name|currentVersion
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
name|version
operator|=
name|currentVersion
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|currentVersion
argument_list|)
condition|)
block|{
name|version
operator|=
name|currentVersion
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|version
argument_list|,
name|masterVersion
argument_list|)
return|;
block|}
DECL|method|readVersionsFromInfo
specifier|private
specifier|static
name|Version
name|readVersionsFromInfo
parameter_list|(
name|RestClient
name|restClient
parameter_list|,
name|int
name|numHosts
parameter_list|)
throws|throws
name|IOException
block|{
name|Version
name|version
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numHosts
condition|;
name|i
operator|++
control|)
block|{
comment|//we don't really use the urls here, we rely on the client doing round-robin to touch all the nodes in the cluster
name|Response
name|response
init|=
name|restClient
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
name|ClientYamlTestResponse
name|restTestResponse
init|=
operator|new
name|ClientYamlTestResponse
argument_list|(
name|response
argument_list|)
decl_stmt|;
name|Object
name|latestVersion
init|=
name|restTestResponse
operator|.
name|evaluate
argument_list|(
literal|"version.number"
argument_list|)
decl_stmt|;
if|if
condition|(
name|latestVersion
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"elasticsearch version not found in the response"
argument_list|)
throw|;
block|}
specifier|final
name|Version
name|currentVersion
init|=
name|Version
operator|.
name|fromString
argument_list|(
name|latestVersion
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
name|version
operator|=
name|currentVersion
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|currentVersion
argument_list|)
condition|)
block|{
name|version
operator|=
name|currentVersion
expr_stmt|;
block|}
block|}
return|return
name|version
return|;
block|}
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
name|debug
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
name|executeSection
argument_list|(
name|doSection
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
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
try|try
block|{
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
name|executeSection
argument_list|(
name|executableSection
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"start teardown test [{}]"
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
name|getTeardownSection
argument_list|()
operator|.
name|getDoSections
argument_list|()
control|)
block|{
name|executeSection
argument_list|(
name|doSection
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"end teardown test [{}]"
argument_list|,
name|testCandidate
operator|.
name|getTestPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Execute an {@link ExecutableSection}, careful to log its place of origin on failure.      */
DECL|method|executeSection
specifier|private
name|void
name|executeSection
parameter_list|(
name|ExecutableSection
name|executableSection
parameter_list|)
block|{
try|try
block|{
name|executableSection
operator|.
name|execute
argument_list|(
name|restTestExecutionContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|errorMessage
argument_list|(
name|executableSection
argument_list|,
name|e
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|errorMessage
argument_list|(
name|executableSection
argument_list|,
name|e
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|errorMessage
specifier|private
name|String
name|errorMessage
parameter_list|(
name|ExecutableSection
name|executableSection
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
return|return
literal|"Failure at ["
operator|+
name|testCandidate
operator|.
name|getSuitePath
argument_list|()
operator|+
literal|":"
operator|+
name|executableSection
operator|.
name|getLocation
argument_list|()
operator|.
name|lineNumber
operator|+
literal|"]: "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
return|;
block|}
block|}
end_class

end_unit

