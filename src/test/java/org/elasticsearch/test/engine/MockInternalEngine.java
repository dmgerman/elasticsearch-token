begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|engine
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
name|AssertingDirectoryReader
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
name|IndexReader
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
name|search
operator|.
name|AssertingIndexSearcher
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|SearcherManager
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
name|common
operator|.
name|Nullable
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
name|inject
operator|.
name|Inject
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|codec
operator|.
name|CodecService
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
name|deletionpolicy
operator|.
name|SnapshotDeletionPolicy
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
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|EngineException
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
name|engine
operator|.
name|internal
operator|.
name|InternalEngine
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
name|indexing
operator|.
name|ShardIndexingService
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
name|merge
operator|.
name|policy
operator|.
name|MergePolicyProvider
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
name|merge
operator|.
name|scheduler
operator|.
name|MergeSchedulerProvider
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
name|settings
operator|.
name|IndexSettings
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
name|settings
operator|.
name|IndexSettingsService
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
name|shard
operator|.
name|ShardId
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
name|similarity
operator|.
name|SimilarityService
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
name|store
operator|.
name|Store
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
name|translog
operator|.
name|Translog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|warmer
operator|.
name|IndicesWarmer
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
name|TestCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|ConcurrentHashMap
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
name|ConcurrentMap
import|;
end_import

begin_class
DECL|class|MockInternalEngine
specifier|public
specifier|final
class|class
name|MockInternalEngine
extends|extends
name|InternalEngine
implements|implements
name|Engine
block|{
DECL|field|INFLIGHT_ENGINE_SEARCHERS
specifier|public
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|INFLIGHT_ENGINE_SEARCHERS
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|WRAP_READER_RATIO
specifier|public
specifier|static
specifier|final
name|String
name|WRAP_READER_RATIO
init|=
literal|"index.engine.mock.random.wrap_reader_ratio"
decl_stmt|;
DECL|field|READER_WRAPPER_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|READER_WRAPPER_TYPE
init|=
literal|"index.engine.mock.random.wrapper"
decl_stmt|;
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|wrapReader
specifier|private
specifier|final
name|boolean
name|wrapReader
decl_stmt|;
DECL|field|wrapper
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|FilterDirectoryReader
argument_list|>
name|wrapper
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockInternalEngine
specifier|public
name|MockInternalEngine
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndexSettingsService
name|indexSettingsService
parameter_list|,
name|ShardIndexingService
name|indexingService
parameter_list|,
annotation|@
name|Nullable
name|IndicesWarmer
name|warmer
parameter_list|,
name|Store
name|store
parameter_list|,
name|SnapshotDeletionPolicy
name|deletionPolicy
parameter_list|,
name|Translog
name|translog
parameter_list|,
name|MergePolicyProvider
name|mergePolicyProvider
parameter_list|,
name|MergeSchedulerProvider
name|mergeScheduler
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
name|SimilarityService
name|similarityService
parameter_list|,
name|CodecService
name|codecService
parameter_list|)
throws|throws
name|EngineException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|threadPool
argument_list|,
name|indexSettingsService
argument_list|,
name|indexingService
argument_list|,
name|warmer
argument_list|,
name|store
argument_list|,
name|deletionPolicy
argument_list|,
name|translog
argument_list|,
name|mergePolicyProvider
argument_list|,
name|mergeScheduler
argument_list|,
name|analysisService
argument_list|,
name|similarityService
argument_list|,
name|codecService
argument_list|)
expr_stmt|;
specifier|final
name|long
name|seed
init|=
name|indexSettings
operator|.
name|getAsLong
argument_list|(
name|TestCluster
operator|.
name|SETTING_INDEX_SEED
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
specifier|final
name|double
name|ratio
init|=
name|indexSettings
operator|.
name|getAsDouble
argument_list|(
name|WRAP_READER_RATIO
argument_list|,
literal|0.0d
argument_list|)
decl_stmt|;
comment|// DISABLED by default - AssertingDR is crazy slow
name|wrapper
operator|=
name|indexSettings
operator|.
name|getAsClass
argument_list|(
name|READER_WRAPPER_TYPE
argument_list|,
name|AssertingDirectoryReader
operator|.
name|class
argument_list|)
expr_stmt|;
name|wrapReader
operator|=
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
name|ratio
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Using [{}] for shard [{}] seed: [{}] wrapReader: [{}]"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|shardId
argument_list|,
name|seed
argument_list|,
name|wrapReader
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
comment|// log debug if we have pending searchers
for|for
control|(
name|Entry
argument_list|<
name|MockInternalEngine
operator|.
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|entry
range|:
name|MockInternalEngine
operator|.
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Unreleased Searchers instance for shard [{}]"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|newSearcher
specifier|protected
name|Searcher
name|newSearcher
parameter_list|(
name|String
name|source
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|SearcherManager
name|manager
parameter_list|)
throws|throws
name|EngineException
block|{
name|IndexReader
name|reader
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
name|IndexReader
name|wrappedReader
init|=
name|reader
decl_stmt|;
if|if
condition|(
name|reader
operator|instanceof
name|DirectoryReader
operator|&&
name|wrapReader
condition|)
block|{
name|wrappedReader
operator|=
name|wrapReader
argument_list|(
operator|(
name|DirectoryReader
operator|)
name|reader
argument_list|)
expr_stmt|;
block|}
comment|// this executes basic query checks and asserts that weights are normalized only once etc.
specifier|final
name|AssertingIndexSearcher
name|assertingIndexSearcher
init|=
operator|new
name|AssertingIndexSearcher
argument_list|(
name|random
argument_list|,
name|wrappedReader
argument_list|)
decl_stmt|;
name|assertingIndexSearcher
operator|.
name|setSimilarity
argument_list|(
name|searcher
operator|.
name|getSimilarity
argument_list|()
argument_list|)
expr_stmt|;
comment|// pass the original searcher to the super.newSearcher() method to make sure this is the searcher that will
comment|// be released later on. If we wrap an index reader here must not pass the wrapped version to the manager
comment|// on release otherwise the reader will be closed too early. - good news, stuff will fail all over the place if we don't get this right here
return|return
operator|new
name|AssertingSearcher
argument_list|(
name|assertingIndexSearcher
argument_list|,
name|super
operator|.
name|newSearcher
argument_list|(
name|source
argument_list|,
name|searcher
argument_list|,
name|manager
argument_list|)
argument_list|,
name|shardId
argument_list|)
return|;
block|}
DECL|method|wrapReader
specifier|private
name|DirectoryReader
name|wrapReader
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|)
block|{
try|try
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
index|[]
name|constructors
init|=
name|wrapper
operator|.
name|getConstructors
argument_list|()
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|nonRandom
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
range|:
name|constructors
control|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|parameterTypes
init|=
name|constructor
operator|.
name|getParameterTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameterTypes
operator|.
name|length
operator|>
literal|0
operator|&&
name|parameterTypes
index|[
literal|0
index|]
operator|==
name|DirectoryReader
operator|.
name|class
condition|)
block|{
if|if
condition|(
name|parameterTypes
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|nonRandom
operator|=
name|constructor
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parameterTypes
operator|.
name|length
operator|==
literal|2
operator|&&
name|parameterTypes
index|[
literal|1
index|]
operator|==
name|Settings
operator|.
name|class
condition|)
block|{
return|return
operator|(
name|DirectoryReader
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|reader
argument_list|,
name|indexSettings
argument_list|)
return|;
block|}
block|}
block|}
if|if
condition|(
name|nonRandom
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
name|DirectoryReader
operator|)
name|nonRandom
operator|.
name|newInstance
argument_list|(
name|reader
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Can not wrap reader"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|reader
return|;
block|}
DECL|class|AssertingSearcher
specifier|public
specifier|final
class|class
name|AssertingSearcher
implements|implements
name|Searcher
block|{
DECL|field|wrappedSearcher
specifier|private
specifier|final
name|Searcher
name|wrappedSearcher
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|indexSearcher
specifier|private
specifier|final
name|IndexSearcher
name|indexSearcher
decl_stmt|;
DECL|field|firstReleaseStack
specifier|private
name|RuntimeException
name|firstReleaseStack
decl_stmt|;
DECL|field|lock
specifier|private
specifier|final
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|initialRefCount
specifier|private
specifier|final
name|int
name|initialRefCount
decl_stmt|;
DECL|method|AssertingSearcher
specifier|public
name|AssertingSearcher
parameter_list|(
name|IndexSearcher
name|indexSearcher
parameter_list|,
name|Searcher
name|wrappedSearcher
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
comment|// we only use the given index searcher here instead of the IS of the wrapped searcher. the IS might be a wrapped searcher
comment|// with a wrapped reader.
name|this
operator|.
name|wrappedSearcher
operator|=
name|wrappedSearcher
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|initialRefCount
operator|=
name|wrappedSearcher
operator|.
name|reader
argument_list|()
operator|.
name|getRefCount
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexSearcher
operator|=
name|indexSearcher
expr_stmt|;
assert|assert
name|initialRefCount
operator|>
literal|0
operator|:
literal|"IndexReader#getRefCount() was ["
operator|+
name|initialRefCount
operator|+
literal|"] expected a value> [0] - reader is already closed"
assert|;
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|put
argument_list|(
name|this
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"Unreleased Searcher, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
name|wrappedSearcher
operator|.
name|source
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|release
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|RuntimeException
name|remove
init|=
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|remove
argument_list|(
name|this
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
comment|// make sure we only get this once and store the stack of the first caller!
if|if
condition|(
name|remove
operator|==
literal|null
condition|)
block|{
assert|assert
name|firstReleaseStack
operator|!=
literal|null
assert|;
name|AssertionError
name|error
init|=
operator|new
name|AssertionError
argument_list|(
literal|"Released Searcher more than once, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
decl_stmt|;
name|error
operator|.
name|initCause
argument_list|(
name|firstReleaseStack
argument_list|)
expr_stmt|;
throw|throw
name|error
throw|;
block|}
else|else
block|{
assert|assert
name|firstReleaseStack
operator|==
literal|null
assert|;
name|firstReleaseStack
operator|=
operator|new
name|RuntimeException
argument_list|(
literal|"Searcher Released first here, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|refCount
init|=
name|wrappedSearcher
operator|.
name|reader
argument_list|()
operator|.
name|getRefCount
argument_list|()
decl_stmt|;
comment|// this assert seems to be paranoid but given LUCENE-5362 we better add some assertions here to make sure we catch any potential
comment|// problems.
assert|assert
name|refCount
operator|>
literal|0
operator|:
literal|"IndexReader#getRefCount() was ["
operator|+
name|refCount
operator|+
literal|"] expected a value> [0] - reader is already closed. Initial refCount was: ["
operator|+
name|initialRefCount
operator|+
literal|"]"
assert|;
try|try
block|{
return|return
name|wrappedSearcher
operator|.
name|release
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to release searcher"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|reader
specifier|public
name|IndexReader
name|reader
parameter_list|()
block|{
return|return
name|indexSearcher
operator|.
name|getIndexReader
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|searcher
specifier|public
name|IndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|indexSearcher
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
block|}
DECL|class|DirectoryReaderWrapper
specifier|public
specifier|static
specifier|abstract
class|class
name|DirectoryReaderWrapper
extends|extends
name|FilterDirectoryReader
block|{
DECL|field|subReaderWrapper
specifier|protected
specifier|final
name|SubReaderWrapper
name|subReaderWrapper
decl_stmt|;
DECL|method|DirectoryReaderWrapper
specifier|public
name|DirectoryReaderWrapper
parameter_list|(
name|DirectoryReader
name|in
parameter_list|,
name|SubReaderWrapper
name|subReaderWrapper
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|,
name|subReaderWrapper
argument_list|)
expr_stmt|;
name|this
operator|.
name|subReaderWrapper
operator|=
name|subReaderWrapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getCoreCacheKey
specifier|public
name|Object
name|getCoreCacheKey
parameter_list|()
block|{
return|return
name|in
operator|.
name|getCoreCacheKey
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getCombinedCoreAndDeletesKey
specifier|public
name|Object
name|getCombinedCoreAndDeletesKey
parameter_list|()
block|{
return|return
name|in
operator|.
name|getCombinedCoreAndDeletesKey
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

