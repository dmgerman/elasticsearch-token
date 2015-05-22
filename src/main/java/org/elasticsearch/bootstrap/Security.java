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
name|SuppressForbidden
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
name|java
operator|.
name|io
operator|.
name|*
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
name|AccessMode
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
name|FileAlreadyExistsException
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
name|NotDirectoryException
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

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Policy
import|;
end_import

begin_comment
comment|/**   * Initializes securitymanager with necessary permissions.  *<p>  * We use a template file (the one we test with), and add additional   * permissions based on the environment (data paths, etc)  */
end_comment

begin_class
DECL|class|Security
specifier|final
class|class
name|Security
block|{
comment|/**       * Initializes securitymanager for the environment      * Can only happen once!      */
DECL|method|configure
specifier|static
name|void
name|configure
parameter_list|(
name|Environment
name|environment
parameter_list|)
throws|throws
name|Exception
block|{
comment|// enable security policy: union of template and environment-based paths.
name|Policy
operator|.
name|setPolicy
argument_list|(
operator|new
name|ESPolicy
argument_list|(
name|createPermissions
argument_list|(
name|environment
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// enable security manager
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|SecurityManager
argument_list|()
argument_list|)
expr_stmt|;
comment|// do some basic tests
name|selfTest
argument_list|()
expr_stmt|;
block|}
comment|/** returns dynamic Permissions to configured paths */
DECL|method|createPermissions
specifier|static
name|Permissions
name|createPermissions
parameter_list|(
name|Environment
name|environment
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: improve test infra so we can reduce permissions where read/write
comment|// is not really needed...
name|Permissions
name|policy
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|tmpFile
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|homeFile
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|configFile
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|logsFile
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|environment
operator|.
name|dataFiles
argument_list|()
control|)
block|{
name|addPath
argument_list|(
name|policy
argument_list|,
name|path
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|path
range|:
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
control|)
block|{
name|addPath
argument_list|(
name|policy
argument_list|,
name|path
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|path
range|:
name|environment
operator|.
name|repoFiles
argument_list|()
control|)
block|{
name|addPath
argument_list|(
name|policy
argument_list|,
name|path
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|environment
operator|.
name|pidFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|pidFile
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
block|}
return|return
name|policy
return|;
block|}
comment|/** Add access to path (and all files underneath it */
DECL|method|addPath
specifier|static
name|void
name|addPath
parameter_list|(
name|Permissions
name|policy
parameter_list|,
name|Path
name|path
parameter_list|,
name|String
name|permissions
parameter_list|)
throws|throws
name|IOException
block|{
comment|// paths may not exist yet
name|ensureDirectoryExists
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// add each path twice: once for itself, again for files underneath it
name|policy
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|permissions
argument_list|)
argument_list|)
expr_stmt|;
name|policy
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|toString
argument_list|()
operator|+
name|path
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getSeparator
argument_list|()
operator|+
literal|"-"
argument_list|,
name|permissions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Ensures configured directory {@code path} exists.      * @throws IOException if {@code path} exists, but is not a directory, not accessible, or broken symbolic link.      */
DECL|method|ensureDirectoryExists
specifier|static
name|void
name|ensureDirectoryExists
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this isn't atomic, but neither is createDirectories.
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// verify access, following links (throws exception if something is wrong)
comment|// we only check READ as a sanity test
name|path
operator|.
name|getFileSystem
argument_list|()
operator|.
name|provider
argument_list|()
operator|.
name|checkAccess
argument_list|(
name|path
operator|.
name|toRealPath
argument_list|()
argument_list|,
name|AccessMode
operator|.
name|READ
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// doesn't exist, or not a directory
try|try
block|{
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|e
parameter_list|)
block|{
comment|// convert optional specific exception so the context is clear
name|IOException
name|e2
init|=
operator|new
name|NotDirectoryException
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|e2
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e2
throw|;
block|}
block|}
block|}
comment|/** Simple checks that everything is ok */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"accesses jvm default tempdir as a self-test"
argument_list|)
DECL|method|selfTest
specifier|static
name|void
name|selfTest
parameter_list|()
throws|throws
name|IOException
block|{
comment|// check we can manipulate temporary files
try|try
block|{
name|Path
name|p
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|delete
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ignored
parameter_list|)
block|{
comment|// potentially virus scanner
block|}
block|}
catch|catch
parameter_list|(
name|SecurityException
name|problem
parameter_list|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Security misconfiguration: cannot access java.io.tmpdir"
argument_list|,
name|problem
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

