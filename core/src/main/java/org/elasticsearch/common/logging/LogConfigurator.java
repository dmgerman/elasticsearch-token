begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.logging
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
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
name|Level
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
name|LogManager
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
name|LoggerContext
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
name|config
operator|.
name|AbstractConfiguration
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
name|config
operator|.
name|Configurator
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
name|config
operator|.
name|builder
operator|.
name|api
operator|.
name|ConfigurationBuilder
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
name|config
operator|.
name|builder
operator|.
name|api
operator|.
name|ConfigurationBuilderFactory
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
name|config
operator|.
name|builder
operator|.
name|impl
operator|.
name|BuiltConfiguration
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
name|config
operator|.
name|composite
operator|.
name|CompositeConfiguration
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
name|config
operator|.
name|properties
operator|.
name|PropertiesConfiguration
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
name|config
operator|.
name|properties
operator|.
name|PropertiesConfigurationFactory
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
name|status
operator|.
name|StatusConsoleListener
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
name|status
operator|.
name|StatusData
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
name|status
operator|.
name|StatusListener
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
name|status
operator|.
name|StatusLogger
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
name|UserException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
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
name|node
operator|.
name|Node
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
name|FileVisitOption
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
name|FileVisitResult
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
name|SimpleFileVisitor
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
name|attribute
operator|.
name|BasicFileAttributes
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
name|EnumSet
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_class
DECL|class|LogConfigurator
specifier|public
class|class
name|LogConfigurator
block|{
comment|/*      * We want to detect situations where we touch logging before the configuration is loaded. If we do this, Log4j will status log an error      * message at the error level. With this error listener, we can capture if this happens. More broadly, we can detect any error-level      * status log message which likely indicates that something is broken. The listener is installed immediately on startup, and then when      * we get around to configuring logging we check that no error-level log messages have been logged by the status logger. If they have we      * fail startup and any such messages can be seen on the console.      */
DECL|field|error
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|error
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|ERROR_LISTENER
specifier|private
specifier|static
specifier|final
name|StatusListener
name|ERROR_LISTENER
init|=
operator|new
name|StatusConsoleListener
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|log
parameter_list|(
name|StatusData
name|data
parameter_list|)
block|{
name|error
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|super
operator|.
name|log
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|/**      * Registers a listener for status logger errors. This listener should be registered as early as possible to ensure that no errors are      * logged by the status logger before logging is configured.      */
DECL|method|registerErrorListener
specifier|public
specifier|static
name|void
name|registerErrorListener
parameter_list|()
block|{
name|error
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|StatusLogger
operator|.
name|getLogger
argument_list|()
operator|.
name|registerListener
argument_list|(
name|ERROR_LISTENER
argument_list|)
expr_stmt|;
block|}
comment|/**      * Configure logging without reading a log4j2.properties file, effectively configuring the      * status logger and all loggers to the console.      *      * @param settings for configuring logger.level and individual loggers      */
DECL|method|configureWithoutConfig
specifier|public
specifier|static
name|void
name|configureWithoutConfig
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// we initialize the status logger immediately otherwise Log4j will complain when we try to get the context
name|configureStatusLogger
argument_list|()
expr_stmt|;
name|configureLoggerLevels
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
comment|/**      * Configure logging reading from any log4j2.properties found in the config directory and its      * subdirectories from the specified environment. Will also configure logging to point the logs      * directory from the specified environment.      *      * @param environment the environment for reading configs and the logs path      * @throws IOException   if there is an issue readings any log4j2.properties in the config      *                       directory      * @throws UserException if there are no log4j2.properties in the specified configs path      */
DECL|method|configure
specifier|public
specifier|static
name|void
name|configure
parameter_list|(
specifier|final
name|Environment
name|environment
parameter_list|)
throws|throws
name|IOException
throws|,
name|UserException
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|environment
argument_list|)
expr_stmt|;
try|try
block|{
comment|// we are about to configure logging, check that the status logger did not log any error-level messages
name|checkErrorListener
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// whether or not the error listener check failed we can remove the listener now
name|StatusLogger
operator|.
name|getLogger
argument_list|()
operator|.
name|removeListener
argument_list|(
name|ERROR_LISTENER
argument_list|)
expr_stmt|;
block|}
name|configure
argument_list|(
name|environment
operator|.
name|settings
argument_list|()
argument_list|,
name|environment
operator|.
name|configFile
argument_list|()
argument_list|,
name|environment
operator|.
name|logsFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|checkErrorListener
specifier|private
specifier|static
name|void
name|checkErrorListener
parameter_list|()
block|{
assert|assert
name|errorListenerIsRegistered
argument_list|()
operator|:
literal|"expected error listener to be registered"
assert|;
if|if
condition|(
name|error
operator|.
name|get
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"status logger logged an error before logging was configured"
argument_list|)
throw|;
block|}
block|}
DECL|method|errorListenerIsRegistered
specifier|private
specifier|static
name|boolean
name|errorListenerIsRegistered
parameter_list|()
block|{
return|return
name|StreamSupport
operator|.
name|stream
argument_list|(
name|StatusLogger
operator|.
name|getLogger
argument_list|()
operator|.
name|getListeners
argument_list|()
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|l
lambda|->
name|l
operator|==
name|ERROR_LISTENER
argument_list|)
return|;
block|}
DECL|method|configure
specifier|private
specifier|static
name|void
name|configure
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|,
specifier|final
name|Path
name|configsPath
parameter_list|,
specifier|final
name|Path
name|logsPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|UserException
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|configsPath
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|logsPath
argument_list|)
expr_stmt|;
name|setLogConfigurationSystemProperty
argument_list|(
name|logsPath
argument_list|,
name|settings
argument_list|)
expr_stmt|;
comment|// we initialize the status logger immediately otherwise Log4j will complain when we try to get the context
name|configureStatusLogger
argument_list|()
expr_stmt|;
specifier|final
name|LoggerContext
name|context
init|=
operator|(
name|LoggerContext
operator|)
name|LogManager
operator|.
name|getContext
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|AbstractConfiguration
argument_list|>
name|configurations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|PropertiesConfigurationFactory
name|factory
init|=
operator|new
name|PropertiesConfigurationFactory
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|FileVisitOption
argument_list|>
name|options
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|FileVisitOption
operator|.
name|FOLLOW_LINKS
argument_list|)
decl_stmt|;
name|Files
operator|.
name|walkFileTree
argument_list|(
name|configsPath
argument_list|,
name|options
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
specifier|final
name|Path
name|file
parameter_list|,
specifier|final
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"log4j2.properties"
argument_list|)
condition|)
block|{
name|configurations
operator|.
name|add
argument_list|(
operator|(
name|PropertiesConfiguration
operator|)
name|factory
operator|.
name|getConfiguration
argument_list|(
name|context
argument_list|,
name|file
operator|.
name|toString
argument_list|()
argument_list|,
name|file
operator|.
name|toUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|configurations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UserException
argument_list|(
name|ExitCodes
operator|.
name|CONFIG
argument_list|,
literal|"no log4j2.properties found; tried ["
operator|+
name|configsPath
operator|+
literal|"] and its subdirectories"
argument_list|)
throw|;
block|}
name|context
operator|.
name|start
argument_list|(
operator|new
name|CompositeConfiguration
argument_list|(
name|configurations
argument_list|)
argument_list|)
expr_stmt|;
name|configureLoggerLevels
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|configureStatusLogger
specifier|private
specifier|static
name|void
name|configureStatusLogger
parameter_list|()
block|{
specifier|final
name|ConfigurationBuilder
argument_list|<
name|BuiltConfiguration
argument_list|>
name|builder
init|=
name|ConfigurationBuilderFactory
operator|.
name|newConfigurationBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setStatusLevel
argument_list|(
name|Level
operator|.
name|ERROR
argument_list|)
expr_stmt|;
name|Configurator
operator|.
name|initialize
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Configures the logging levels for loggers configured in the specified settings.      *      * @param settings the settings from which logger levels will be extracted      */
DECL|method|configureLoggerLevels
specifier|private
specifier|static
name|void
name|configureLoggerLevels
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|ESLoggerFactory
operator|.
name|LOG_DEFAULT_LEVEL_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
specifier|final
name|Level
name|level
init|=
name|ESLoggerFactory
operator|.
name|LOG_DEFAULT_LEVEL_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Loggers
operator|.
name|setLevel
argument_list|(
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
argument_list|,
name|level
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|levels
init|=
name|settings
operator|.
name|filter
argument_list|(
name|ESLoggerFactory
operator|.
name|LOG_LEVEL_SETTING
operator|::
name|match
argument_list|)
operator|.
name|getAsMap
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|key
range|:
name|levels
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|// do not set a log level for a logger named level (from the default log setting)
if|if
condition|(
operator|!
name|key
operator|.
name|equals
argument_list|(
name|ESLoggerFactory
operator|.
name|LOG_DEFAULT_LEVEL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
specifier|final
name|Level
name|level
init|=
name|ESLoggerFactory
operator|.
name|LOG_LEVEL_SETTING
operator|.
name|getConcreteSetting
argument_list|(
name|key
argument_list|)
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Loggers
operator|.
name|setLevel
argument_list|(
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|key
operator|.
name|substring
argument_list|(
literal|"logger."
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|level
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Set system properties that can be used in configuration files to specify paths and file patterns for log files. We expose three      * properties here:      *<ul>      *<li>      * {@code es.logs.base_path} the base path containing the log files      *</li>      *<li>      * {@code es.logs.cluster_name} the cluster name, used as the prefix of log filenames in the default configuration      *</li>      *<li>      * {@code es.logs.node_name} the node name, can be used as part of log filenames (only exposed if {@link Node#NODE_NAME_SETTING} is      * explicitly set)      *</li>      *</ul>      *      * @param logsPath the path to the log files      * @param settings the settings to extract the cluster and node names      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"sets system property for logging configuration"
argument_list|)
DECL|method|setLogConfigurationSystemProperty
specifier|private
specifier|static
name|void
name|setLogConfigurationSystemProperty
parameter_list|(
specifier|final
name|Path
name|logsPath
parameter_list|,
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logs.base_path"
argument_list|,
name|logsPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logs.cluster_name"
argument_list|,
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logs.node_name"
argument_list|,
name|Node
operator|.
name|NODE_NAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

