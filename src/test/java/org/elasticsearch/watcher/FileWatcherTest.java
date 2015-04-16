begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.watcher
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|watcher
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
name|BufferedWriter
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
name|charset
operator|.
name|Charset
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
name|StandardOpenOption
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
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
name|contains
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressFileSystems
argument_list|(
literal|"ExtrasFS"
argument_list|)
DECL|class|FileWatcherTest
specifier|public
class|class
name|FileWatcherTest
extends|extends
name|ElasticsearchTestCase
block|{
DECL|class|RecordingChangeListener
specifier|private
class|class
name|RecordingChangeListener
extends|extends
name|FileChangesListener
block|{
DECL|field|rootDir
specifier|private
name|Path
name|rootDir
decl_stmt|;
DECL|method|RecordingChangeListener
specifier|private
name|RecordingChangeListener
parameter_list|(
name|Path
name|rootDir
parameter_list|)
block|{
name|this
operator|.
name|rootDir
operator|=
name|rootDir
expr_stmt|;
block|}
DECL|method|getRelativeFileName
specifier|private
name|String
name|getRelativeFileName
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
return|return
name|rootDir
operator|.
name|toUri
argument_list|()
operator|.
name|relativize
argument_list|(
name|file
operator|.
name|toUri
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
return|;
block|}
DECL|field|notifications
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|notifications
init|=
name|newArrayList
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|onFileInit
specifier|public
name|void
name|onFileInit
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onFileInit: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onDirectoryInit
specifier|public
name|void
name|onDirectoryInit
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onDirectoryInit: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFileCreated
specifier|public
name|void
name|onFileCreated
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onFileCreated: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFileDeleted
specifier|public
name|void
name|onFileDeleted
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onFileDeleted: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFileChanged
specifier|public
name|void
name|onFileChanged
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onFileChanged: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onDirectoryCreated
specifier|public
name|void
name|onDirectoryCreated
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onDirectoryCreated: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onDirectoryDeleted
specifier|public
name|void
name|onDirectoryDeleted
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|notifications
operator|.
name|add
argument_list|(
literal|"onDirectoryDeleted: "
operator|+
name|getRelativeFileName
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|notifications
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|notifications
parameter_list|()
block|{
return|return
name|notifications
return|;
block|}
block|}
annotation|@
name|Test
DECL|method|testSimpleFileOperations
specifier|public
name|void
name|testSimpleFileOperations
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testFile
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test.txt"
argument_list|)
decl_stmt|;
name|touch
argument_list|(
name|testFile
argument_list|)
expr_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testFile
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileInit: test.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|append
argument_list|(
literal|"Test"
argument_list|,
name|testFile
argument_list|,
name|Charset
operator|.
name|defaultCharset
argument_list|()
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileChanged: test.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testFile
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimpleDirectoryOperations
specifier|public
name|void
name|testSimpleDirectoryOperations
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test-dir"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onDirectoryInit: test-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/test.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/test0.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
name|i
operator|++
control|)
block|{
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test"
operator|+
name|i
operator|+
literal|".txt"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Make sure that first file is modified
name|append
argument_list|(
literal|"Test"
argument_list|,
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|,
name|Charset
operator|.
name|defaultCharset
argument_list|()
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileChanged: test-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test1.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test2.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test3.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test2.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test1.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test2.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test2.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test4.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test2.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test4.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test3.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test4.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test3.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test4.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|testDir
argument_list|)
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
block|}
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test2.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryDeleted: test-dir"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNestedDirectoryOperations
specifier|public
name|void
name|testNestedDirectoryOperations
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test-dir"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"sub-dir"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"sub-dir/test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onDirectoryInit: test-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryInit: test-dir/sub-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/sub-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/test.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create new file in subdirectory
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"sub-dir/test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/sub-dir/test1.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create new subdirectory in subdirectory
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"first-level"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"first-level/file1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"first-level/second-level"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"first-level/second-level/file2.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onDirectoryCreated: test-dir/first-level/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/first-level/file1.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryCreated: test-dir/first-level/second-level/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/first-level/second-level/file2.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Delete a directory, check notifications for
name|Path
name|path
init|=
name|testDir
operator|.
name|resolve
argument_list|(
literal|"first-level"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/first-level/file1.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/first-level/second-level/file2.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryDeleted: test-dir/first-level/second-level"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryDeleted: test-dir/first-level"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testFileReplacingDirectory
specifier|public
name|void
name|testFileReplacingDirectory
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test-dir"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
name|Path
name|subDir
init|=
name|testDir
operator|.
name|resolve
argument_list|(
literal|"sub-dir"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|subDir
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|subDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|subDir
operator|.
name|resolve
argument_list|(
literal|"test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onDirectoryInit: test-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryInit: test-dir/sub-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/sub-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileInit: test-dir/sub-dir/test1.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|subDir
argument_list|)
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|subDir
argument_list|)
expr_stmt|;
block|}
name|touch
argument_list|(
name|subDir
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/sub-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/sub-dir/test1.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryDeleted: test-dir/sub-dir"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/sub-dir"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|subDir
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|subDir
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/sub-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onDirectoryCreated: test-dir/sub-dir/"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testEmptyDirectory
specifier|public
name|void
name|testEmptyDirectory
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test-dir"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileDeleted: test-dir/test1.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNoDirectoryOnInit
specifier|public
name|void
name|testNoDirectoryOnInit
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testDir
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"test-dir"
argument_list|)
decl_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testDir
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test0.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|touch
argument_list|(
name|testDir
operator|.
name|resolve
argument_list|(
literal|"test1.txt"
argument_list|)
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onDirectoryCreated: test-dir/"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test0.txt"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"onFileCreated: test-dir/test1.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNoFileOnInit
specifier|public
name|void
name|testNoFileOnInit
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|tempDir
init|=
name|newTempDirPath
argument_list|()
decl_stmt|;
name|RecordingChangeListener
name|changes
init|=
operator|new
name|RecordingChangeListener
argument_list|(
name|tempDir
argument_list|)
decl_stmt|;
name|Path
name|testFile
init|=
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"testfile.txt"
argument_list|)
decl_stmt|;
name|FileWatcher
name|fileWatcher
init|=
operator|new
name|FileWatcher
argument_list|(
name|testFile
argument_list|)
decl_stmt|;
name|fileWatcher
operator|.
name|addListener
argument_list|(
name|changes
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|changes
operator|.
name|notifications
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|touch
argument_list|(
name|testFile
argument_list|)
expr_stmt|;
name|fileWatcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|changes
operator|.
name|notifications
argument_list|()
argument_list|,
name|contains
argument_list|(
name|equalTo
argument_list|(
literal|"onFileCreated: testfile.txt"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|touch
specifier|static
name|void
name|touch
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Files
operator|.
name|newOutputStream
argument_list|(
name|path
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|append
specifier|static
name|void
name|append
parameter_list|(
name|String
name|string
parameter_list|,
name|Path
name|path
parameter_list|,
name|Charset
name|cs
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|BufferedWriter
name|writer
init|=
name|Files
operator|.
name|newBufferedWriter
argument_list|(
name|path
argument_list|,
name|cs
argument_list|,
name|StandardOpenOption
operator|.
name|APPEND
argument_list|)
init|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

