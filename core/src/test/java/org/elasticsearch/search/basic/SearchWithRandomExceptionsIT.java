begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.basic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|basic
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DirectoryReader
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
name|index
operator|.
name|FilterDirectoryReader
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
name|index
operator|.
name|LeafReader
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
name|English
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|DocWriteResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|RefreshResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchPhaseExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|settings
operator|.
name|Settings
operator|.
name|Builder
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
name|unit
operator|.
name|TimeValue
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
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|MockEngineFactoryPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortOrder
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
name|ESIntegTestCase
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
name|engine
operator|.
name|MockEngineSupport
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
name|engine
operator|.
name|ThrowingLeafReaderWrapper
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Random
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
name|ExecutionException
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
name|assertAcked
import|;
end_import

begin_class
DECL|class|SearchWithRandomExceptionsIT
specifier|public
class|class
name|SearchWithRandomExceptionsIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|RandomExceptionDirectoryReaderWrapper
operator|.
name|TestPlugin
operator|.
name|class
argument_list|,
name|MockEngineFactoryPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testRandomExceptions
specifier|public
name|void
name|testRandomExceptions
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
specifier|final
name|double
name|lowLevelRate
decl_stmt|;
specifier|final
name|double
name|topLevelRate
decl_stmt|;
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|lowLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|topLevelRate
operator|=
literal|0.0d
expr_stmt|;
block|}
else|else
block|{
name|topLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|lowLevelRate
operator|=
literal|0.0d
expr_stmt|;
block|}
block|}
else|else
block|{
name|lowLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|topLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// rarely no exception
name|topLevelRate
operator|=
literal|0d
expr_stmt|;
name|lowLevelRate
operator|=
literal|0d
expr_stmt|;
block|}
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
argument_list|,
name|topLevelRate
argument_list|)
operator|.
name|put
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
argument_list|,
name|lowLevelRate
argument_list|)
operator|.
name|put
argument_list|(
name|MockEngineSupport
operator|.
name|WRAP_READER_RATIO
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1.0d
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating index: [test] using settings: [{}]"
argument_list|,
name|settings
operator|.
name|build
argument_list|()
operator|.
name|getAsMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|between
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|numCreated
init|=
literal|0
decl_stmt|;
name|boolean
index|[]
name|added
init|=
operator|new
name|boolean
index|[
name|numDocs
index|]
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"test"
argument_list|,
name|English
operator|.
name|intToEnglish
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|indexResponse
operator|.
name|getResult
argument_list|()
operator|==
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
condition|)
block|{
name|numCreated
operator|++
expr_stmt|;
name|added
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|ex
parameter_list|)
block|{             }
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Start Refresh"
argument_list|)
expr_stmt|;
name|RefreshResponse
name|refreshResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// don't assert on failures here
specifier|final
name|boolean
name|refreshFailed
init|=
name|refreshResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
operator|!=
literal|0
operator|||
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
operator|!=
literal|0
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Refresh failed [{}] numShardsFailed: [{}], shardFailuresLength: [{}], successfulShards: [{}], totalShards: [{}] "
argument_list|,
name|refreshFailed
argument_list|,
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|refreshResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|refreshResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|)
expr_stmt|;
name|NumShards
name|test
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numSearches
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|)
decl_stmt|;
comment|// we don't check anything here really just making sure we don't leave any open files or a broken index behind.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numSearches
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|int
name|docToQuery
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|numDocs
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|expectedResults
init|=
name|added
index|[
name|docToQuery
index|]
condition|?
literal|1
else|:
literal|0
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Searching for [test:{}]"
argument_list|,
name|English
operator|.
name|intToEnglish
argument_list|(
name|docToQuery
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"test"
argument_list|,
name|English
operator|.
name|intToEnglish
argument_list|(
name|docToQuery
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
name|expectedResults
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Successful shards: [{}]  numShards: [{}]"
argument_list|,
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|test
operator|.
name|numPrimaries
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
operator|==
name|test
operator|.
name|numPrimaries
operator|&&
operator|!
name|refreshFailed
condition|)
block|{
name|assertResultsAndLogOnFailure
argument_list|(
name|expectedResults
argument_list|,
name|searchResponse
argument_list|)
expr_stmt|;
block|}
comment|// check match all
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
name|numCreated
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"_id"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Match all Successful shards: [{}]  numShards: [{}]"
argument_list|,
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|test
operator|.
name|numPrimaries
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
operator|==
name|test
operator|.
name|numPrimaries
operator|&&
operator|!
name|refreshFailed
condition|)
block|{
name|assertResultsAndLogOnFailure
argument_list|(
name|numCreated
argument_list|,
name|searchResponse
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"expected SearchPhaseException: [{}]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|field|EXCEPTION_TOP_LEVEL_RATIO_KEY
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
init|=
literal|"index.engine.exception.ratio.top"
decl_stmt|;
DECL|field|EXCEPTION_LOW_LEVEL_RATIO_KEY
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
init|=
literal|"index.engine.exception.ratio.low"
decl_stmt|;
DECL|class|RandomExceptionDirectoryReaderWrapper
specifier|public
specifier|static
class|class
name|RandomExceptionDirectoryReaderWrapper
extends|extends
name|MockEngineSupport
operator|.
name|DirectoryReaderWrapper
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
DECL|field|EXCEPTION_TOP_LEVEL_RATIO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|EXCEPTION_TOP_LEVEL_RATIO_SETTING
init|=
name|Setting
operator|.
name|doubleSetting
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|,
literal|0.0d
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
DECL|field|EXCEPTION_LOW_LEVEL_RATIO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|EXCEPTION_LOW_LEVEL_RATIO_SETTING
init|=
name|Setting
operator|.
name|doubleSetting
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|,
literal|0.0d
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_SETTING
argument_list|,
name|EXCEPTION_LOW_LEVEL_RATIO_SETTING
argument_list|)
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|MockEngineFactoryPlugin
operator|.
name|MockEngineReaderModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|setReaderClass
argument_list|(
name|RandomExceptionDirectoryReaderWrapper
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|class|ThrowingSubReaderWrapper
specifier|static
class|class
name|ThrowingSubReaderWrapper
extends|extends
name|FilterDirectoryReader
operator|.
name|SubReaderWrapper
implements|implements
name|ThrowingLeafReaderWrapper
operator|.
name|Thrower
block|{
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|topLevelRatio
specifier|private
specifier|final
name|double
name|topLevelRatio
decl_stmt|;
DECL|field|lowLevelRatio
specifier|private
specifier|final
name|double
name|lowLevelRatio
decl_stmt|;
DECL|method|ThrowingSubReaderWrapper
name|ThrowingSubReaderWrapper
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
specifier|final
name|long
name|seed
init|=
name|ESIntegTestCase
operator|.
name|INDEX_TEST_SEED_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|this
operator|.
name|topLevelRatio
operator|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|)
expr_stmt|;
name|this
operator|.
name|lowLevelRatio
operator|=
name|settings
operator|.
name|getAsDouble
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|)
expr_stmt|;
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|wrap
specifier|public
name|LeafReader
name|wrap
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
return|return
operator|new
name|ThrowingLeafReaderWrapper
argument_list|(
name|reader
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|maybeThrow
specifier|public
name|void
name|maybeThrow
parameter_list|(
name|ThrowingLeafReaderWrapper
operator|.
name|Flags
name|flag
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|flag
condition|)
block|{
case|case
name|Fields
case|:
case|case
name|TermVectors
case|:
case|case
name|Terms
case|:
case|case
name|TermsEnum
case|:
case|case
name|Intersect
case|:
case|case
name|Norms
case|:
case|case
name|NumericDocValues
case|:
case|case
name|BinaryDocValues
case|:
case|case
name|SortedDocValues
case|:
case|case
name|SortedSetDocValues
case|:
if|if
condition|(
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
name|topLevelRatio
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced top level Exception on ["
operator|+
name|flag
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
case|case
name|DocsEnum
case|:
case|case
name|DocsAndPositionsEnum
case|:
if|if
condition|(
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
name|lowLevelRatio
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced low level Exception on ["
operator|+
name|flag
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
block|}
block|}
annotation|@
name|Override
DECL|method|wrapTerms
specifier|public
name|boolean
name|wrapTerms
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
DECL|method|RandomExceptionDirectoryReaderWrapper
specifier|public
name|RandomExceptionDirectoryReaderWrapper
parameter_list|(
name|DirectoryReader
name|in
parameter_list|,
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
operator|new
name|ThrowingSubReaderWrapper
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWrapDirectoryReader
specifier|protected
name|DirectoryReader
name|doWrapDirectoryReader
parameter_list|(
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RandomExceptionDirectoryReaderWrapper
argument_list|(
name|in
argument_list|,
name|settings
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

