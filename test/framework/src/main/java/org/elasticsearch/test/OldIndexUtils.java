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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexWriter
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
name|TestUtil
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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|IndexSegments
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
name|segments
operator|.
name|IndexShardSegments
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
name|segments
operator|.
name|IndicesSegmentResponse
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
name|segments
operator|.
name|ShardSegments
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
name|upgrade
operator|.
name|get
operator|.
name|IndexUpgradeStatus
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
name|upgrade
operator|.
name|get
operator|.
name|UpgradeStatusResponse
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
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|ThrottlingAllocationDecider
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
name|FileSystemUtils
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
name|util
operator|.
name|IndexFolderUpgrader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
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
name|MergePolicyConfig
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
name|Segment
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
name|nio
operator|.
name|file
operator|.
name|DirectoryStream
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
name|FileVisitResult
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
name|SimpleFileVisitor
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
name|attribute
operator|.
name|BasicFileAttributes
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
name|Collection
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
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertTrue
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
name|ESTestCase
operator|.
name|randomInt
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
name|assertNoFailures
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_class
DECL|class|OldIndexUtils
specifier|public
class|class
name|OldIndexUtils
block|{
DECL|method|loadIndexesList
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|loadIndexesList
parameter_list|(
name|String
name|prefix
parameter_list|,
name|Path
name|bwcIndicesPath
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|indexes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|bwcIndicesPath
argument_list|,
name|prefix
operator|+
literal|"-*.zip"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|path
range|:
name|stream
control|)
block|{
name|indexes
operator|.
name|add
argument_list|(
name|path
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|indexes
argument_list|)
expr_stmt|;
return|return
name|indexes
return|;
block|}
DECL|method|getSettings
specifier|public
specifier|static
name|Settings
name|getSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MergePolicyConfig
operator|.
name|INDEX_MERGE_ENABLED
argument_list|,
literal|false
argument_list|)
comment|// disable merging so no segments will be upgraded
operator|.
name|put
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_INCOMING_RECOVERIES_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|30
argument_list|)
comment|//
comment|// speed up recoveries
operator|.
name|put
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_NODE_CONCURRENT_OUTGOING_RECOVERIES_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|30
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|upgradeIndexFolder
specifier|public
specifier|static
name|void
name|upgradeIndexFolder
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|,
name|String
name|nodeName
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|NodeEnvironment
name|nodeEnvironment
init|=
name|cluster
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|IndexFolderUpgrader
operator|.
name|upgradeIndicesIfNeeded
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|nodeEnvironment
argument_list|)
expr_stmt|;
block|}
DECL|method|loadIndex
specifier|public
specifier|static
name|void
name|loadIndex
parameter_list|(
name|String
name|indexName
parameter_list|,
name|String
name|indexFile
parameter_list|,
name|Path
name|unzipDir
parameter_list|,
name|Path
name|bwcPath
parameter_list|,
name|ESLogger
name|logger
parameter_list|,
name|Path
modifier|...
name|paths
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|unzipDataDir
init|=
name|unzipDir
operator|.
name|resolve
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
name|Path
name|backwardsIndex
init|=
name|bwcPath
operator|.
name|resolve
argument_list|(
name|indexFile
argument_list|)
decl_stmt|;
comment|// decompress the index
try|try
init|(
name|InputStream
name|stream
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|backwardsIndex
argument_list|)
init|)
block|{
name|TestUtil
operator|.
name|unzip
argument_list|(
name|stream
argument_list|,
name|unzipDir
argument_list|)
expr_stmt|;
block|}
comment|// check it is unique
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|unzipDataDir
argument_list|)
argument_list|)
expr_stmt|;
name|Path
index|[]
name|list
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|unzipDataDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Backwards index must contain exactly one cluster"
argument_list|)
throw|;
block|}
comment|// the bwc scripts packs the indices under this path
name|Path
name|src
init|=
name|list
index|[
literal|0
index|]
operator|.
name|resolve
argument_list|(
literal|"nodes/0/indices/"
operator|+
name|indexName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"["
operator|+
name|indexFile
operator|+
literal|"] missing index dir: "
operator|+
name|src
operator|.
name|toString
argument_list|()
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|copyIndex
argument_list|(
name|logger
argument_list|,
name|src
argument_list|,
name|indexName
argument_list|,
name|paths
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNotUpgraded
specifier|public
specifier|static
name|void
name|assertNotUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not be zero sized"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
comment|// TODO: it would be better for this to be strictly greater, but sometimes an extra flush
comment|// mysteriously happens after the second round of docs are indexed
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should have recovered some segments from transaction log"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|>=
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should need upgrading"
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|getUpgradeStatus
specifier|public
specifier|static
name|Collection
argument_list|<
name|IndexUpgradeStatus
argument_list|>
name|getUpgradeStatus
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
throws|throws
name|Exception
block|{
name|UpgradeStatusResponse
name|upgradeStatusResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpgradeStatus
argument_list|(
name|indices
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|upgradeStatusResponse
argument_list|)
expr_stmt|;
return|return
name|upgradeStatusResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|values
argument_list|()
return|;
block|}
comment|// randomly distribute the files from src over dests paths
DECL|method|copyIndex
specifier|public
specifier|static
name|void
name|copyIndex
parameter_list|(
specifier|final
name|ESLogger
name|logger
parameter_list|,
specifier|final
name|Path
name|src
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|,
specifier|final
name|Path
modifier|...
name|dests
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|destinationDataPath
init|=
name|dests
index|[
name|randomInt
argument_list|(
name|dests
operator|.
name|length
operator|-
literal|1
argument_list|)
index|]
decl_stmt|;
for|for
control|(
name|Path
name|dest
range|:
name|dests
control|)
block|{
name|Path
name|indexDir
init|=
name|dest
operator|.
name|resolve
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|indexDir
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|indexDir
argument_list|)
expr_stmt|;
block|}
name|Files
operator|.
name|walkFileTree
argument_list|(
name|src
argument_list|,
operator|new
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|preVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|relativeDir
init|=
name|src
operator|.
name|relativize
argument_list|(
name|dir
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|dest
range|:
name|dests
control|)
block|{
name|Path
name|destDir
init|=
name|dest
operator|.
name|resolve
argument_list|(
name|indexName
argument_list|)
operator|.
name|resolve
argument_list|(
name|relativeDir
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|destDir
argument_list|)
expr_stmt|;
block|}
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|IndexWriter
operator|.
name|WRITE_LOCK_NAME
argument_list|)
condition|)
block|{
comment|// skip lock file, we don't need it
name|logger
operator|.
name|trace
argument_list|(
literal|"Skipping lock file: {}"
argument_list|,
name|file
argument_list|)
expr_stmt|;
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
name|Path
name|relativeFile
init|=
name|src
operator|.
name|relativize
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|Path
name|destFile
init|=
name|destinationDataPath
operator|.
name|resolve
argument_list|(
name|indexName
argument_list|)
operator|.
name|resolve
argument_list|(
name|relativeFile
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"--> Moving {} to {}"
argument_list|,
name|relativeFile
argument_list|,
name|destFile
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|file
argument_list|,
name|destFile
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|destFile
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|assertUpgraded
specifier|public
specifier|static
name|void
name|assertUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|index
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should not be zero sized"
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index "
operator|+
name|status
operator|.
name|getIndex
argument_list|()
operator|+
literal|" should be upgraded"
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// double check using the segments api that all segments are actually upgraded
name|IndicesSegmentResponse
name|segsRsp
decl_stmt|;
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
name|segsRsp
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSegments
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|segsRsp
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareSegments
argument_list|(
name|index
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|IndexSegments
name|indexSegments
range|:
name|segsRsp
operator|.
name|getIndices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardSegments
name|shard
range|:
name|indexSegments
control|)
block|{
for|for
control|(
name|ShardSegments
name|segs
range|:
name|shard
operator|.
name|getShards
argument_list|()
control|)
block|{
for|for
control|(
name|Segment
name|seg
range|:
name|segs
operator|.
name|getSegments
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
literal|"Index "
operator|+
name|indexSegments
operator|.
name|getIndex
argument_list|()
operator|+
literal|" has unupgraded segment "
operator|+
name|seg
operator|.
name|toString
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|major
argument_list|,
name|seg
operator|.
name|version
operator|.
name|major
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Index "
operator|+
name|indexSegments
operator|.
name|getIndex
argument_list|()
operator|+
literal|" has unupgraded segment "
operator|+
name|seg
operator|.
name|toString
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|minor
argument_list|,
name|seg
operator|.
name|version
operator|.
name|minor
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|isUpgraded
specifier|public
specifier|static
name|boolean
name|isUpgraded
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|index
parameter_list|)
throws|throws
name|Exception
block|{
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|OldIndexUtils
operator|.
name|class
argument_list|)
decl_stmt|;
name|int
name|toUpgrade
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IndexUpgradeStatus
name|status
range|:
name|getUpgradeStatus
argument_list|(
name|client
argument_list|,
name|index
argument_list|)
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Index: {}, total: {}, toUpgrade: {}"
argument_list|,
name|status
operator|.
name|getIndex
argument_list|()
argument_list|,
name|status
operator|.
name|getTotalBytes
argument_list|()
argument_list|,
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
argument_list|)
expr_stmt|;
name|toUpgrade
operator|+=
name|status
operator|.
name|getToUpgradeBytes
argument_list|()
expr_stmt|;
block|}
return|return
name|toUpgrade
operator|==
literal|0
return|;
block|}
DECL|method|assertUpgradeWorks
specifier|public
specifier|static
name|void
name|assertUpgradeWorks
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|indexName
parameter_list|,
name|Version
name|version
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|OldIndexUtils
operator|.
name|isLatestLuceneVersion
argument_list|(
name|version
argument_list|)
operator|==
literal|false
condition|)
block|{
name|OldIndexUtils
operator|.
name|assertNotUpgraded
argument_list|(
name|client
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
block|}
name|assertNoFailures
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpgrade
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertUpgraded
argument_list|(
name|client
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
block|}
DECL|method|extractVersion
specifier|public
specifier|static
name|Version
name|extractVersion
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|Version
operator|.
name|fromString
argument_list|(
name|index
operator|.
name|substring
argument_list|(
name|index
operator|.
name|indexOf
argument_list|(
literal|'-'
argument_list|)
operator|+
literal|1
argument_list|,
name|index
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|isLatestLuceneVersion
specifier|public
specifier|static
name|boolean
name|isLatestLuceneVersion
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
return|return
name|version
operator|.
name|luceneVersion
operator|.
name|major
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|major
operator|&&
name|version
operator|.
name|luceneVersion
operator|.
name|minor
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|minor
return|;
block|}
block|}
end_class

end_unit

