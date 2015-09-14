begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|ESTestCase
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
name|SuppressFileSystems
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|net
operator|.
name|URISyntaxException
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
name|util
operator|.
name|Arrays
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
name|assertFileExists
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
name|assertFileNotExists
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  * Unit tests for {@link org.elasticsearch.common.io.FileSystemUtils}.  */
end_comment

begin_class
annotation|@
name|SuppressFileSystems
argument_list|(
literal|"WindowsFS"
argument_list|)
comment|// tries to move away open file handles
DECL|class|FileSystemUtilsTests
specifier|public
class|class
name|FileSystemUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|field|src
specifier|private
name|Path
name|src
decl_stmt|;
DECL|field|dst
specifier|private
name|Path
name|dst
decl_stmt|;
annotation|@
name|Before
DECL|method|copySourceFilesToTarget
specifier|public
name|void
name|copySourceFilesToTarget
parameter_list|()
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
name|src
operator|=
name|createTempDir
argument_list|()
expr_stmt|;
name|dst
operator|=
name|createTempDir
argument_list|()
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|src
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|dst
argument_list|)
expr_stmt|;
comment|// We first copy sources test files from src/test/resources
comment|// Because after when the test runs, src files are moved to their destination
specifier|final
name|Path
name|path
init|=
name|getDataPath
argument_list|(
literal|"/org/elasticsearch/common/io/copyappend"
argument_list|)
decl_stmt|;
name|FileSystemUtils
operator|.
name|copyDirectoryRecursively
argument_list|(
name|path
argument_list|,
name|src
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMoveOverExistingFileAndAppend
specifier|public
name|void
name|testMoveOverExistingFileAndAppend
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v1"
argument_list|)
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v2"
argument_list|)
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt.new"
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt.new"
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file3.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/subdir/file4.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v3"
argument_list|)
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt.new"
argument_list|,
literal|"version3"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt.new"
argument_list|,
literal|"version3"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file3.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/subdir/file4.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file3.txt.new"
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/subdir/file4.txt.new"
argument_list|,
literal|"version2"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/subdir/file5.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMoveOverExistingFileAndIgnore
specifier|public
name|void
name|testMoveOverExistingFileAndIgnore
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|dest
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v1"
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v2"
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file1.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/file2.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file3.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/subdir/file4.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|src
operator|.
name|resolve
argument_list|(
literal|"v3"
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file1.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/file2.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file3.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/subdir/file4.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"file3.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/subdir/file4.txt.new"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dest
argument_list|,
literal|"dir/subdir/file5.txt"
argument_list|,
literal|"version1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMoveFilesDoesNotCreateSameFileWithSuffix
specifier|public
name|void
name|testMoveFilesDoesNotCreateSameFileWithSuffix
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
index|[]
name|dirs
init|=
operator|new
name|Path
index|[]
block|{
name|createTempDir
argument_list|()
block|,
name|createTempDir
argument_list|()
block|,
name|createTempDir
argument_list|()
block|}
decl_stmt|;
for|for
control|(
name|Path
name|dir
range|:
name|dirs
control|)
block|{
name|Files
operator|.
name|write
argument_list|(
name|dir
operator|.
name|resolve
argument_list|(
literal|"file1.txt"
argument_list|)
argument_list|,
literal|"file1"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|dir
operator|.
name|resolve
argument_list|(
literal|"dir"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|write
argument_list|(
name|dir
operator|.
name|resolve
argument_list|(
literal|"dir"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"file2.txt"
argument_list|)
argument_list|,
literal|"file2"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|dirs
index|[
literal|0
index|]
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"file1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"file2"
argument_list|)
expr_stmt|;
comment|// do the same operation again, make sure, no .new files have been added
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|dirs
index|[
literal|1
index|]
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"file1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"file2"
argument_list|)
expr_stmt|;
name|assertFileNotExists
argument_list|(
name|dst
operator|.
name|resolve
argument_list|(
literal|"file1.txt.new"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFileNotExists
argument_list|(
name|dst
operator|.
name|resolve
argument_list|(
literal|"dir"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"file2.txt.new"
argument_list|)
argument_list|)
expr_stmt|;
comment|// change file content, make sure it gets updated
name|Files
operator|.
name|write
argument_list|(
name|dirs
index|[
literal|2
index|]
operator|.
name|resolve
argument_list|(
literal|"dir"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"file2.txt"
argument_list|)
argument_list|,
literal|"UPDATED"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|FileSystemUtils
operator|.
name|moveFilesWithoutOverwriting
argument_list|(
name|dirs
index|[
literal|2
index|]
argument_list|,
name|dst
argument_list|,
literal|".new"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"file1.txt"
argument_list|,
literal|"file1"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt"
argument_list|,
literal|"file2"
argument_list|)
expr_stmt|;
name|assertFileContent
argument_list|(
name|dst
argument_list|,
literal|"dir/file2.txt.new"
argument_list|,
literal|"UPDATED"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Check that a file contains a given String      * @param dir root dir for file      * @param filename relative path from root dir to file      * @param expected expected content (if null, we don't expect any file)      */
DECL|method|assertFileContent
specifier|public
specifier|static
name|void
name|assertFileContent
parameter_list|(
name|Path
name|dir
parameter_list|,
name|String
name|filename
parameter_list|,
name|String
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|Assert
operator|.
name|assertThat
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|file
init|=
name|dir
operator|.
name|resolve
argument_list|(
name|filename
argument_list|)
decl_stmt|;
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|assertThat
argument_list|(
literal|"file ["
operator|+
name|file
operator|+
literal|"] should not exist."
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertFileExists
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|String
name|fileContent
init|=
operator|new
name|String
argument_list|(
name|Files
operator|.
name|readAllBytes
argument_list|(
name|file
argument_list|)
argument_list|,
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
comment|// trim the string content to prevent different handling on windows vs. unix and CR chars...
name|Assert
operator|.
name|assertThat
argument_list|(
name|fileContent
operator|.
name|trim
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expected
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testAppend
specifier|public
name|void
name|testAppend
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|FileSystemUtils
operator|.
name|append
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/hello/world/this_is/awesome"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar/hello/world/this_is/awesome"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FileSystemUtils
operator|.
name|append
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/hello/world/this_is/awesome"
argument_list|)
argument_list|,
literal|2
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar/this_is/awesome"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|FileSystemUtils
operator|.
name|append
argument_list|(
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/hello/world/this_is/awesome"
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|,
name|PathUtils
operator|.
name|get
argument_list|(
literal|"/foo/bar/world/this_is/awesome"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIsHidden
specifier|public
name|void
name|testIsHidden
parameter_list|()
block|{
for|for
control|(
name|String
name|p
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|"/"
argument_list|,
literal|"foo"
argument_list|,
literal|"/foo"
argument_list|,
literal|"foo.bar"
argument_list|,
literal|"/foo.bar"
argument_list|,
literal|"foo/bar"
argument_list|,
literal|"foo/./bar"
argument_list|,
literal|"foo/../bar"
argument_list|,
literal|"/foo/./bar"
argument_list|,
literal|"/foo/../bar"
argument_list|)
control|)
block|{
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|FileSystemUtils
operator|.
name|isHidden
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|p
range|:
name|Arrays
operator|.
name|asList
argument_list|(
literal|".hidden"
argument_list|,
literal|".hidden.ext"
argument_list|,
literal|"/.hidden"
argument_list|,
literal|"/.hidden.ext"
argument_list|,
literal|"foo/.hidden"
argument_list|,
literal|"foo/.hidden.ext"
argument_list|,
literal|"/foo/.hidden"
argument_list|,
literal|"/foo/.hidden.ext"
argument_list|,
literal|"."
argument_list|,
literal|".."
argument_list|,
literal|"foo/."
argument_list|,
literal|"foo/.."
argument_list|)
control|)
block|{
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|FileSystemUtils
operator|.
name|isHidden
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

