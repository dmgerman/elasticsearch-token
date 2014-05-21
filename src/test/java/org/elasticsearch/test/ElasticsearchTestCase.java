begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|*
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
name|ThreadLeakScope
operator|.
name|Scope
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
name|base
operator|.
name|Predicate
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
name|ImmutableList
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
name|store
operator|.
name|MockDirectoryWrapper
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
name|AbstractRandomizedTest
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
name|Requests
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
name|logging
operator|.
name|ESLogger
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsAbortPolicy
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
name|EsRejectedExecutionException
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
name|XContentType
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
name|cache
operator|.
name|recycler
operator|.
name|MockBigArrays
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
name|cache
operator|.
name|recycler
operator|.
name|MockPageCacheRecycler
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
name|elasticsearch
operator|.
name|test
operator|.
name|store
operator|.
name|MockDirectoryHelper
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
name|AfterClass
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
name|io
operator|.
name|IOException
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
name|Field
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
name|Modifier
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
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAllFilesClosed
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAllSearchersClosed
import|;
end_import

begin_comment
comment|/**  * Base testcase for randomized unit testing with Elasticsearch  */
end_comment

begin_class
annotation|@
name|ThreadLeakFilters
argument_list|(
name|defaultFilters
operator|=
literal|true
argument_list|,
name|filters
operator|=
block|{
name|ElasticsearchThreadFilter
operator|.
name|class
block|}
argument_list|)
annotation|@
name|ThreadLeakScope
argument_list|(
name|Scope
operator|.
name|NONE
argument_list|)
annotation|@
name|TimeoutSuite
argument_list|(
name|millis
operator|=
literal|20
operator|*
name|TimeUnits
operator|.
name|MINUTE
argument_list|)
comment|// timeout the suite after 20min and fail the test.
annotation|@
name|Listeners
argument_list|(
name|LoggingListener
operator|.
name|class
argument_list|)
DECL|class|ElasticsearchTestCase
specifier|public
specifier|abstract
class|class
name|ElasticsearchTestCase
extends|extends
name|AbstractRandomizedTest
block|{
DECL|field|defaultHandler
specifier|private
specifier|static
name|Thread
operator|.
name|UncaughtExceptionHandler
name|defaultHandler
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|CHILD_VM_ID
specifier|public
specifier|static
specifier|final
name|String
name|CHILD_VM_ID
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"junit4.childvm.id"
argument_list|,
literal|""
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|TESTS_SECURITY_MANAGER
specifier|public
specifier|static
specifier|final
name|String
name|TESTS_SECURITY_MANAGER
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.security.manager"
argument_list|)
decl_stmt|;
DECL|field|JAVA_SECURTY_POLICY
specifier|public
specifier|static
specifier|final
name|String
name|JAVA_SECURTY_POLICY
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.security.policy"
argument_list|)
decl_stmt|;
DECL|field|ASSERTIONS_ENABLED
specifier|public
specifier|static
specifier|final
name|boolean
name|ASSERTIONS_ENABLED
decl_stmt|;
static|static
block|{
name|boolean
name|enabled
init|=
literal|false
decl_stmt|;
assert|assert
name|enabled
operator|=
literal|true
assert|;
name|ASSERTIONS_ENABLED
operator|=
name|enabled
expr_stmt|;
if|if
condition|(
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|TESTS_SECURITY_MANAGER
argument_list|)
condition|?
name|TESTS_SECURITY_MANAGER
else|:
literal|"true"
argument_list|)
operator|&&
name|JAVA_SECURTY_POLICY
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|SecurityManager
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|awaitBusy
specifier|public
specifier|static
name|boolean
name|awaitBusy
parameter_list|(
name|Predicate
argument_list|<
name|?
argument_list|>
name|breakPredicate
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
name|awaitBusy
argument_list|(
name|breakPredicate
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
DECL|method|awaitBusy
specifier|public
specifier|static
name|boolean
name|awaitBusy
parameter_list|(
name|Predicate
argument_list|<
name|?
argument_list|>
name|breakPredicate
parameter_list|,
name|long
name|maxWaitTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|maxTimeInMillis
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|maxWaitTime
argument_list|,
name|unit
argument_list|)
decl_stmt|;
name|long
name|iterations
init|=
name|Math
operator|.
name|max
argument_list|(
name|Math
operator|.
name|round
argument_list|(
name|Math
operator|.
name|log10
argument_list|(
name|maxTimeInMillis
argument_list|)
operator|/
name|Math
operator|.
name|log10
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|long
name|timeInMillis
init|=
literal|1
decl_stmt|;
name|long
name|sum
init|=
literal|0
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
name|iterations
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|breakPredicate
operator|.
name|apply
argument_list|(
literal|null
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|sum
operator|+=
name|timeInMillis
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|timeInMillis
argument_list|)
expr_stmt|;
name|timeInMillis
operator|*=
literal|2
expr_stmt|;
block|}
name|timeInMillis
operator|=
name|maxTimeInMillis
operator|-
name|sum
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|timeInMillis
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|breakPredicate
operator|.
name|apply
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|field|numericTypes
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|numericTypes
init|=
operator|new
name|String
index|[]
block|{
literal|"byte"
block|,
literal|"short"
block|,
literal|"integer"
block|,
literal|"long"
block|}
decl_stmt|;
DECL|method|randomNumericType
specifier|public
specifier|static
name|String
name|randomNumericType
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|numericTypes
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|numericTypes
operator|.
name|length
argument_list|)
index|]
return|;
block|}
comment|/**      * Returns a {@link File} pointing to the class path relative resource given      * as the first argument. In contrast to      *<code>getClass().getResource(...).getFile()</code> this method will not      * return URL encoded paths if the parent path contains spaces or other      * non-standard characters.      */
DECL|method|getResource
specifier|public
name|File
name|getResource
parameter_list|(
name|String
name|relativePath
parameter_list|)
block|{
name|URI
name|uri
init|=
name|URI
operator|.
name|create
argument_list|(
name|getClass
argument_list|()
operator|.
name|getResource
argument_list|(
name|relativePath
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|File
argument_list|(
name|uri
argument_list|)
return|;
block|}
annotation|@
name|After
DECL|method|ensureAllPagesReleased
specifier|public
name|void
name|ensureAllPagesReleased
parameter_list|()
throws|throws
name|Exception
block|{
name|MockPageCacheRecycler
operator|.
name|ensureAllPagesAreReleased
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|ensureAllArraysReleased
specifier|public
name|void
name|ensureAllArraysReleased
parameter_list|()
throws|throws
name|Exception
block|{
name|MockBigArrays
operator|.
name|ensureAllArraysAreReleased
argument_list|()
expr_stmt|;
block|}
DECL|method|hasUnclosedWrapper
specifier|public
specifier|static
name|boolean
name|hasUnclosedWrapper
parameter_list|()
block|{
for|for
control|(
name|MockDirectoryWrapper
name|w
range|:
name|MockDirectoryHelper
operator|.
name|wrappers
control|)
block|{
if|if
condition|(
name|w
operator|.
name|isOpen
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|BeforeClass
DECL|method|setBeforeClass
specifier|public
specifier|static
name|void
name|setBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|closeAfterSuite
argument_list|(
operator|new
name|Closeable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|assertAllFilesClosed
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|closeAfterSuite
argument_list|(
operator|new
name|Closeable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|assertAllSearchersClosed
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|defaultHandler
operator|=
name|Thread
operator|.
name|getDefaultUncaughtExceptionHandler
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
operator|new
name|ElasticsearchUncaughtExceptionHandler
argument_list|(
name|defaultHandler
argument_list|)
argument_list|)
expr_stmt|;
name|Requests
operator|.
name|CONTENT_TYPE
operator|=
name|randomXContentType
argument_list|()
expr_stmt|;
name|Requests
operator|.
name|INDEX_CONTENT_TYPE
operator|=
name|randomXContentType
argument_list|()
expr_stmt|;
block|}
DECL|method|randomXContentType
specifier|private
specifier|static
name|XContentType
name|randomXContentType
parameter_list|()
block|{
return|return
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|AfterClass
DECL|method|resetAfterClass
specifier|public
specifier|static
name|void
name|resetAfterClass
parameter_list|()
block|{
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
name|defaultHandler
argument_list|)
expr_stmt|;
name|Requests
operator|.
name|CONTENT_TYPE
operator|=
name|XContentType
operator|.
name|SMILE
expr_stmt|;
name|Requests
operator|.
name|INDEX_CONTENT_TYPE
operator|=
name|XContentType
operator|.
name|JSON
expr_stmt|;
block|}
DECL|method|maybeDocValues
specifier|public
specifier|static
name|boolean
name|maybeDocValues
parameter_list|()
block|{
return|return
name|LuceneTestCase
operator|.
name|defaultCodecSupportsSortedSet
argument_list|()
operator|&&
name|randomBoolean
argument_list|()
return|;
block|}
DECL|field|SORTED_VERSIONS
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Version
argument_list|>
name|SORTED_VERSIONS
decl_stmt|;
static|static
block|{
name|Field
index|[]
name|declaredFields
init|=
name|Version
operator|.
name|class
operator|.
name|getDeclaredFields
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|ids
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|declaredFields
control|)
block|{
specifier|final
name|int
name|mod
init|=
name|field
operator|.
name|getModifiers
argument_list|()
decl_stmt|;
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|mod
argument_list|)
operator|&&
name|Modifier
operator|.
name|isFinal
argument_list|(
name|mod
argument_list|)
operator|&&
name|Modifier
operator|.
name|isPublic
argument_list|(
name|mod
argument_list|)
condition|)
block|{
if|if
condition|(
name|field
operator|.
name|getType
argument_list|()
operator|==
name|Version
operator|.
name|class
condition|)
block|{
try|try
block|{
name|Version
name|object
init|=
operator|(
name|Version
operator|)
name|field
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|ids
operator|.
name|add
argument_list|(
name|object
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|idList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ids
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|idList
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|reverse
argument_list|(
name|idList
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Version
argument_list|>
name|version
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|integer
range|:
name|idList
control|)
block|{
name|version
operator|.
name|add
argument_list|(
name|Version
operator|.
name|fromId
argument_list|(
name|integer
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SORTED_VERSIONS
operator|=
name|version
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|getPreviousVersion
specifier|public
specifier|static
name|Version
name|getPreviousVersion
parameter_list|()
block|{
name|Version
name|version
init|=
name|SORTED_VERSIONS
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
assert|;
return|return
name|version
return|;
block|}
DECL|method|randomVersion
specifier|public
specifier|static
name|Version
name|randomVersion
parameter_list|()
block|{
return|return
name|randomVersion
argument_list|(
name|getRandom
argument_list|()
argument_list|)
return|;
block|}
DECL|method|randomVersion
specifier|public
specifier|static
name|Version
name|randomVersion
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|SORTED_VERSIONS
operator|.
name|get
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|SORTED_VERSIONS
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
DECL|class|ElasticsearchUncaughtExceptionHandler
specifier|static
specifier|final
class|class
name|ElasticsearchUncaughtExceptionHandler
implements|implements
name|Thread
operator|.
name|UncaughtExceptionHandler
block|{
DECL|field|parent
specifier|private
specifier|final
name|Thread
operator|.
name|UncaughtExceptionHandler
name|parent
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|ElasticsearchUncaughtExceptionHandler
specifier|private
name|ElasticsearchUncaughtExceptionHandler
parameter_list|(
name|Thread
operator|.
name|UncaughtExceptionHandler
name|parent
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|uncaughtException
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|EsRejectedExecutionException
condition|)
block|{
if|if
condition|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|EsAbortPolicy
operator|.
name|SHUTTING_DOWN_KEY
argument_list|)
condition|)
block|{
return|return;
comment|// ignore the EsRejectedExecutionException when a node shuts down
block|}
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
if|if
condition|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"unable to create new native thread"
argument_list|)
condition|)
block|{
name|printStackDump
argument_list|(
name|logger
argument_list|)
expr_stmt|;
block|}
block|}
name|parent
operator|.
name|uncaughtException
argument_list|(
name|t
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|printStackDump
specifier|protected
specifier|static
specifier|final
name|void
name|printStackDump
parameter_list|(
name|ESLogger
name|logger
parameter_list|)
block|{
comment|// print stack traces if we can't create any native thread anymore
name|Map
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|allStackTraces
init|=
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
decl_stmt|;
name|logger
operator|.
name|error
argument_list|(
name|formatThreadStacks
argument_list|(
name|allStackTraces
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Dump threads and their current stack trace.      */
DECL|method|formatThreadStacks
specifier|private
specifier|static
name|String
name|formatThreadStacks
parameter_list|(
name|Map
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|threads
parameter_list|)
block|{
name|StringBuilder
name|message
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|cnt
init|=
literal|1
decl_stmt|;
specifier|final
name|Formatter
name|f
init|=
operator|new
name|Formatter
argument_list|(
name|message
argument_list|,
name|Locale
operator|.
name|ENGLISH
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|e
range|:
name|threads
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|isAlive
argument_list|()
condition|)
name|f
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|,
literal|"\n  %2d) %s"
argument_list|,
name|cnt
operator|++
argument_list|,
name|threadName
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|flush
argument_list|()
expr_stmt|;
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|message
operator|.
name|append
argument_list|(
literal|"\n        at (empty stack)"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|StackTraceElement
name|ste
range|:
name|e
operator|.
name|getValue
argument_list|()
control|)
block|{
name|message
operator|.
name|append
argument_list|(
literal|"\n        at "
argument_list|)
operator|.
name|append
argument_list|(
name|ste
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|message
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|threadName
specifier|private
specifier|static
name|String
name|threadName
parameter_list|(
name|Thread
name|t
parameter_list|)
block|{
return|return
literal|"Thread["
operator|+
literal|"id="
operator|+
name|t
operator|.
name|getId
argument_list|()
operator|+
literal|", name="
operator|+
name|t
operator|.
name|getName
argument_list|()
operator|+
literal|", state="
operator|+
name|t
operator|.
name|getState
argument_list|()
operator|+
literal|", group="
operator|+
name|groupName
argument_list|(
name|t
operator|.
name|getThreadGroup
argument_list|()
argument_list|)
operator|+
literal|"]"
return|;
block|}
DECL|method|groupName
specifier|private
specifier|static
name|String
name|groupName
parameter_list|(
name|ThreadGroup
name|threadGroup
parameter_list|)
block|{
if|if
condition|(
name|threadGroup
operator|==
literal|null
condition|)
block|{
return|return
literal|"{null group}"
return|;
block|}
else|else
block|{
return|return
name|threadGroup
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
DECL|method|randomFrom
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|randomFrom
parameter_list|(
name|T
modifier|...
name|values
parameter_list|)
block|{
return|return
name|RandomizedTest
operator|.
name|randomFrom
argument_list|(
name|values
argument_list|)
return|;
block|}
DECL|method|generateRandomStringArray
specifier|public
specifier|static
name|String
index|[]
name|generateRandomStringArray
parameter_list|(
name|int
name|maxArraySize
parameter_list|,
name|int
name|maxStringSize
parameter_list|,
name|boolean
name|allowNull
parameter_list|)
block|{
if|if
condition|(
name|allowNull
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
index|[]
name|array
init|=
operator|new
name|String
index|[
name|randomInt
argument_list|(
name|maxArraySize
argument_list|)
index|]
decl_stmt|;
comment|// allow empty arrays
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|array
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|array
index|[
name|i
index|]
operator|=
name|randomAsciiOfLength
argument_list|(
name|maxStringSize
argument_list|)
expr_stmt|;
block|}
return|return
name|array
return|;
block|}
DECL|method|generateRandomStringArray
specifier|public
specifier|static
name|String
index|[]
name|generateRandomStringArray
parameter_list|(
name|int
name|maxArraySize
parameter_list|,
name|int
name|maxStringSize
parameter_list|)
block|{
return|return
name|generateRandomStringArray
argument_list|(
name|maxArraySize
argument_list|,
name|maxStringSize
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
end_class

end_unit

