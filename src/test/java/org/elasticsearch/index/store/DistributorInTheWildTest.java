begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
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
name|ThreadLeakFilters
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
name|ThreadLeakLingering
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
name|index
operator|.
name|ThreadedIndexingAndSearchingTestCase
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
name|store
operator|.
name|Directory
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
name|LuceneTestCase
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
name|index
operator|.
name|store
operator|.
name|distributor
operator|.
name|Distributor
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
name|ElasticsearchThreadFilter
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
name|Before
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
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_comment
comment|/**  * This test is a copy of TestNRTThreads from lucene that puts some  * hard concurrent pressure on the directory etc. to ensure DistributorDirectory is behaving ok.  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressCodecs
argument_list|(
block|{
literal|"SimpleText"
block|,
literal|"Memory"
block|,
literal|"Direct"
block|}
argument_list|)
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
name|ThreadLeakScope
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
annotation|@
name|ThreadLeakLingering
argument_list|(
name|linger
operator|=
literal|5000
argument_list|)
comment|// 5 sec lingering
annotation|@
name|Listeners
argument_list|(
name|LoggingListener
operator|.
name|class
argument_list|)
annotation|@
name|LuceneTestCase
operator|.
name|SuppressSysoutChecks
argument_list|(
name|bugUrl
operator|=
literal|"we log a lot on purpose"
argument_list|)
DECL|class|DistributorInTheWildTest
specifier|public
class|class
name|DistributorInTheWildTest
extends|extends
name|ThreadedIndexingAndSearchingTestCase
block|{
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
DECL|field|useNonNrtReaders
specifier|private
name|boolean
name|useNonNrtReaders
init|=
literal|true
decl_stmt|;
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
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|useNonNrtReaders
operator|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doSearching
specifier|protected
name|void
name|doSearching
parameter_list|(
name|ExecutorService
name|es
parameter_list|,
name|long
name|stopTime
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|anyOpenDelFiles
init|=
literal|false
decl_stmt|;
name|DirectoryReader
name|r
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|stopTime
operator|&&
operator|!
name|failed
operator|.
name|get
argument_list|()
condition|)
block|{
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"TEST: now reopen r="
operator|+
name|r
argument_list|)
expr_stmt|;
block|}
specifier|final
name|DirectoryReader
name|r2
init|=
name|DirectoryReader
operator|.
name|openIfChanged
argument_list|(
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|r2
operator|!=
literal|null
condition|)
block|{
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|=
name|r2
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"TEST: now close reader="
operator|+
name|r
argument_list|)
expr_stmt|;
block|}
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|openDeletedFiles
init|=
name|getOpenDeletedFiles
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|openDeletedFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"OBD files: "
operator|+
name|openDeletedFiles
argument_list|)
expr_stmt|;
block|}
name|anyOpenDelFiles
operator||=
name|openDeletedFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
expr_stmt|;
comment|//assertEquals("open but deleted: " + openDeletedFiles, 0, openDeletedFiles.size());
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"TEST: now open"
argument_list|)
expr_stmt|;
block|}
name|r
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|VERBOSE
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"TEST: got new reader="
operator|+
name|r
argument_list|)
expr_stmt|;
block|}
comment|//logger.info("numDocs=" + r.numDocs() + "
comment|//openDelFileCount=" + dir.openDeleteFileCount());
if|if
condition|(
name|r
operator|.
name|numDocs
argument_list|()
operator|>
literal|0
condition|)
block|{
name|fixedSearcher
operator|=
operator|new
name|IndexSearcher
argument_list|(
name|r
argument_list|,
name|es
argument_list|)
expr_stmt|;
name|smokeTestSearcher
argument_list|(
name|fixedSearcher
argument_list|)
expr_stmt|;
name|runSearchThreads
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|500
argument_list|)
expr_stmt|;
block|}
block|}
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//logger.info("numDocs=" + r.numDocs() + " openDelFileCount=" + dir.openDeleteFileCount());
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|openDeletedFiles
init|=
name|getOpenDeletedFiles
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|openDeletedFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"OBD files: "
operator|+
name|openDeletedFiles
argument_list|)
expr_stmt|;
block|}
name|anyOpenDelFiles
operator||=
name|openDeletedFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
expr_stmt|;
name|assertFalse
argument_list|(
literal|"saw non-zero open-but-deleted count"
argument_list|,
name|anyOpenDelFiles
argument_list|)
expr_stmt|;
block|}
DECL|method|getOpenDeletedFiles
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getOpenDeletedFiles
parameter_list|(
name|Directory
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
operator|&&
name|dir
operator|instanceof
name|MockDirectoryWrapper
condition|)
block|{
return|return
operator|(
operator|(
name|MockDirectoryWrapper
operator|)
name|dir
operator|)
operator|.
name|getOpenDeletedFiles
argument_list|()
return|;
block|}
name|DistributorDirectory
name|d
init|=
name|dir
operator|instanceof
name|MockDirectoryWrapper
condition|?
call|(
name|DistributorDirectory
call|)
argument_list|(
operator|(
name|MockDirectoryWrapper
operator|)
name|dir
argument_list|)
operator|.
name|getDelegate
argument_list|()
else|:
operator|(
name|DistributorDirectory
operator|)
name|dir
decl_stmt|;
name|assertTrue
argument_list|(
name|DistributorDirectory
operator|.
name|assertConsistency
argument_list|(
name|logger
argument_list|,
name|d
argument_list|)
argument_list|)
expr_stmt|;
name|Distributor
name|distributor
init|=
name|d
operator|.
name|getDistributor
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Directory
name|subDir
range|:
name|distributor
operator|.
name|all
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|openDeletedFiles
init|=
operator|(
operator|(
name|MockDirectoryWrapper
operator|)
name|subDir
operator|)
operator|.
name|getOpenDeletedFiles
argument_list|()
decl_stmt|;
name|set
operator|.
name|addAll
argument_list|(
name|openDeletedFiles
argument_list|)
expr_stmt|;
block|}
return|return
name|set
return|;
block|}
annotation|@
name|Override
DECL|method|getDirectory
specifier|protected
name|Directory
name|getDirectory
parameter_list|(
name|Directory
name|in
parameter_list|)
block|{
assert|assert
name|in
operator|instanceof
name|MockDirectoryWrapper
assert|;
if|if
condition|(
operator|!
name|useNonNrtReaders
condition|)
operator|(
operator|(
name|MockDirectoryWrapper
operator|)
name|in
operator|)
operator|.
name|setAssertNoDeleteOpenFile
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Directory
index|[]
name|directories
init|=
operator|new
name|Directory
index|[
literal|1
operator|+
name|random
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
index|]
decl_stmt|;
name|directories
index|[
literal|0
index|]
operator|=
name|in
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|directories
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|File
name|tempDir
init|=
name|createTempDir
argument_list|(
name|getTestName
argument_list|()
argument_list|)
decl_stmt|;
name|directories
index|[
name|i
index|]
operator|=
name|newMockFSDirectory
argument_list|(
name|tempDir
argument_list|)
expr_stmt|;
comment|// some subclasses rely on this being MDW
if|if
condition|(
operator|!
name|useNonNrtReaders
condition|)
operator|(
operator|(
name|MockDirectoryWrapper
operator|)
name|directories
index|[
name|i
index|]
operator|)
operator|.
name|setAssertNoDeleteOpenFile
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Directory
name|dir
range|:
name|directories
control|)
block|{
operator|(
operator|(
name|MockDirectoryWrapper
operator|)
name|dir
operator|)
operator|.
name|setCheckIndexOnClose
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
return|return
operator|new
name|MockDirectoryWrapper
argument_list|(
name|random
argument_list|()
argument_list|,
operator|new
name|DistributorDirectory
argument_list|(
name|directories
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|DistributorDirectory
argument_list|(
name|directories
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doAfterWriter
specifier|protected
name|void
name|doAfterWriter
parameter_list|(
name|ExecutorService
name|es
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Force writer to do reader pooling, always, so that
comment|// all merged segments, even for merges before
comment|// doSearching is called, are warmed:
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|field|fixedSearcher
specifier|private
name|IndexSearcher
name|fixedSearcher
decl_stmt|;
annotation|@
name|Override
DECL|method|getCurrentSearcher
specifier|protected
name|IndexSearcher
name|getCurrentSearcher
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|fixedSearcher
return|;
block|}
annotation|@
name|Override
DECL|method|releaseSearcher
specifier|protected
name|void
name|releaseSearcher
parameter_list|(
name|IndexSearcher
name|s
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|s
operator|!=
name|fixedSearcher
condition|)
block|{
comment|// Final searcher:
name|s
operator|.
name|getIndexReader
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getFinalSearcher
specifier|protected
name|IndexSearcher
name|getFinalSearcher
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|IndexReader
name|r2
decl_stmt|;
if|if
condition|(
name|useNonNrtReaders
condition|)
block|{
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|r2
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|r2
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|r2
operator|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|newSearcher
argument_list|(
name|r2
argument_list|)
return|;
block|}
DECL|method|testNRTThreads
specifier|public
name|void
name|testNRTThreads
parameter_list|()
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
literal|"TestNRTThreads"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

