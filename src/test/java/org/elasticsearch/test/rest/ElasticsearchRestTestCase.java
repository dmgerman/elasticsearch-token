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
name|RandomizedTest
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
name|TestGroup
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
name|TimeoutSuite
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
operator|.
name|AwaitsFix
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
name|LuceneTestCase
operator|.
name|Slow
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
name|LuceneTestCase
operator|.
name|SuppressCodecs
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
name|LuceneTestCase
operator|.
name|SuppressFsync
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
name|TimeUnits
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
name|io
operator|.
name|PathUtils
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
name|node
operator|.
name|Node
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
name|section
operator|.
name|ExecutableSection
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
name|RestTestSuite
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
name|SkipSection
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
name|TestSection
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
name|RestApi
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
name|annotation
operator|.
name|ElementType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Inherited
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
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
name|StandardCopyOption
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
name|Comparator
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

begin_class
annotation|@
name|ElasticsearchRestTestCase
operator|.
name|Rest
annotation|@
name|Slow
annotation|@
name|SuppressFsync
comment|// we aren't trying to test this here, and it can make the test slow
annotation|@
name|SuppressCodecs
argument_list|(
literal|"*"
argument_list|)
comment|// requires custom completion postings format
annotation|@
name|ClusterScope
argument_list|(
name|randomDynamicTemplates
operator|=
literal|false
argument_list|)
annotation|@
name|TimeoutSuite
argument_list|(
name|millis
operator|=
literal|40
operator|*
name|TimeUnits
operator|.
name|MINUTE
argument_list|)
comment|// timeout the suite after 40min and fail the test.
annotation|@
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"script/10_basic/Indexed script and update/15_script/Script fail due to commit 35a58d874ef56be50a0ad1d7bfb13edb4204d0a3"
argument_list|)
DECL|class|ElasticsearchRestTestCase
specifier|public
specifier|abstract
class|class
name|ElasticsearchRestTestCase
extends|extends
name|ElasticsearchIntegrationTest
block|{
comment|/**      * Property that allows to control whether the REST tests are run (default) or not      */
DECL|field|TESTS_REST
specifier|public
specifier|static
specifier|final
name|String
name|TESTS_REST
init|=
literal|"tests.rest"
decl_stmt|;
comment|/**      * Annotation for REST tests      */
annotation|@
name|Inherited
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|TYPE
argument_list|)
annotation|@
name|TestGroup
argument_list|(
name|enabled
operator|=
literal|true
argument_list|,
name|sysProperty
operator|=
name|ElasticsearchRestTestCase
operator|.
name|TESTS_REST
argument_list|)
DECL|interface|Rest
specifier|public
annotation_defn|@interface
name|Rest
block|{     }
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
comment|/**      * Property that allows to control whether spec validation is enabled or not (default true).      */
DECL|field|REST_TESTS_VALIDATE_SPEC
specifier|public
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
DECL|field|testCandidate
specifier|private
specifier|final
name|RestTestCandidate
name|testCandidate
decl_stmt|;
DECL|method|ElasticsearchRestTestCase
specifier|public
name|ElasticsearchRestTestCase
parameter_list|(
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
name|PathUtils
operator|.
name|getDefaultFileSystem
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
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|HTTP_ENABLED
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
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
parameter_list|(
name|int
name|id
parameter_list|,
name|int
name|count
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestTestParseException
block|{
name|TestGroup
name|testGroup
init|=
name|Rest
operator|.
name|class
operator|.
name|getAnnotation
argument_list|(
name|TestGroup
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|sysProperty
init|=
name|TestGroup
operator|.
name|Utilities
operator|.
name|getSysProperty
argument_list|(
name|Rest
operator|.
name|class
argument_list|)
decl_stmt|;
name|boolean
name|enabled
decl_stmt|;
try|try
block|{
name|enabled
operator|=
name|RandomizedTest
operator|.
name|systemPropertyAsBoolean
argument_list|(
name|sysProperty
argument_list|,
name|testGroup
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// Ignore malformed system property, disable the group if malformed though.
name|enabled
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|()
return|;
block|}
comment|//parse tests only if rest test group is enabled, otherwise rest tests might not even be available on file system
name|List
argument_list|<
name|RestTestCandidate
argument_list|>
name|restTestCandidates
init|=
name|collectTestCandidates
argument_list|(
name|id
argument_list|,
name|count
argument_list|)
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
parameter_list|(
name|int
name|id
parameter_list|,
name|int
name|count
parameter_list|)
throws|throws
name|RestTestParseException
throws|,
name|IOException
block|{
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
name|RestTestSuiteParser
name|restTestSuiteParser
init|=
operator|new
name|RestTestSuiteParser
argument_list|()
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
name|Path
name|yamlFile
range|:
name|yamlFiles
control|)
block|{
name|String
name|key
init|=
name|api
operator|+
name|yamlFile
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|mustExecute
argument_list|(
name|key
argument_list|,
name|id
argument_list|,
name|count
argument_list|)
condition|)
block|{
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
operator|new
name|Comparator
argument_list|<
name|RestTestCandidate
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|RestTestCandidate
name|o1
parameter_list|,
name|RestTestCandidate
name|o2
parameter_list|)
block|{
return|return
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
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|testCandidates
return|;
block|}
DECL|method|mustExecute
specifier|private
specifier|static
name|boolean
name|mustExecute
parameter_list|(
name|String
name|test
parameter_list|,
name|int
name|id
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|int
name|hash
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|long
operator|)
name|test
operator|.
name|hashCode
argument_list|()
argument_list|)
operator|%
name|count
argument_list|)
decl_stmt|;
return|return
name|hash
operator|==
name|id
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
comment|/**      * Returns a new FileSystem to read REST resources, or null if they      * are available from classpath.      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"proper use of URL, hack around a JDK bug"
argument_list|)
DECL|method|getFileSystem
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
expr|<
name|String
argument_list|,
name|Object
operator|>
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
name|RestSpec
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
name|restTestExecutionContext
operator|=
operator|new
name|RestTestExecutionContext
argument_list|(
name|restSpec
argument_list|)
expr_stmt|;
block|}
DECL|method|validateSpec
specifier|private
specifier|static
name|void
name|validateSpec
parameter_list|(
name|RestSpec
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
name|RestApi
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
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|restTestExecutionContext
operator|!=
literal|null
condition|)
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
block|}
annotation|@
name|Override
DECL|method|maximumNumberOfShards
specifier|protected
name|int
name|maximumNumberOfShards
parameter_list|()
block|{
return|return
literal|3
return|;
comment|// never go crazy in the REST tests
block|}
annotation|@
name|Override
DECL|method|maximumNumberOfReplicas
specifier|protected
name|int
name|maximumNumberOfReplicas
parameter_list|()
block|{
comment|// hardcoded 1 since this is what clients also do and our tests must expect that we have only node
comment|// with replicas set to 1 ie. the cluster won't be green
return|return
literal|1
return|;
block|}
comment|/**      * Used to obtain settings for the REST client that is used to send REST requests.      */
DECL|method|restClientSettings
specifier|protected
name|Settings
name|restClientSettings
parameter_list|()
block|{
return|return
name|Settings
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
name|replace
argument_list|(
literal|":"
argument_list|,
literal|""
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
name|PathUtils
operator|.
name|get
argument_list|(
name|testPath
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//The client needs non static info to get initialized, therefore it can't be initialized in the before class
name|restTestExecutionContext
operator|.
name|initClient
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

