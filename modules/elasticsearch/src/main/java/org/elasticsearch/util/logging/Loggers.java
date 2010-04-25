begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.logging
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|logging
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
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
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
name|Classes
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
import|import static
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
name|Lists
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|asList
import|;
end_import

begin_comment
comment|/**  * A set of utilities around Logging.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|Loggers
specifier|public
class|class
name|Loggers
block|{
DECL|field|consoleLoggingEnabled
specifier|private
specifier|static
name|boolean
name|consoleLoggingEnabled
init|=
literal|true
decl_stmt|;
DECL|method|disableConsoleLogging
specifier|public
specifier|static
name|void
name|disableConsoleLogging
parameter_list|()
block|{
name|consoleLoggingEnabled
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|enableConsoleLogging
specifier|public
specifier|static
name|void
name|enableConsoleLogging
parameter_list|()
block|{
name|consoleLoggingEnabled
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|consoleLoggingEnabled
specifier|public
specifier|static
name|boolean
name|consoleLoggingEnabled
parameter_list|()
block|{
return|return
name|consoleLoggingEnabled
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
return|return
name|getLogger
argument_list|(
name|clazz
argument_list|,
name|settings
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|Lists
operator|.
name|asList
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|prefixes
argument_list|)
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|Index
name|index
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
return|return
name|getLogger
argument_list|(
name|clazz
argument_list|,
name|settings
argument_list|,
name|Lists
operator|.
name|asList
argument_list|(
name|index
operator|.
name|name
argument_list|()
argument_list|,
name|prefixes
argument_list|)
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|prefixesList
init|=
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"logger.logHostAddress"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
try|try
block|{
name|prefixesList
operator|.
name|add
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"logger.logHostName"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
try|try
block|{
name|prefixesList
operator|.
name|add
argument_list|(
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|String
name|name
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|prefixesList
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|prefixes
operator|!=
literal|null
operator|&&
name|prefixes
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|prefixesList
operator|.
name|addAll
argument_list|(
name|asList
argument_list|(
name|prefixes
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|getLogger
argument_list|(
name|getLoggerName
argument_list|(
name|clazz
argument_list|)
argument_list|,
name|prefixesList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|prefixesList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|ESLogger
name|parentLogger
parameter_list|,
name|String
name|s
parameter_list|)
block|{
return|return
name|getLogger
argument_list|(
name|parentLogger
operator|.
name|getName
argument_list|()
operator|+
name|s
argument_list|,
name|parentLogger
operator|.
name|getPrefix
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|s
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|Class
name|clazz
parameter_list|)
block|{
return|return
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|getLoggerName
argument_list|(
name|clazz
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
return|return
name|getLogger
argument_list|(
name|getLoggerName
argument_list|(
name|clazz
argument_list|)
argument_list|,
name|prefixes
argument_list|)
return|;
block|}
DECL|method|getLogger
specifier|public
specifier|static
name|ESLogger
name|getLogger
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|prefixes
parameter_list|)
block|{
name|String
name|prefix
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|prefixes
operator|!=
literal|null
operator|&&
name|prefixes
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|prefixX
range|:
name|prefixes
control|)
block|{
if|if
condition|(
name|prefixX
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|prefixX
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|prefix
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|prefix
argument_list|,
name|getLoggerName
argument_list|(
name|name
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getLoggerName
specifier|private
specifier|static
name|String
name|getLoggerName
parameter_list|(
name|Class
name|clazz
parameter_list|)
block|{
name|String
name|name
init|=
name|clazz
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"org.elasticsearch."
argument_list|)
condition|)
block|{
name|name
operator|=
name|Classes
operator|.
name|getPackageName
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
return|return
name|getLoggerName
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|getLoggerName
specifier|private
specifier|static
name|String
name|getLoggerName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"org.elasticsearch."
argument_list|)
condition|)
block|{
return|return
name|name
operator|.
name|substring
argument_list|(
literal|"org.elasticsearch."
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
return|return
name|name
return|;
block|}
block|}
end_class

end_unit

