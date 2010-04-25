begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.logging.log4j
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|logging
operator|.
name|log4j
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|PropertyConfigurator
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
name|env
operator|.
name|FailedToResolveConfigException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|MapBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|Properties
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|LogConfigurator
specifier|public
class|class
name|LogConfigurator
block|{
DECL|field|loaded
specifier|private
specifier|static
name|boolean
name|loaded
decl_stmt|;
DECL|field|replacements
specifier|private
specifier|static
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|replacements
init|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
operator|.
name|put
argument_list|(
literal|"console"
argument_list|,
literal|"org.elasticsearch.util.logging.log4j.ConsoleAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"async"
argument_list|,
literal|"org.apache.log4j.AsyncAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"dailyRollingFile"
argument_list|,
literal|"org.apache.log4j.DailyRollingFileAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"externallyRolledFile"
argument_list|,
literal|"org.apache.log4j.ExternallyRolledFileAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"file"
argument_list|,
literal|"org.apache.log4j.FileAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"jdbc"
argument_list|,
literal|"org.apache.log4j.JDBCAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"jms"
argument_list|,
literal|"org.apache.log4j.JMSAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"lf5"
argument_list|,
literal|"org.apache.log4j.LF5Appender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"ntevent"
argument_list|,
literal|"org.apache.log4j.NTEventLogAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|"org.apache.log4j.NullAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"rollingFile"
argument_list|,
literal|"org.apache.log4j.RollingFileAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"smtp"
argument_list|,
literal|"org.apache.log4j.SMTPAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"socket"
argument_list|,
literal|"org.apache.log4j.SocketAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"socketHub"
argument_list|,
literal|"org.apache.log4j.SocketHubAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"syslog"
argument_list|,
literal|"org.apache.log4j.SyslogAppender"
argument_list|)
operator|.
name|put
argument_list|(
literal|"telnet"
argument_list|,
literal|"org.apache.log4j.TelnetAppender"
argument_list|)
comment|// layouts
operator|.
name|put
argument_list|(
literal|"simple"
argument_list|,
literal|"org.apache.log4j.SimpleLayout"
argument_list|)
operator|.
name|put
argument_list|(
literal|"html"
argument_list|,
literal|"org.apache.log4j.HTMLLayout"
argument_list|)
operator|.
name|put
argument_list|(
literal|"pattern"
argument_list|,
literal|"org.apache.log4j.PatternLayout"
argument_list|)
operator|.
name|put
argument_list|(
literal|"consolePattern"
argument_list|,
literal|"org.elasticsearch.util.logging.log4j.JLinePatternLayout"
argument_list|)
operator|.
name|put
argument_list|(
literal|"ttcc"
argument_list|,
literal|"org.apache.log4j.TTCCLayout"
argument_list|)
operator|.
name|put
argument_list|(
literal|"xml"
argument_list|,
literal|"org.apache.log4j.XMLLayout"
argument_list|)
operator|.
name|immutableMap
argument_list|()
decl_stmt|;
DECL|method|configure
specifier|public
specifier|static
name|void
name|configure
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|loaded
condition|)
block|{
return|return;
block|}
name|loaded
operator|=
literal|true
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
name|ImmutableSettings
operator|.
name|Builder
name|settingsBuilder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
decl_stmt|;
try|try
block|{
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"logging.yml"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
catch|catch
parameter_list|(
name|NoClassDefFoundError
name|e
parameter_list|)
block|{
comment|// ignore, no yaml
block|}
try|try
block|{
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"logging.json"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
try|try
block|{
name|settingsBuilder
operator|.
name|loadFromUrl
argument_list|(
name|environment
operator|.
name|resolveConfig
argument_list|(
literal|"logging.properties"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedToResolveConfigException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|settingsBuilder
operator|.
name|putProperties
argument_list|(
literal|"elasticsearch."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|putProperties
argument_list|(
literal|"es."
argument_list|,
name|System
operator|.
name|getProperties
argument_list|()
argument_list|)
operator|.
name|replacePropertyPlaceholders
argument_list|()
expr_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|settingsBuilder
operator|.
name|build
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
literal|"log4j."
operator|+
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|replacements
operator|.
name|containsKey
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|value
operator|=
name|replacements
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|key
operator|.
name|endsWith
argument_list|(
literal|".value"
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|key
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|()
operator|-
literal|".value"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|endsWith
argument_list|(
literal|".type"
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|key
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|()
operator|-
literal|".type"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
name|PropertyConfigurator
operator|.
name|configure
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

