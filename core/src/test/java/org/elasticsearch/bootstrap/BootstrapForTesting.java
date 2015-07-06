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
name|common
operator|.
name|logging
operator|.
name|Loggers
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
name|Objects
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
comment|// just like bootstrap, initialize natives, then SM
name|Bootstrap
operator|.
name|initializeNatives
argument_list|(
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
if|if
condition|(
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"tests.maven"
argument_list|)
argument_list|)
condition|)
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
else|else
block|{
name|Loggers
operator|.
name|getLogger
argument_list|(
name|BootstrapForTesting
operator|.
name|class
argument_list|)
operator|.
name|warn
argument_list|(
literal|"Your ide or custom test runner has jar hell issues, "
operator|+
literal|"you might want to look into that"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|// install security manager if requested
if|if
condition|(
name|systemPropertyAsBoolean
argument_list|(
literal|"tests.security.manager"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
try|try
block|{
name|Security
operator|.
name|setCodebaseProperties
argument_list|()
expr_stmt|;
comment|// initialize paths the same exact way as bootstrap.
name|Permissions
name|perms
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|Path
name|basedir
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
literal|"project.basedir"
argument_list|)
argument_list|,
literal|"please set ${project.basedir} in pom.xml"
argument_list|)
argument_list|)
decl_stmt|;
comment|// target/classes, target/test-classes
name|Security
operator|.
name|addPath
argument_list|(
name|perms
argument_list|,
name|basedir
operator|.
name|resolve
argument_list|(
literal|"target"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"classes"
argument_list|)
argument_list|,
literal|"read,readlink"
argument_list|)
expr_stmt|;
name|Security
operator|.
name|addPath
argument_list|(
name|perms
argument_list|,
name|basedir
operator|.
name|resolve
argument_list|(
literal|"target"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"test-classes"
argument_list|)
argument_list|,
literal|"read,readlink"
argument_list|)
expr_stmt|;
comment|// .m2/repository
name|Path
name|m2repoDir
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
literal|"m2.repository"
argument_list|)
argument_list|,
literal|"please set ${m2.repository} in pom.xml"
argument_list|)
argument_list|)
decl_stmt|;
name|Security
operator|.
name|addPath
argument_list|(
name|perms
argument_list|,
name|m2repoDir
argument_list|,
literal|"read,readlink"
argument_list|)
expr_stmt|;
comment|// java.io.tmpdir
name|Security
operator|.
name|addPath
argument_list|(
name|perms
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
name|Policy
operator|.
name|setPolicy
argument_list|(
operator|new
name|ESPolicy
argument_list|(
name|perms
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|TestSecurityManager
argument_list|()
argument_list|)
expr_stmt|;
name|Security
operator|.
name|selfTest
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
literal|"unable to install test security manager"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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

