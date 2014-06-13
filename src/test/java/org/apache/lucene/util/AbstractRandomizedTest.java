begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.util
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
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
name|JUnit4MethodProvider
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
name|LifecycleScope
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
name|RandomizedContext
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
name|Listeners
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
name|TestMethodProviders
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
name|rules
operator|.
name|NoClassHooksShadowingRule
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
name|rules
operator|.
name|NoInstanceHooksOverridesRule
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
name|rules
operator|.
name|StaticFieldsInvariantRule
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
name|rules
operator|.
name|SystemPropertiesInvariantRule
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
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|Lucene
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|ElasticsearchTestCase
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
name|ReproduceInfoPrinter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|RuleChain
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|RunWith
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|lang
operator|.
name|annotation
operator|.
name|*
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
import|;
end_import

begin_class
annotation|@
name|TestMethodProviders
argument_list|(
block|{
name|LuceneJUnit3MethodProvider
operator|.
name|class
block|,
name|JUnit4MethodProvider
operator|.
name|class
block|}
argument_list|)
annotation|@
name|Listeners
argument_list|(
block|{
name|ReproduceInfoPrinter
operator|.
name|class
block|,
name|FailureMarker
operator|.
name|class
block|}
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|value
operator|=
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedRunner
operator|.
name|class
argument_list|)
annotation|@
name|SuppressCodecs
argument_list|(
name|value
operator|=
literal|"Lucene3x"
argument_list|)
comment|// NOTE: this class is in o.a.lucene.util since it uses some classes that are related
comment|// to the test framework that didn't make sense to copy but are package private access
DECL|class|AbstractRandomizedTest
specifier|public
specifier|abstract
class|class
name|AbstractRandomizedTest
extends|extends
name|RandomizedTest
block|{
comment|/**      * Annotation for integration tests      */
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
name|SYSPROP_INTEGRATION
argument_list|)
DECL|interface|IntegrationTests
specifier|public
annotation_defn|@interface
name|IntegrationTests
block|{     }
comment|// --------------------------------------------------------------------
comment|// Test groups, system properties and other annotations modifying tests
comment|// --------------------------------------------------------------------
comment|/**      * @see #ignoreAfterMaxFailures      */
DECL|field|SYSPROP_MAXFAILURES
specifier|public
specifier|static
specifier|final
name|String
name|SYSPROP_MAXFAILURES
init|=
literal|"tests.maxfailures"
decl_stmt|;
comment|/**      * @see #ignoreAfterMaxFailures      */
DECL|field|SYSPROP_FAILFAST
specifier|public
specifier|static
specifier|final
name|String
name|SYSPROP_FAILFAST
init|=
literal|"tests.failfast"
decl_stmt|;
DECL|field|SYSPROP_INTEGRATION
specifier|public
specifier|static
specifier|final
name|String
name|SYSPROP_INTEGRATION
init|=
literal|"tests.integration"
decl_stmt|;
DECL|field|SYSPROP_PROCESSORS
specifier|public
specifier|static
specifier|final
name|String
name|SYSPROP_PROCESSORS
init|=
literal|"tests.processors"
decl_stmt|;
comment|// -----------------------------------------------------------------
comment|// Truly immutable fields and constants, initialized once and valid
comment|// for all suites ever since.
comment|// -----------------------------------------------------------------
comment|/**      * Use this constant when creating Analyzers and any other version-dependent stuff.      *<p><b>NOTE:</b> Change this when development starts for new Lucene version:      */
DECL|field|TEST_VERSION_CURRENT
specifier|public
specifier|static
specifier|final
name|Version
name|TEST_VERSION_CURRENT
init|=
name|Lucene
operator|.
name|VERSION
decl_stmt|;
comment|/**      * True if and only if tests are run in verbose mode. If this flag is false      * tests are not expected to print any messages.      */
DECL|field|VERBOSE
specifier|public
specifier|static
specifier|final
name|boolean
name|VERBOSE
init|=
name|systemPropertyAsBoolean
argument_list|(
literal|"tests.verbose"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|/**      * A random multiplier which you should use when writing random tests:      * multiply it by the number of iterations to scale your tests (for nightly builds).      */
DECL|field|RANDOM_MULTIPLIER
specifier|public
specifier|static
specifier|final
name|int
name|RANDOM_MULTIPLIER
init|=
name|systemPropertyAsInt
argument_list|(
literal|"tests.multiplier"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|/**      * TODO: javadoc?      */
DECL|field|DEFAULT_LINE_DOCS_FILE
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_LINE_DOCS_FILE
init|=
literal|"europarl.lines.txt.gz"
decl_stmt|;
comment|/**      * the line file used by LineFileDocs      */
DECL|field|TEST_LINE_DOCS_FILE
specifier|public
specifier|static
specifier|final
name|String
name|TEST_LINE_DOCS_FILE
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.linedocsfile"
argument_list|,
name|DEFAULT_LINE_DOCS_FILE
argument_list|)
decl_stmt|;
comment|/**      * Create indexes in this directory, optimally use a subdir, named after the test      */
DECL|field|TEMP_DIR
specifier|public
specifier|static
specifier|final
name|File
name|TEMP_DIR
decl_stmt|;
DECL|field|TESTS_PROCESSORS
specifier|public
specifier|static
specifier|final
name|int
name|TESTS_PROCESSORS
decl_stmt|;
static|static
block|{
name|String
name|s
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"tempDir"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|==
literal|null
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"To run tests, you need to define system property 'tempDir' or 'java.io.tmpdir'."
argument_list|)
throw|;
name|TEMP_DIR
operator|=
operator|new
name|File
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|TEMP_DIR
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|String
name|processors
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|SYSPROP_PROCESSORS
argument_list|,
literal|""
argument_list|)
decl_stmt|;
comment|// mvn sets "" as default
if|if
condition|(
name|processors
operator|==
literal|null
operator|||
name|processors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|processors
operator|=
name|Integer
operator|.
name|toString
argument_list|(
name|EsExecutors
operator|.
name|boundedNumberOfProcessors
argument_list|(
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TESTS_PROCESSORS
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|processors
argument_list|)
expr_stmt|;
block|}
comment|/**      * These property keys will be ignored in verification of altered properties.      *      * @see SystemPropertiesInvariantRule      * @see #ruleChain      * @see #classRules      */
DECL|field|IGNORED_INVARIANT_PROPERTIES
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|IGNORED_INVARIANT_PROPERTIES
init|=
block|{
literal|"user.timezone"
block|,
literal|"java.rmi.server.randomIDs"
block|,
literal|"sun.nio.ch.bugLevel"
block|,
literal|"solr.directoryFactory"
block|,
literal|"solr.solr.home"
block|,
literal|"solr.data.dir"
comment|// these might be set by the LuceneTestCase -- ignore
block|}
decl_stmt|;
comment|// -----------------------------------------------------------------
comment|// Fields initialized in class or instance rules.
comment|// -----------------------------------------------------------------
comment|// -----------------------------------------------------------------
comment|// Class level (suite) rules.
comment|// -----------------------------------------------------------------
comment|/**      * Stores the currently class under test.      */
DECL|field|classNameRule
specifier|private
specifier|static
specifier|final
name|TestRuleStoreClassName
name|classNameRule
decl_stmt|;
comment|/**      * Class environment setup rule.      */
DECL|field|classEnvRule
specifier|static
specifier|final
name|TestRuleSetupAndRestoreClassEnv
name|classEnvRule
decl_stmt|;
comment|/**      * Suite failure marker (any error in the test or suite scope).      */
DECL|field|suiteFailureMarker
specifier|public
specifier|final
specifier|static
name|TestRuleMarkFailure
name|suiteFailureMarker
init|=
operator|new
name|TestRuleMarkFailure
argument_list|()
decl_stmt|;
comment|/**      * Ignore tests after hitting a designated number of initial failures. This      * is truly a "static" global singleton since it needs to span the lifetime of all      * test classes running inside this JVM (it cannot be part of a class rule).      *<p/>      *<p>This poses some problems for the test framework's tests because these sometimes      * trigger intentional failures which add up to the global count. This field contains      * a (possibly) changing reference to {@link TestRuleIgnoreAfterMaxFailures} and we      * dispatch to its current value from the {@link #classRules} chain using {@link TestRuleDelegate}.      */
DECL|field|ignoreAfterMaxFailuresDelegate
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|TestRuleIgnoreAfterMaxFailures
argument_list|>
name|ignoreAfterMaxFailuresDelegate
decl_stmt|;
DECL|field|ignoreAfterMaxFailures
specifier|private
specifier|static
specifier|final
name|TestRule
name|ignoreAfterMaxFailures
decl_stmt|;
static|static
block|{
name|int
name|maxFailures
init|=
name|systemPropertyAsInt
argument_list|(
name|SYSPROP_MAXFAILURES
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|boolean
name|failFast
init|=
name|systemPropertyAsBoolean
argument_list|(
name|SYSPROP_FAILFAST
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|failFast
condition|)
block|{
if|if
condition|(
name|maxFailures
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|maxFailures
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|Logger
operator|.
name|getLogger
argument_list|(
name|LuceneTestCase
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|warning
argument_list|(
literal|"Property '"
operator|+
name|SYSPROP_MAXFAILURES
operator|+
literal|"'="
operator|+
name|maxFailures
operator|+
literal|", 'failfast' is"
operator|+
literal|" ignored."
argument_list|)
expr_stmt|;
block|}
block|}
name|ignoreAfterMaxFailuresDelegate
operator|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
operator|new
name|TestRuleIgnoreAfterMaxFailures
argument_list|(
name|maxFailures
argument_list|)
argument_list|)
expr_stmt|;
name|ignoreAfterMaxFailures
operator|=
name|TestRuleDelegate
operator|.
name|of
argument_list|(
name|ignoreAfterMaxFailuresDelegate
argument_list|)
expr_stmt|;
block|}
comment|/**      * Temporarily substitute the global {@link TestRuleIgnoreAfterMaxFailures}. See      * {@link #ignoreAfterMaxFailuresDelegate} for some explanation why this method      * is needed.      */
DECL|method|replaceMaxFailureRule
specifier|public
specifier|static
name|TestRuleIgnoreAfterMaxFailures
name|replaceMaxFailureRule
parameter_list|(
name|TestRuleIgnoreAfterMaxFailures
name|newValue
parameter_list|)
block|{
return|return
name|ignoreAfterMaxFailuresDelegate
operator|.
name|getAndSet
argument_list|(
name|newValue
argument_list|)
return|;
block|}
comment|/**      * Max 10mb of static data stored in a test suite class after the suite is complete.      * Prevents static data structures leaking and causing OOMs in subsequent tests.      */
DECL|field|STATIC_LEAK_THRESHOLD
specifier|private
specifier|final
specifier|static
name|long
name|STATIC_LEAK_THRESHOLD
init|=
literal|10
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|/**      * By-name list of ignored types like loggers etc.      */
DECL|field|STATIC_LEAK_IGNORED_TYPES
specifier|private
specifier|final
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|STATIC_LEAK_IGNORED_TYPES
init|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|EnumSet
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|TOP_LEVEL_CLASSES
specifier|private
specifier|final
specifier|static
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|TOP_LEVEL_CLASSES
init|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|AbstractRandomizedTest
operator|.
name|class
argument_list|,
name|LuceneTestCase
operator|.
name|class
argument_list|,
name|ElasticsearchIntegrationTest
operator|.
name|class
argument_list|,
name|ElasticsearchTestCase
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|/**      * This controls how suite-level rules are nested. It is important that _all_ rules declared      * in {@link LuceneTestCase} are executed in proper order if they depend on each      * other.      */
annotation|@
name|ClassRule
DECL|field|classRules
specifier|public
specifier|static
name|TestRule
name|classRules
init|=
name|RuleChain
operator|.
name|outerRule
argument_list|(
operator|new
name|TestRuleIgnoreTestSuites
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
name|ignoreAfterMaxFailures
argument_list|)
operator|.
name|around
argument_list|(
name|suiteFailureMarker
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|TestRuleAssertionsRequired
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|StaticFieldsInvariantRule
argument_list|(
name|STATIC_LEAK_THRESHOLD
argument_list|,
literal|true
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|accept
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
name|field
parameter_list|)
block|{
comment|// Don't count known classes that consume memory once.
if|if
condition|(
name|STATIC_LEAK_IGNORED_TYPES
operator|.
name|contains
argument_list|(
name|field
operator|.
name|getType
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Don't count references from ourselves, we're top-level.
if|if
condition|(
name|TOP_LEVEL_CLASSES
operator|.
name|contains
argument_list|(
name|field
operator|.
name|getDeclaringClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|accept
argument_list|(
name|field
argument_list|)
return|;
block|}
block|}
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|NoClassHooksShadowingRule
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|NoInstanceHooksOverridesRule
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|verify
parameter_list|(
name|Method
name|key
parameter_list|)
block|{
name|String
name|name
init|=
name|key
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
operator|(
name|name
operator|.
name|equals
argument_list|(
literal|"setUp"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"tearDown"
argument_list|)
operator|)
return|;
block|}
block|}
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|SystemPropertiesInvariantRule
argument_list|(
name|IGNORED_INVARIANT_PROPERTIES
argument_list|)
argument_list|)
operator|.
name|around
argument_list|(
name|classNameRule
operator|=
operator|new
name|TestRuleStoreClassName
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
name|classEnvRule
operator|=
operator|new
name|TestRuleSetupAndRestoreClassEnv
argument_list|()
argument_list|)
decl_stmt|;
comment|// -----------------------------------------------------------------
comment|// Test level rules.
comment|// -----------------------------------------------------------------
comment|/**      * Enforces {@link #setUp()} and {@link #tearDown()} calls are chained.      */
DECL|field|parentChainCallRule
specifier|private
name|TestRuleSetupTeardownChained
name|parentChainCallRule
init|=
operator|new
name|TestRuleSetupTeardownChained
argument_list|()
decl_stmt|;
comment|/**      * Save test thread and name.      */
DECL|field|threadAndTestNameRule
specifier|private
name|TestRuleThreadAndTestName
name|threadAndTestNameRule
init|=
operator|new
name|TestRuleThreadAndTestName
argument_list|()
decl_stmt|;
comment|/**      * Taint suite result with individual test failures.      */
DECL|field|testFailureMarker
specifier|private
name|TestRuleMarkFailure
name|testFailureMarker
init|=
operator|new
name|TestRuleMarkFailure
argument_list|(
name|suiteFailureMarker
argument_list|)
decl_stmt|;
comment|/**      * This controls how individual test rules are nested. It is important that      * _all_ rules declared in {@link LuceneTestCase} are executed in proper order      * if they depend on each other.      */
annotation|@
name|Rule
DECL|field|ruleChain
specifier|public
specifier|final
name|TestRule
name|ruleChain
init|=
name|RuleChain
operator|.
name|outerRule
argument_list|(
name|testFailureMarker
argument_list|)
operator|.
name|around
argument_list|(
name|ignoreAfterMaxFailures
argument_list|)
operator|.
name|around
argument_list|(
name|threadAndTestNameRule
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|SystemPropertiesInvariantRule
argument_list|(
name|IGNORED_INVARIANT_PROPERTIES
argument_list|)
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|TestRuleSetupAndRestoreInstanceEnv
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
operator|new
name|TestRuleFieldCacheSanity
argument_list|()
argument_list|)
operator|.
name|around
argument_list|(
name|parentChainCallRule
argument_list|)
decl_stmt|;
comment|// -----------------------------------------------------------------
comment|// Suite and test case setup/ cleanup.
comment|// -----------------------------------------------------------------
comment|/**      * For subclasses to override. Overrides must call {@code super.setUp()}.      */
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|parentChainCallRule
operator|.
name|setupCalled
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * For subclasses to override. Overrides must call {@code super.tearDown()}.      */
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|parentChainCallRule
operator|.
name|teardownCalled
operator|=
literal|true
expr_stmt|;
block|}
comment|// -----------------------------------------------------------------
comment|// Test facilities and facades for subclasses.
comment|// -----------------------------------------------------------------
comment|/**      * Registers a {@link Closeable} resource that should be closed after the test      * completes.      *      * @return<code>resource</code> (for call chaining).      */
DECL|method|closeAfterTest
specifier|public
parameter_list|<
name|T
extends|extends
name|Closeable
parameter_list|>
name|T
name|closeAfterTest
parameter_list|(
name|T
name|resource
parameter_list|)
block|{
return|return
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|closeAtEnd
argument_list|(
name|resource
argument_list|,
name|LifecycleScope
operator|.
name|TEST
argument_list|)
return|;
block|}
comment|/**      * Registers a {@link Closeable} resource that should be closed after the suite      * completes.      *      * @return<code>resource</code> (for call chaining).      */
DECL|method|closeAfterSuite
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Closeable
parameter_list|>
name|T
name|closeAfterSuite
parameter_list|(
name|T
name|resource
parameter_list|)
block|{
return|return
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|closeAtEnd
argument_list|(
name|resource
argument_list|,
name|LifecycleScope
operator|.
name|SUITE
argument_list|)
return|;
block|}
comment|/**      * Return the current class being tested.      */
DECL|method|getTestClass
specifier|public
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|getTestClass
parameter_list|()
block|{
return|return
name|classNameRule
operator|.
name|getTestClass
argument_list|()
return|;
block|}
comment|/**      * Return the name of the currently executing test case.      */
DECL|method|getTestName
specifier|public
name|String
name|getTestName
parameter_list|()
block|{
return|return
name|threadAndTestNameRule
operator|.
name|testMethodName
return|;
block|}
block|}
end_class

end_unit

