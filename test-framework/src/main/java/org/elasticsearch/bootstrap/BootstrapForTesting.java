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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedRunner
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
name|TestSecurityManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SecureSM
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|Bootstrap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|ESPolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|Security
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
name|SuppressForbidden
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
name|PathUtils
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
name|PluginInfo
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketPermission
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
name|Permission
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
name|security
operator|.
name|ProtectionDomain
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
name|Arrays
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
name|HashMap
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
name|List
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
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
import|import static
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
operator|.
name|systemPropertyAsBoolean
import|;
end_import

begin_comment
comment|/**   * Initializes natives and installs test security manager  * (init'd early by base classes to ensure it happens regardless of which  * test case happens to be first, test ordering, etc).   *<p>  * The idea is to mimic as much as possible what happens with ES in production  * mode (e.g. assign permissions and install security manager the same way)  */
end_comment

begin_class
DECL|class|BootstrapForTesting
specifier|public
class|class
name|BootstrapForTesting
block|{
comment|// TODO: can we share more code with the non-test side here
comment|// without making things complex???
static|static
block|{
comment|// make sure java.io.tmpdir exists always (in case code uses it in a static initializer)
name|Path
name|javaTmpDir
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
argument_list|,
literal|"please set ${java.io.tmpdir} in pom.xml"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|Security
operator|.
name|ensureDirectoryExists
argument_list|(
name|javaTmpDir
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unable to create test temp directory"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// just like bootstrap, initialize natives, then SM
name|Bootstrap
operator|.
name|initializeNatives
argument_list|(
name|javaTmpDir
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// initialize probes
name|Bootstrap
operator|.
name|initializeProbes
argument_list|()
expr_stmt|;
comment|// check for jar hell
try|try
block|{
name|JarHell
operator|.
name|checkJarHell
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"found jar hell in test classpath"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// install security manager if requested
if|if
condition|(
name|systemPropertyAsBoolean
argument_list|(
literal|"tests.security.manager"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
try|try
block|{
comment|// initialize paths the same exact way as bootstrap
name|Permissions
name|perms
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
comment|// add permissions to everything in classpath
for|for
control|(
name|URL
name|url
range|:
name|JarHell
operator|.
name|parseClassPath
argument_list|()
control|)
block|{
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|url
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
comment|// resource itself
name|perms
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
literal|"read,readlink"
argument_list|)
argument_list|)
expr_stmt|;
comment|// classes underneath
name|perms
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
literal|"read,readlink"
argument_list|)
argument_list|)
expr_stmt|;
comment|// crazy jython...
name|String
name|filename
init|=
name|path
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|filename
operator|.
name|contains
argument_list|(
literal|"jython"
argument_list|)
operator|&&
name|filename
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
condition|)
block|{
comment|// just enough so it won't fail when it does not exist
name|perms
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|getParent
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink"
argument_list|)
argument_list|)
expr_stmt|;
name|perms
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|path
operator|.
name|getParent
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"Lib"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,readlink"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// java.io.tmpdir
name|Security
operator|.
name|addPath
argument_list|(
name|perms
argument_list|,
literal|"java.io.tmpdir"
argument_list|,
name|javaTmpDir
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
comment|// custom test config file
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.config"
argument_list|)
argument_list|)
condition|)
block|{
name|perms
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.config"
argument_list|)
argument_list|,
literal|"read,readlink"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// jacoco coverage output file
if|if
condition|(
name|Boolean
operator|.
name|getBoolean
argument_list|(
literal|"tests.coverage"
argument_list|)
condition|)
block|{
name|Path
name|coverageDir
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.coverage.dir"
argument_list|)
argument_list|)
decl_stmt|;
name|perms
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|coverageDir
operator|.
name|resolve
argument_list|(
literal|"jacoco.exec"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,write"
argument_list|)
argument_list|)
expr_stmt|;
comment|// in case we get fancy and use the -integration goals later:
name|perms
operator|.
name|add
argument_list|(
operator|new
name|FilePermission
argument_list|(
name|coverageDir
operator|.
name|resolve
argument_list|(
literal|"jacoco-it.exec"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"read,write"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// intellij hack: intellij test runner wants setIO and will
comment|// screw up all test logging without it!
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.maven"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|perms
operator|.
name|add
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"setIO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// add bind permissions for testing
comment|// ephemeral ports (note, on java 7 before update 51, this is a different permission)
comment|// this should really be the only one allowed for tests, otherwise they have race conditions
name|perms
operator|.
name|add
argument_list|(
operator|new
name|SocketPermission
argument_list|(
literal|"localhost:0"
argument_list|,
literal|"listen,resolve"
argument_list|)
argument_list|)
expr_stmt|;
comment|// ... but tests are messy. like file permissions, just let them live in a fantasy for now.
comment|// TODO: cut over all tests to bind to ephemeral ports
name|perms
operator|.
name|add
argument_list|(
operator|new
name|SocketPermission
argument_list|(
literal|"localhost:1024-"
argument_list|,
literal|"listen,resolve"
argument_list|)
argument_list|)
expr_stmt|;
comment|// read test-framework permissions
specifier|final
name|Policy
name|testFramework
init|=
name|Security
operator|.
name|readPolicy
argument_list|(
name|Bootstrap
operator|.
name|class
operator|.
name|getResource
argument_list|(
literal|"test-framework.policy"
argument_list|)
argument_list|,
name|JarHell
operator|.
name|parseClassPath
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Policy
name|esPolicy
init|=
operator|new
name|ESPolicy
argument_list|(
name|perms
argument_list|,
name|getPluginPermissions
argument_list|()
argument_list|)
decl_stmt|;
name|Policy
operator|.
name|setPolicy
argument_list|(
operator|new
name|Policy
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|implies
parameter_list|(
name|ProtectionDomain
name|domain
parameter_list|,
name|Permission
name|permission
parameter_list|)
block|{
comment|// implements union
return|return
name|esPolicy
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
operator|||
name|testFramework
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|SecureSM
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Security
operator|.
name|selfTest
argument_list|()
expr_stmt|;
comment|// guarantee plugin classes are initialized first, in case they have one-time hacks.
comment|// this just makes unit testing more realistic
for|for
control|(
name|URL
name|url
range|:
name|Collections
operator|.
name|list
argument_list|(
name|BootstrapForTesting
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResources
argument_list|(
name|PluginInfo
operator|.
name|ES_PLUGIN_PROPERTIES
argument_list|)
argument_list|)
control|)
block|{
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
try|try
init|(
name|InputStream
name|stream
init|=
name|url
operator|.
name|openStream
argument_list|()
init|)
block|{
name|properties
operator|.
name|load
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|properties
operator|.
name|getProperty
argument_list|(
literal|"jvm"
argument_list|)
argument_list|)
condition|)
block|{
name|String
name|clazz
init|=
name|properties
operator|.
name|getProperty
argument_list|(
literal|"classname"
argument_list|)
decl_stmt|;
if|if
condition|(
name|clazz
operator|!=
literal|null
condition|)
block|{
name|Class
operator|.
name|forName
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
block|}
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
name|RuntimeException
argument_list|(
literal|"unable to install test security manager"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**       * we dont know which codesources belong to which plugin, so just remove the permission from key codebases      * like core, test-framework, etc. this way tests fail if accesscontroller blocks are missing.      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"accesses fully qualified URLs to configure security"
argument_list|)
DECL|method|getPluginPermissions
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Policy
argument_list|>
name|getPluginPermissions
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|URL
argument_list|>
name|pluginPolicies
init|=
name|Collections
operator|.
name|list
argument_list|(
name|BootstrapForTesting
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResources
argument_list|(
name|PluginInfo
operator|.
name|ES_PLUGIN_POLICY
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|pluginPolicies
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
comment|// compute classpath minus obvious places, all other jars will get the permission.
name|Set
argument_list|<
name|URL
argument_list|>
name|codebases
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|parseClassPathWithSymlinks
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|URL
argument_list|>
name|excluded
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
comment|// es core
name|Bootstrap
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
comment|// es test framework
name|BootstrapForTesting
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
comment|// lucene test framework
name|LuceneTestCase
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
comment|// randomized runner
name|RandomizedRunner
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
comment|// junit library
name|Assert
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|codebases
operator|.
name|removeAll
argument_list|(
name|excluded
argument_list|)
expr_stmt|;
comment|// parse each policy file, with codebase substitution from the classpath
specifier|final
name|List
argument_list|<
name|Policy
argument_list|>
name|policies
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|URL
name|policyFile
range|:
name|pluginPolicies
control|)
block|{
name|policies
operator|.
name|add
argument_list|(
name|Security
operator|.
name|readPolicy
argument_list|(
name|policyFile
argument_list|,
name|codebases
operator|.
name|toArray
argument_list|(
operator|new
name|URL
index|[
name|codebases
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// consult each policy file for those codebases
name|Map
argument_list|<
name|String
argument_list|,
name|Policy
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|URL
name|url
range|:
name|codebases
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|url
operator|.
name|getFile
argument_list|()
argument_list|,
operator|new
name|Policy
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|implies
parameter_list|(
name|ProtectionDomain
name|domain
parameter_list|,
name|Permission
name|permission
parameter_list|)
block|{
comment|// implements union
for|for
control|(
name|Policy
name|p
range|:
name|policies
control|)
block|{
if|if
condition|(
name|p
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
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
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
return|;
block|}
comment|/**      * return parsed classpath, but with symlinks resolved to destination files for matching      * this is for matching the toRealPath() in the code where we have a proper plugin structure      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"does evil stuff with paths and urls because devs and jenkins do evil stuff with paths and urls"
argument_list|)
DECL|method|parseClassPathWithSymlinks
specifier|static
name|URL
index|[]
name|parseClassPathWithSymlinks
parameter_list|()
throws|throws
name|Exception
block|{
name|URL
name|raw
index|[]
init|=
name|JarHell
operator|.
name|parseClassPath
argument_list|()
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
name|raw
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|raw
index|[
name|i
index|]
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|raw
index|[
name|i
index|]
operator|.
name|toURI
argument_list|()
argument_list|)
operator|.
name|toRealPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
expr_stmt|;
block|}
return|return
name|raw
return|;
block|}
comment|// does nothing, just easy way to make sure the class is loaded.
DECL|method|ensureInitialized
specifier|public
specifier|static
name|void
name|ensureInitialized
parameter_list|()
block|{}
block|}
end_class

end_unit

