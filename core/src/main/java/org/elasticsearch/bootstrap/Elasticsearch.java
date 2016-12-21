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
name|joptsimple
operator|.
name|OptionSet
import|;
end_import

begin_import
import|import
name|joptsimple
operator|.
name|OptionSpec
import|;
end_import

begin_import
import|import
name|joptsimple
operator|.
name|OptionSpecBuilder
import|;
end_import

begin_import
import|import
name|joptsimple
operator|.
name|util
operator|.
name|PathConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|ExitCodes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|EnvironmentAwareCommand
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Terminal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|UserException
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
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeValidationException
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
name|Map
import|;
end_import

begin_comment
comment|/**  * This class starts elasticsearch.  */
end_comment

begin_class
DECL|class|Elasticsearch
class|class
name|Elasticsearch
extends|extends
name|EnvironmentAwareCommand
block|{
DECL|field|versionOption
specifier|private
specifier|final
name|OptionSpecBuilder
name|versionOption
decl_stmt|;
DECL|field|daemonizeOption
specifier|private
specifier|final
name|OptionSpecBuilder
name|daemonizeOption
decl_stmt|;
DECL|field|pidfileOption
specifier|private
specifier|final
name|OptionSpec
argument_list|<
name|Path
argument_list|>
name|pidfileOption
decl_stmt|;
DECL|field|quietOption
specifier|private
specifier|final
name|OptionSpecBuilder
name|quietOption
decl_stmt|;
comment|// visible for testing
DECL|method|Elasticsearch
name|Elasticsearch
parameter_list|()
block|{
name|super
argument_list|(
literal|"starts elasticsearch"
argument_list|)
expr_stmt|;
name|versionOption
operator|=
name|parser
operator|.
name|acceptsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"V"
argument_list|,
literal|"version"
argument_list|)
argument_list|,
literal|"Prints elasticsearch version information and exits"
argument_list|)
expr_stmt|;
name|daemonizeOption
operator|=
name|parser
operator|.
name|acceptsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"d"
argument_list|,
literal|"daemonize"
argument_list|)
argument_list|,
literal|"Starts Elasticsearch in the background"
argument_list|)
operator|.
name|availableUnless
argument_list|(
name|versionOption
argument_list|)
expr_stmt|;
name|pidfileOption
operator|=
name|parser
operator|.
name|acceptsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"p"
argument_list|,
literal|"pidfile"
argument_list|)
argument_list|,
literal|"Creates a pid file in the specified path on start"
argument_list|)
operator|.
name|availableUnless
argument_list|(
name|versionOption
argument_list|)
operator|.
name|withRequiredArg
argument_list|()
operator|.
name|withValuesConvertedBy
argument_list|(
operator|new
name|PathConverter
argument_list|()
argument_list|)
expr_stmt|;
name|quietOption
operator|=
name|parser
operator|.
name|acceptsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"q"
argument_list|,
literal|"quiet"
argument_list|)
argument_list|,
literal|"Turns off standard ouput/error streams logging in console"
argument_list|)
operator|.
name|availableUnless
argument_list|(
name|versionOption
argument_list|)
operator|.
name|availableUnless
argument_list|(
name|daemonizeOption
argument_list|)
expr_stmt|;
block|}
comment|/**      * Main entry point for starting elasticsearch      */
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
comment|// we want the JVM to think there is a security manager installed so that if internal policy decisions that would be based on the
comment|// presence of a security manager or lack thereof act as if there is a security manager present (e.g., DNS cache policy)
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|SecurityManager
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{
comment|// grant all permissions so that we can later set the security manager to the one that we want
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|Elasticsearch
name|elasticsearch
init|=
operator|new
name|Elasticsearch
argument_list|()
decl_stmt|;
name|int
name|status
init|=
name|main
argument_list|(
name|args
argument_list|,
name|elasticsearch
argument_list|,
name|Terminal
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|!=
name|ExitCodes
operator|.
name|OK
condition|)
block|{
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|main
specifier|static
name|int
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|,
specifier|final
name|Elasticsearch
name|elasticsearch
parameter_list|,
specifier|final
name|Terminal
name|terminal
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|elasticsearch
operator|.
name|main
argument_list|(
name|args
argument_list|,
name|terminal
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|protected
name|void
name|execute
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|OptionSet
name|options
parameter_list|,
name|Environment
name|env
parameter_list|)
throws|throws
name|UserException
block|{
if|if
condition|(
name|options
operator|.
name|nonOptionArguments
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UserException
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|"Positional arguments not allowed, found "
operator|+
name|options
operator|.
name|nonOptionArguments
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|options
operator|.
name|has
argument_list|(
name|versionOption
argument_list|)
condition|)
block|{
if|if
condition|(
name|options
operator|.
name|has
argument_list|(
name|daemonizeOption
argument_list|)
operator|||
name|options
operator|.
name|has
argument_list|(
name|pidfileOption
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UserException
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|"Elasticsearch version option is mutually exclusive with any other option"
argument_list|)
throw|;
block|}
name|terminal
operator|.
name|println
argument_list|(
literal|"Version: "
operator|+
name|org
operator|.
name|elasticsearch
operator|.
name|Version
operator|.
name|CURRENT
operator|+
literal|", Build: "
operator|+
name|Build
operator|.
name|CURRENT
operator|.
name|shortHash
argument_list|()
operator|+
literal|"/"
operator|+
name|Build
operator|.
name|CURRENT
operator|.
name|date
argument_list|()
operator|+
literal|", JVM: "
operator|+
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|boolean
name|daemonize
init|=
name|options
operator|.
name|has
argument_list|(
name|daemonizeOption
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|pidFile
init|=
name|pidfileOption
operator|.
name|value
argument_list|(
name|options
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|quiet
init|=
name|options
operator|.
name|has
argument_list|(
name|quietOption
argument_list|)
decl_stmt|;
try|try
block|{
name|init
argument_list|(
name|daemonize
argument_list|,
name|pidFile
argument_list|,
name|quiet
argument_list|,
name|env
operator|.
name|settings
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NodeValidationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UserException
argument_list|(
name|ExitCodes
operator|.
name|CONFIG
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|method|init
name|void
name|init
parameter_list|(
specifier|final
name|boolean
name|daemonize
parameter_list|,
specifier|final
name|Path
name|pidFile
parameter_list|,
specifier|final
name|boolean
name|quiet
parameter_list|,
name|Settings
name|initialSettings
parameter_list|)
throws|throws
name|NodeValidationException
throws|,
name|UserException
block|{
try|try
block|{
name|Bootstrap
operator|.
name|init
argument_list|(
operator|!
name|daemonize
argument_list|,
name|pidFile
argument_list|,
name|quiet
argument_list|,
name|initialSettings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BootstrapException
decl||
name|RuntimeException
name|e
parameter_list|)
block|{
comment|// format exceptions to the console in a special way
comment|// to avoid 2MB stacktraces from guice, etc.
throw|throw
operator|new
name|StartupException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Required method that's called by Apache Commons procrun when      * running as a service on Windows, when the service is stopped.      *      * http://commons.apache.org/proper/commons-daemon/procrun.html      *      * NOTE: If this method is renamed and/or moved, make sure to      * update elasticsearch-service.bat!      */
DECL|method|close
specifier|static
name|void
name|close
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|Bootstrap
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

