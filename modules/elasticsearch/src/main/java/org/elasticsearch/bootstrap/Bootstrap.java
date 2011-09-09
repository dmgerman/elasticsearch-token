begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ExceptionsHelper
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
name|common
operator|.
name|Classes
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
name|collect
operator|.
name|Tuple
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
name|inject
operator|.
name|spi
operator|.
name|Message
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
name|FileSystemUtils
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
name|jline
operator|.
name|ANSI
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
name|jna
operator|.
name|Natives
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
name|common
operator|.
name|logging
operator|.
name|log4j
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
name|os
operator|.
name|OsUtils
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
name|NodeBuilder
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
name|InternalSettingsPerparer
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
name|CountDownLatch
import|;
end_import

begin_import
import|import static
name|jline
operator|.
name|ANSIBuffer
operator|.
name|ANSICodes
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A main entry point when starting from the command line.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|Bootstrap
specifier|public
class|class
name|Bootstrap
block|{
DECL|field|node
specifier|private
name|Node
name|node
decl_stmt|;
DECL|field|keepAliveThread
specifier|private
specifier|static
specifier|volatile
name|Thread
name|keepAliveThread
decl_stmt|;
DECL|field|keepAliveLatch
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|keepAliveLatch
decl_stmt|;
DECL|method|setup
specifier|private
name|void
name|setup
parameter_list|(
name|boolean
name|addShutdownHook
parameter_list|,
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
parameter_list|)
throws|throws
name|Exception
block|{
comment|//        Loggers.getLogger(Bootstrap.class, tuple.v1().get("name")).info("heap_size {}/{}", JvmStats.jvmStats().mem().heapCommitted(), JvmInfo.jvmInfo().mem().heapMax());
if|if
condition|(
name|tuple
operator|.
name|v1
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"bootstrap.mlockall"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|Natives
operator|.
name|tryMlockall
argument_list|()
expr_stmt|;
block|}
name|tuple
operator|=
name|setupJmx
argument_list|(
name|tuple
argument_list|)
expr_stmt|;
name|NodeBuilder
name|nodeBuilder
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|tuple
operator|.
name|v1
argument_list|()
argument_list|)
operator|.
name|loadConfigSettings
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|node
operator|=
name|nodeBuilder
operator|.
name|build
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
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setupJmx
specifier|private
specifier|static
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|setupJmx
parameter_list|(
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
parameter_list|)
block|{
comment|// We disable JMX on by default, since we don't really want the overhead of RMI (and RMI GC...)
comment|//        if (tuple.v1().get(JmxService.SettingsConstants.CREATE_CONNECTOR) == null) {
comment|//            // automatically create the connector if we are bootstrapping
comment|//            Settings updated = settingsBuilder().put(tuple.v1()).put(JmxService.SettingsConstants.CREATE_CONNECTOR, true).build();
comment|//            tuple = new Tuple<Settings, Environment>(updated, tuple.v2());
comment|//        }
return|return
name|tuple
return|;
block|}
DECL|method|setupLogging
specifier|private
specifier|static
name|void
name|setupLogging
parameter_list|(
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
parameter_list|)
block|{
try|try
block|{
name|Classes
operator|.
name|getDefaultClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"org.apache.log4j.Logger"
argument_list|)
expr_stmt|;
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|tuple
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// no log4j
block|}
catch|catch
parameter_list|(
name|NoClassDefFoundError
name|e
parameter_list|)
block|{
comment|// no log4j
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Failed to configure logging..."
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|initialSettings
specifier|private
specifier|static
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|initialSettings
parameter_list|()
block|{
return|return
name|InternalSettingsPerparer
operator|.
name|prepareSettings
argument_list|(
name|EMPTY_SETTINGS
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * hook for JSVC      */
DECL|method|init
specifier|public
name|void
name|init
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
init|=
name|initialSettings
argument_list|()
decl_stmt|;
name|setupLogging
argument_list|(
name|tuple
argument_list|)
expr_stmt|;
name|setup
argument_list|(
literal|true
argument_list|,
name|tuple
argument_list|)
expr_stmt|;
block|}
comment|/**      * hook for JSVC      */
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
name|node
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**      * hook for JSVC      */
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|node
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/**      * hook for JSVC      */
DECL|method|destroy
specifier|public
name|void
name|destroy
parameter_list|()
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
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
name|Bootstrap
name|bootstrap
init|=
operator|new
name|Bootstrap
argument_list|()
decl_stmt|;
name|String
name|pidFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es-pidfile"
argument_list|)
decl_stmt|;
comment|// enable jline by default when running form "main" (and not on windows)
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"jline.enabled"
argument_list|)
operator|==
literal|null
operator|&&
operator|!
name|OsUtils
operator|.
name|WINDOWS
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"jline.enabled"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|boolean
name|foreground
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es-foreground"
argument_list|)
operator|!=
literal|null
decl_stmt|;
comment|// handle the wrapper system property, if its a service, don't run as a service
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"wrapper.service"
argument_list|,
literal|"XXX"
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|foreground
operator|=
literal|false
expr_stmt|;
block|}
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tuple
operator|=
name|initialSettings
argument_list|()
expr_stmt|;
name|setupLogging
argument_list|(
name|tuple
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|errorMessage
init|=
name|buildErrorMessage
argument_list|(
literal|"Setup"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|errorMessage
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.max-open-files"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|ESLogger
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
literal|"max_open_files [{}]"
argument_list|,
name|FileSystemUtils
operator|.
name|maxOpenFiles
argument_list|(
operator|new
name|File
argument_list|(
name|tuple
operator|.
name|v2
argument_list|()
operator|.
name|workFile
argument_list|()
argument_list|,
literal|"open_files"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// warn if running using the client VM
if|if
condition|(
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|vmName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
literal|"client"
argument_list|)
condition|)
block|{
name|ESLogger
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
name|warn
argument_list|(
literal|"jvm uses the client vm, make sure to run `java` with the server vm for best performance by adding `-server` to the command line"
argument_list|)
expr_stmt|;
block|}
name|String
name|stage
init|=
literal|"Initialization"
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|foreground
condition|)
block|{
name|Loggers
operator|.
name|disableConsoleLogging
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|bootstrap
operator|.
name|setup
argument_list|(
literal|true
argument_list|,
name|tuple
argument_list|)
expr_stmt|;
if|if
condition|(
name|pidFile
operator|!=
literal|null
condition|)
block|{
operator|new
name|File
argument_list|(
name|pidFile
argument_list|)
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
name|stage
operator|=
literal|"Startup"
expr_stmt|;
name|bootstrap
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|foreground
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|keepAliveLatch
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
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
literal|"es[keepAlive]"
argument_list|)
expr_stmt|;
name|keepAliveThread
operator|.
name|setDaemon
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|keepAliveThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|ESLogger
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
name|bootstrap
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
name|bootstrap
operator|.
name|node
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|errorMessage
init|=
name|buildErrorMessage
argument_list|(
name|stage
argument_list|,
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
name|foreground
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|errorMessage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|errorMessage
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
name|Loggers
operator|.
name|disableConsoleLogging
argument_list|()
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildErrorMessage
specifier|private
specifier|static
name|String
name|buildErrorMessage
parameter_list|(
name|String
name|stage
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|StringBuilder
name|errorMessage
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"{"
argument_list|)
operator|.
name|append
argument_list|(
name|Version
operator|.
name|full
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"}: "
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ANSI
operator|.
name|isEnabled
argument_list|()
condition|)
block|{
name|errorMessage
operator|.
name|append
argument_list|(
name|attrib
argument_list|(
name|ANSI
operator|.
name|Code
operator|.
name|FG_RED
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
name|stage
argument_list|)
operator|.
name|append
argument_list|(
literal|" Failed ..."
argument_list|)
operator|.
name|append
argument_list|(
name|attrib
argument_list|(
name|ANSI
operator|.
name|Code
operator|.
name|OFF
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|errorMessage
operator|.
name|append
argument_list|(
name|stage
argument_list|)
operator|.
name|append
argument_list|(
literal|" Failed ...\n"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|errorMessage
operator|.
name|append
argument_list|(
name|stage
argument_list|)
operator|.
name|append
argument_list|(
literal|" Failed ...\n"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|CreationException
condition|)
block|{
name|CreationException
name|createException
init|=
operator|(
name|CreationException
operator|)
name|e
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|seenMessages
init|=
name|newHashSet
argument_list|()
decl_stmt|;
name|int
name|counter
init|=
literal|1
decl_stmt|;
for|for
control|(
name|Message
name|message
range|:
name|createException
operator|.
name|getErrorMessages
argument_list|()
control|)
block|{
name|String
name|detailedMessage
decl_stmt|;
if|if
condition|(
name|message
operator|.
name|getCause
argument_list|()
operator|==
literal|null
condition|)
block|{
name|detailedMessage
operator|=
name|message
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|detailedMessage
operator|=
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|message
operator|.
name|getCause
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|detailedMessage
operator|==
literal|null
condition|)
block|{
name|detailedMessage
operator|=
name|message
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|seenMessages
operator|.
name|contains
argument_list|(
name|detailedMessage
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|seenMessages
operator|.
name|add
argument_list|(
name|detailedMessage
argument_list|)
expr_stmt|;
name|errorMessage
operator|.
name|append
argument_list|(
literal|""
argument_list|)
operator|.
name|append
argument_list|(
name|counter
operator|++
argument_list|)
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
operator|.
name|append
argument_list|(
name|detailedMessage
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|errorMessage
operator|.
name|append
argument_list|(
literal|"- "
argument_list|)
operator|.
name|append
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|errorMessage
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

