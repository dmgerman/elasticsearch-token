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
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|appender
operator|.
name|ConsoleAppender
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
name|Constants
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
name|StringHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|PidFile
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
name|inject
operator|.
name|CreationException
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
name|ESLoggerFactory
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
name|LogConfigurator
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
name|transport
operator|.
name|BoundTransportAddress
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
name|monitor
operator|.
name|os
operator|.
name|OsProbe
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
name|process
operator|.
name|ProcessProbe
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
name|Node
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
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
operator|.
name|InternalSettingsPreparer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_comment
comment|/**  * Internal startup code.  */
end_comment

begin_class
DECL|class|Bootstrap
specifier|final
class|class
name|Bootstrap
block|{
DECL|field|INSTANCE
specifier|private
specifier|static
specifier|volatile
name|Bootstrap
name|INSTANCE
decl_stmt|;
DECL|field|node
specifier|private
specifier|volatile
name|Node
name|node
decl_stmt|;
DECL|field|keepAliveLatch
specifier|private
specifier|final
name|CountDownLatch
name|keepAliveLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|field|keepAliveThread
specifier|private
specifier|final
name|Thread
name|keepAliveThread
decl_stmt|;
comment|/** creates a new instance */
DECL|method|Bootstrap
name|Bootstrap
parameter_list|()
block|{
name|keepAliveThread
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|keepAliveLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// bail out
block|}
block|}
block|}
argument_list|,
literal|"elasticsearch[keepAlive/"
operator|+
name|Version
operator|.
name|CURRENT
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|keepAliveThread
operator|.
name|setDaemon
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// keep this thread alive (non daemon thread) until we shutdown
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|keepAliveLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/** initialize native resources */
DECL|method|initializeNatives
specifier|public
specifier|static
name|void
name|initializeNatives
parameter_list|(
name|Path
name|tmpFile
parameter_list|,
name|boolean
name|mlockAll
parameter_list|,
name|boolean
name|seccomp
parameter_list|,
name|boolean
name|ctrlHandler
parameter_list|)
block|{
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Bootstrap
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// check if the user is running as root, and bail
if|if
condition|(
name|Natives
operator|.
name|definitelyRunningAsRoot
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"can not run elasticsearch as root"
argument_list|)
throw|;
block|}
comment|// enable secure computing mode
if|if
condition|(
name|seccomp
condition|)
block|{
name|Natives
operator|.
name|trySeccomp
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
block|}
comment|// mlockall if requested
if|if
condition|(
name|mlockAll
condition|)
block|{
if|if
condition|(
name|Constants
operator|.
name|WINDOWS
condition|)
block|{
name|Natives
operator|.
name|tryVirtualLock
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Natives
operator|.
name|tryMlockall
argument_list|()
expr_stmt|;
block|}
block|}
comment|// listener for windows close event
if|if
condition|(
name|ctrlHandler
condition|)
block|{
name|Natives
operator|.
name|addConsoleCtrlHandler
argument_list|(
operator|new
name|ConsoleCtrlHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|handle
parameter_list|(
name|int
name|code
parameter_list|)
block|{
if|if
condition|(
name|CTRL_CLOSE_EVENT
operator|==
name|code
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"running graceful exit on windows"
argument_list|)
expr_stmt|;
try|try
block|{
name|Bootstrap
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to stop node"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|// force remainder of JNA to be loaded (if available).
try|try
block|{
name|JNAKernel32Library
operator|.
name|getInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ignored
parameter_list|)
block|{
comment|// we've already logged this.
block|}
name|Natives
operator|.
name|trySetMaxNumberOfThreads
argument_list|()
expr_stmt|;
name|Natives
operator|.
name|trySetMaxSizeVirtualMemory
argument_list|()
expr_stmt|;
comment|// init lucene random seed. it will use /dev/urandom where available:
name|StringHelper
operator|.
name|randomId
argument_list|()
expr_stmt|;
block|}
DECL|method|initializeProbes
specifier|static
name|void
name|initializeProbes
parameter_list|()
block|{
comment|// Force probes to be loaded
name|ProcessProbe
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|OsProbe
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
expr_stmt|;
block|}
DECL|method|setup
specifier|private
name|void
name|setup
parameter_list|(
name|boolean
name|addShutdownHook
parameter_list|,
name|Environment
name|environment
parameter_list|)
throws|throws
name|BootstrapException
block|{
name|Settings
name|settings
init|=
name|environment
operator|.
name|settings
argument_list|()
decl_stmt|;
name|initializeNatives
argument_list|(
name|environment
operator|.
name|tmpFile
argument_list|()
argument_list|,
name|BootstrapSettings
operator|.
name|MEMORY_LOCK_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|,
name|BootstrapSettings
operator|.
name|SECCOMP_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|,
name|BootstrapSettings
operator|.
name|CTRLHANDLER_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
comment|// initialize probes before the security manager is installed
name|initializeProbes
argument_list|()
expr_stmt|;
if|if
condition|(
name|addShutdownHook
condition|)
block|{
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to stop node"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// look for jar hell
name|JarHell
operator|.
name|checkJarHell
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BootstrapException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// install SM after natives, shutdown hooks, etc.
try|try
block|{
name|Security
operator|.
name|configure
argument_list|(
name|environment
argument_list|,
name|BootstrapSettings
operator|.
name|SECURITY_FILTER_BAD_DEFAULTS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BootstrapException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|node
operator|=
operator|new
name|Node
argument_list|(
name|environment
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|validateNodeBeforeAcceptingRequests
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|,
specifier|final
name|BoundTransportAddress
name|boundTransportAddress
parameter_list|)
throws|throws
name|NodeValidationException
block|{
name|BootstrapCheck
operator|.
name|check
argument_list|(
name|settings
argument_list|,
name|boundTransportAddress
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
DECL|method|initialEnvironment
specifier|private
specifier|static
name|Environment
name|initialEnvironment
parameter_list|(
name|boolean
name|foreground
parameter_list|,
name|Path
name|pidFile
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|esSettings
parameter_list|)
block|{
name|Terminal
name|terminal
init|=
name|foreground
condition|?
name|Terminal
operator|.
name|DEFAULT
else|:
literal|null
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|pidFile
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PIDFILE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|pidFile
argument_list|)
expr_stmt|;
block|}
return|return
name|InternalSettingsPreparer
operator|.
name|prepareEnvironment
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|terminal
argument_list|,
name|esSettings
argument_list|)
return|;
block|}
DECL|method|start
specifier|private
name|void
name|start
parameter_list|()
throws|throws
name|NodeValidationException
block|{
name|node
operator|.
name|start
argument_list|()
expr_stmt|;
name|keepAliveThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|stop
specifier|static
name|void
name|stop
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|INSTANCE
operator|.
name|node
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|INSTANCE
operator|.
name|keepAliveLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** Set the system property before anything has a chance to trigger its use */
comment|// TODO: why? is it just a bad default somewhere? or is it some BS around 'but the client' garbage<-- my guess
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"sets logger prefix on initialization"
argument_list|)
DECL|method|initLoggerPrefix
specifier|static
name|void
name|initLoggerPrefix
parameter_list|()
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logger.prefix"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
comment|/**      * This method is invoked by {@link Elasticsearch#main(String[])} to startup elasticsearch.      */
DECL|method|init
specifier|static
name|void
name|init
parameter_list|(
specifier|final
name|boolean
name|foreground
parameter_list|,
specifier|final
name|Path
name|pidFile
parameter_list|,
specifier|final
name|boolean
name|quiet
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|esSettings
parameter_list|)
throws|throws
name|BootstrapException
throws|,
name|NodeValidationException
throws|,
name|UserException
block|{
comment|// Set the system property before anything has a chance to trigger its use
name|initLoggerPrefix
argument_list|()
expr_stmt|;
comment|// force the class initializer for BootstrapInfo to run before
comment|// the security manager is installed
name|BootstrapInfo
operator|.
name|init
argument_list|()
expr_stmt|;
name|INSTANCE
operator|=
operator|new
name|Bootstrap
argument_list|()
expr_stmt|;
name|Environment
name|environment
init|=
name|initialEnvironment
argument_list|(
name|foreground
argument_list|,
name|pidFile
argument_list|,
name|esSettings
argument_list|)
decl_stmt|;
try|try
block|{
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|environment
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BootstrapException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|checkForCustomConfFile
argument_list|()
expr_stmt|;
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
try|try
block|{
name|PidFile
operator|.
name|create
argument_list|(
name|environment
operator|.
name|pidFile
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BootstrapException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|final
name|boolean
name|closeStandardStreams
init|=
operator|(
name|foreground
operator|==
literal|false
operator|)
operator|||
name|quiet
decl_stmt|;
try|try
block|{
if|if
condition|(
name|closeStandardStreams
condition|)
block|{
specifier|final
name|Logger
name|rootLogger
init|=
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
decl_stmt|;
specifier|final
name|Appender
name|maybeConsoleAppender
init|=
name|Loggers
operator|.
name|findAppender
argument_list|(
name|rootLogger
argument_list|,
name|ConsoleAppender
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|maybeConsoleAppender
operator|!=
literal|null
condition|)
block|{
name|Loggers
operator|.
name|removeAppender
argument_list|(
name|rootLogger
argument_list|,
name|maybeConsoleAppender
argument_list|)
expr_stmt|;
block|}
name|closeSystOut
argument_list|()
expr_stmt|;
block|}
comment|// fail if using broken version
name|JVMCheck
operator|.
name|check
argument_list|()
expr_stmt|;
comment|// fail if somebody replaced the lucene jars
name|checkLucene
argument_list|()
expr_stmt|;
comment|// install the default uncaught exception handler; must be done before security is
comment|// initialized as we do not want to grant the runtime permission
comment|// setDefaultUncaughtExceptionHandler
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
operator|new
name|ElasticsearchUncaughtExceptionHandler
argument_list|(
parameter_list|()
lambda|->
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|environment
operator|.
name|settings
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|INSTANCE
operator|.
name|setup
argument_list|(
literal|true
argument_list|,
name|environment
argument_list|)
expr_stmt|;
name|INSTANCE
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|closeStandardStreams
condition|)
block|{
name|closeSysError
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NodeValidationException
decl||
name|RuntimeException
name|e
parameter_list|)
block|{
comment|// disable console logging, so user does not see the exception twice (jvm will show it already)
specifier|final
name|Logger
name|rootLogger
init|=
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
decl_stmt|;
specifier|final
name|Appender
name|maybeConsoleAppender
init|=
name|Loggers
operator|.
name|findAppender
argument_list|(
name|rootLogger
argument_list|,
name|ConsoleAppender
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|foreground
operator|&&
name|maybeConsoleAppender
operator|!=
literal|null
condition|)
block|{
name|Loggers
operator|.
name|removeAppender
argument_list|(
name|rootLogger
argument_list|,
name|maybeConsoleAppender
argument_list|)
expr_stmt|;
block|}
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Bootstrap
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|INSTANCE
operator|.
name|node
operator|!=
literal|null
condition|)
block|{
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Bootstrap
operator|.
name|class
argument_list|,
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|INSTANCE
operator|.
name|node
operator|.
name|settings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// HACK, it sucks to do this, but we will run users out of disk space otherwise
if|if
condition|(
name|e
operator|instanceof
name|CreationException
condition|)
block|{
comment|// guice: log the shortened exc to the log file
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|ps
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ps
operator|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|,
literal|false
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|uee
parameter_list|)
block|{
assert|assert
literal|false
assert|;
name|e
operator|.
name|addSuppressed
argument_list|(
name|uee
argument_list|)
expr_stmt|;
block|}
operator|new
name|StartupException
argument_list|(
name|e
argument_list|)
operator|.
name|printStackTrace
argument_list|(
name|ps
argument_list|)
expr_stmt|;
name|ps
operator|.
name|flush
argument_list|()
expr_stmt|;
try|try
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Guice Exception: {}"
argument_list|,
name|os
operator|.
name|toString
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|uee
parameter_list|)
block|{
assert|assert
literal|false
assert|;
name|e
operator|.
name|addSuppressed
argument_list|(
name|uee
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|NodeValidationException
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"node validation exception\n{}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// full exception
name|logger
operator|.
name|error
argument_list|(
literal|"Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// re-enable it if appropriate, so they can see any logging during the shutdown process
if|if
condition|(
name|foreground
operator|&&
name|maybeConsoleAppender
operator|!=
literal|null
condition|)
block|{
name|Loggers
operator|.
name|addAppender
argument_list|(
name|rootLogger
argument_list|,
name|maybeConsoleAppender
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"System#out"
argument_list|)
DECL|method|closeSystOut
specifier|private
specifier|static
name|void
name|closeSystOut
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"System#err"
argument_list|)
DECL|method|closeSysError
specifier|private
specifier|static
name|void
name|closeSysError
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|checkForCustomConfFile
specifier|private
specifier|static
name|void
name|checkForCustomConfFile
parameter_list|()
block|{
name|String
name|confFileSetting
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.default.config"
argument_list|)
decl_stmt|;
name|checkUnsetAndMaybeExit
argument_list|(
name|confFileSetting
argument_list|,
literal|"es.default.config"
argument_list|)
expr_stmt|;
name|confFileSetting
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.config"
argument_list|)
expr_stmt|;
name|checkUnsetAndMaybeExit
argument_list|(
name|confFileSetting
argument_list|,
literal|"es.config"
argument_list|)
expr_stmt|;
name|confFileSetting
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"elasticsearch.config"
argument_list|)
expr_stmt|;
name|checkUnsetAndMaybeExit
argument_list|(
name|confFileSetting
argument_list|,
literal|"elasticsearch.config"
argument_list|)
expr_stmt|;
block|}
DECL|method|checkUnsetAndMaybeExit
specifier|private
specifier|static
name|void
name|checkUnsetAndMaybeExit
parameter_list|(
name|String
name|confFileSetting
parameter_list|,
name|String
name|settingName
parameter_list|)
block|{
if|if
condition|(
name|confFileSetting
operator|!=
literal|null
operator|&&
name|confFileSetting
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Bootstrap
operator|.
name|class
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{} is no longer supported. elasticsearch.yml must be placed in the config directory and cannot be renamed."
argument_list|,
name|settingName
argument_list|)
expr_stmt|;
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"Allowed to exit explicitly in bootstrap phase"
argument_list|)
DECL|method|exit
specifier|private
specifier|static
name|void
name|exit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
DECL|method|checkLucene
specifier|private
specifier|static
name|void
name|checkLucene
parameter_list|()
block|{
if|if
condition|(
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|equals
argument_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
operator|.
name|LATEST
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Lucene version mismatch this version of Elasticsearch requires lucene version ["
operator|+
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|+
literal|"]  but the current lucene version is ["
operator|+
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
operator|.
name|LATEST
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

