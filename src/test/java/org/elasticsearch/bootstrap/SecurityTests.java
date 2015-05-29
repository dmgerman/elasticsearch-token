begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

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
name|env
operator|.
name|Environment
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
name|java
operator|.
name|io
operator|.
name|FilePermission
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
name|security
operator|.
name|Permissions
import|;
end_import

begin_class
DECL|class|SecurityTests
specifier|public
class|class
name|SecurityTests
extends|extends
name|ElasticsearchTestCase
block|{
comment|/** test generated permissions */
DECL|method|testGeneratedPermissions
specifier|public
name|void
name|testGeneratedPermissions
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|path
init|=
name|createTempDir
argument_list|()
decl_stmt|;
comment|// make a fake ES home and ensure we only grant permissions to that.
name|Path
name|esHome
init|=
name|path
operator|.
name|resolve
argument_list|(
literal|"esHome"
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|settingsBuilder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|esHome
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|Path
name|fakeTmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|String
name|realTmpDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|Permissions
name|permissions
decl_stmt|;
try|try
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
name|fakeTmpDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|permissions
operator|=
name|Security
operator|.
name|createPermissions
argument_list|(
name|environment
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
name|realTmpDir
argument_list|)
expr_stmt|;
block|}
comment|// the fake es home
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|esHome
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// its parent
name|assertFalse
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// some other sibling
name|assertFalse
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"other"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// double check we overwrote java.io.tmpdir correctly for the test
name|assertFalse
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|realTmpDir
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** test generated permissions for all configured paths */
DECL|method|testEnvironmentPaths
specifier|public
name|void
name|testEnvironmentPaths
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|path
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Settings
operator|.
name|Builder
name|settingsBuilder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"home"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"conf"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.plugins"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"plugins"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|putArray
argument_list|(
literal|"path.data"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"data1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"data2"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"path.logs"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"logs"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
literal|"pidfile"
argument_list|,
name|path
operator|.
name|resolve
argument_list|(
literal|"test.pid"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|Path
name|fakeTmpDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|String
name|realTmpDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|Permissions
name|permissions
decl_stmt|;
name|Environment
name|environment
decl_stmt|;
try|try
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
name|fakeTmpDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|environment
operator|=
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|permissions
operator|=
name|Security
operator|.
name|createPermissions
argument_list|(
name|environment
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
name|realTmpDir
argument_list|)
expr_stmt|;
block|}
comment|// check that all directories got permissions:
comment|// homefile: this is needed unless we break out rules for "lib" dir.
comment|// TODO: make read-only
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|environment
operator|.
name|homeFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// config file
comment|// TODO: make read-only
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// plugins: r/w, TODO: can this be minimized?
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// data paths: r/w
for|for
control|(
name|Path
name|dataPath
range|:
name|environment
operator|.
name|dataFiles
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|dataPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|dataPath
range|:
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|dataPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// logs: r/w
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|environment
operator|.
name|logsFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// temp dir: r/w
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|fakeTmpDir
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// double check we overwrote java.io.tmpdir correctly for the test
name|assertFalse
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|realTmpDir
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// PID file: r/w
name|assertTrue
argument_list|(
name|permissions
operator|.
name|implies
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|environment
operator|.
name|pidFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnsureExists
specifier|public
name|void
name|testEnsureExists
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|createTempDir
argument_list|()
decl_stmt|;
comment|// directory exists
name|Path
name|exists
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"exists"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|exists
argument_list|)
expr_stmt|;
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|exists
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createTempFile
argument_list|(
name|exists
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnsureNotExists
specifier|public
name|void
name|testEnsureNotExists
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|createTempDir
argument_list|()
decl_stmt|;
comment|// directory does not exist: create it
name|Path
name|notExists
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"notexists"
argument_list|)
decl_stmt|;
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|notExists
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createTempFile
argument_list|(
name|notExists
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnsureRegularFile
specifier|public
name|void
name|testEnsureRegularFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|createTempDir
argument_list|()
decl_stmt|;
comment|// regular file
name|Path
name|regularFile
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"regular"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createFile
argument_list|(
name|regularFile
argument_list|)
expr_stmt|;
try|try
block|{
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|regularFile
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"didn't get expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|expected
parameter_list|)
block|{}
block|}
DECL|method|testEnsureSymlink
specifier|public
name|void
name|testEnsureSymlink
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|exists
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"exists"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|exists
argument_list|)
expr_stmt|;
comment|// symlink
name|Path
name|linkExists
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"linkExists"
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|createSymbolicLink
argument_list|(
name|linkExists
argument_list|,
name|exists
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
decl||
name|IOException
name|e
parameter_list|)
block|{
name|assumeNoException
argument_list|(
literal|"test requires filesystem that supports symbolic links"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assumeNoException
argument_list|(
literal|"test cannot create symbolic links with security manager enabled"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|linkExists
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createTempFile
argument_list|(
name|linkExists
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testEnsureBrokenSymlink
specifier|public
name|void
name|testEnsureBrokenSymlink
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
name|createTempDir
argument_list|()
decl_stmt|;
comment|// broken symlink
name|Path
name|brokenLink
init|=
name|p
operator|.
name|resolve
argument_list|(
literal|"brokenLink"
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|createSymbolicLink
argument_list|(
name|brokenLink
argument_list|,
name|p
operator|.
name|resolve
argument_list|(
literal|"nonexistent"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
decl||
name|IOException
name|e
parameter_list|)
block|{
name|assumeNoException
argument_list|(
literal|"test requires filesystem that supports symbolic links"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assumeNoException
argument_list|(
literal|"test cannot create symbolic links with security manager enabled"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|brokenLink
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"didn't get expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|expected
parameter_list|)
block|{}
block|}
block|}
end_class

end_unit
