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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
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
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
comment|// set properties for jar locations
name|setCodebaseProperties
argument_list|()
expr_stmt|;
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
block|{
comment|// we disable this completely, because its granted otherwise:
comment|// 'Note: The "exitVM.*" permission is automatically granted to
comment|// all code loaded from the application class path, thus enabling
comment|// applications to terminate themselves.'
annotation|@
name|Override
specifier|public
name|void
name|checkExit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"exit("
operator|+
name|status
operator|+
literal|") not allowed by system policy"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// do some basic tests
name|selfTest
argument_list|()
expr_stmt|;
block|}
comment|// mapping of jars to codebase properties
comment|// note that this is only read once, when policy is parsed.
DECL|field|SPECIAL_JARS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|SPECIAL_JARS
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*lucene-core-.*\\.jar$"
argument_list|)
argument_list|,
literal|"es.security.jar.lucene.core"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*jsr166e-.*\\.jar$"
argument_list|)
argument_list|,
literal|"es.security.jar.twitter.jsr166e"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*securemock-.*\\.jar$"
argument_list|)
argument_list|,
literal|"es.security.jar.elasticsearch.securemock"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*bcprov-.*\\.jar$"
argument_list|)
argument_list|,
literal|"es.security.jar.bouncycastle.bcprov"
argument_list|)
expr_stmt|;
name|SPECIAL_JARS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets properties (codebase URLs) for policy files.      * JAR locations are not fixed so we have to find the locations of      * the ones we want.      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"proper use of URL"
argument_list|)
DECL|method|setCodebaseProperties
specifier|static
name|void
name|setCodebaseProperties
parameter_list|()
block|{
name|ClassLoader
name|loader
init|=
name|Security
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|loader
operator|instanceof
name|URLClassLoader
condition|)
block|{
for|for
control|(
name|URL
name|url
range|:
operator|(
operator|(
name|URLClassLoader
operator|)
name|loader
operator|)
operator|.
name|getURLs
argument_list|()
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|e
range|:
name|SPECIAL_JARS
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
name|matcher
argument_list|(
name|url
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|String
name|prop
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|prop
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"property: "
operator|+
name|prop
operator|+
literal|" is unexpectedly set: "
operator|+
name|System
operator|.
name|getProperty
argument_list|(
name|prop
argument_list|)
argument_list|)
throw|;
block|}
name|System
operator|.
name|setProperty
argument_list|(
name|prop
argument_list|,
name|url
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|prop
range|:
name|SPECIAL_JARS
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|prop
argument_list|)
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|prop
argument_list|,
literal|"file:/dev/null"
argument_list|)
expr_stmt|;
comment|// no chance to be interpreted as "all"
block|}
block|}
block|}
else|else
block|{
comment|// we could try to parse the classpath or something, but screw it for now.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unsupported system classloader type: "
operator|+
name|loader
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
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
name|Permissions
name|policy
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
comment|// read-only dirs
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|binFile
argument_list|()
argument_list|,
literal|"read,readlink"
argument_list|)
expr_stmt|;
name|addPath
argument_list|(
name|policy
argument_list|,
name|environment
operator|.
name|libFile
argument_list|()
argument_list|,
literal|"read,readlink"
argument_list|)
expr_stmt|;
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

