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
name|Set
import|;
end_import

begin_class
DECL|class|LogConfigurator
specifier|public
class|class
name|LogConfigurator
block|{
static|static
block|{
comment|// we initialize the status logger immediately otherwise Log4j will complain when we try to get the context
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
comment|/**      * for triggering class initialization      */
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
block|{     }
DECL|method|configure
specifier|public
specifier|static
name|void
name|configure
parameter_list|(
specifier|final
name|Environment
name|environment
parameter_list|,
specifier|final
name|boolean
name|resolveConfig
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Settings
name|settings
init|=
name|environment
operator|.
name|settings
argument_list|()
decl_stmt|;
name|setLogConfigurationSystemProperty
argument_list|(
name|environment
argument_list|,
name|settings
argument_list|)
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
if|if
condition|(
name|resolveConfig
condition|)
block|{
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
name|Files
operator|.
name|walkFileTree
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
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
name|Path
name|file
parameter_list|,
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
block|}
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
name|Loggers
operator|.
name|setLevel
argument_list|(
name|ESLoggerFactory
operator|.
name|getRootLogger
argument_list|()
argument_list|,
name|ESLoggerFactory
operator|.
name|LOG_DEFAULT_LEVEL_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
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
name|String
name|key
range|:
name|levels
operator|.
name|keySet
argument_list|()
control|)
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
name|Loggers
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
name|Environment
name|environment
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logs"
argument_list|,
name|environment
operator|.
name|logsFile
argument_list|()
operator|.
name|resolve
argument_list|(
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
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

