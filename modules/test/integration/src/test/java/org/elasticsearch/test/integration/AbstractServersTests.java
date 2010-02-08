begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|server
operator|.
name|Server
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
name|util
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
name|slf4j
operator|.
name|Logger
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
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|server
operator|.
name|ServerBuilder
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
name|util
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

begin_class
DECL|class|AbstractServersTests
specifier|public
specifier|abstract
class|class
name|AbstractServersTests
block|{
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|servers
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Server
argument_list|>
name|servers
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|clients
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Client
argument_list|>
name|clients
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|startServer
specifier|public
name|Server
name|startServer
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|buildServer
argument_list|(
name|id
argument_list|)
operator|.
name|start
argument_list|()
return|;
block|}
DECL|method|startServer
specifier|public
name|Server
name|startServer
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|buildServer
argument_list|(
name|id
argument_list|,
name|settings
argument_list|)
operator|.
name|start
argument_list|()
return|;
block|}
DECL|method|buildServer
specifier|public
name|Server
name|buildServer
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|buildServer
argument_list|(
name|id
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
return|;
block|}
DECL|method|buildServer
specifier|public
name|Server
name|buildServer
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|settingsSource
init|=
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'/'
argument_list|)
operator|+
literal|".yml"
decl_stmt|;
name|Settings
name|finalSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
name|settingsSource
argument_list|)
operator|.
name|putAll
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Server
name|server
init|=
name|serverBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|finalSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|servers
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|clients
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|server
operator|.
name|client
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|server
return|;
block|}
DECL|method|closeServer
specifier|public
name|void
name|closeServer
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|Client
name|client
init|=
name|clients
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Server
name|server
init|=
name|servers
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|server
specifier|public
name|Server
name|server
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|servers
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|clients
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|closeAllServers
specifier|public
name|void
name|closeAllServers
parameter_list|()
block|{
for|for
control|(
name|Client
name|client
range|:
name|clients
operator|.
name|values
argument_list|()
control|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|clients
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Server
name|server
range|:
name|servers
operator|.
name|values
argument_list|()
control|)
block|{
name|server
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|servers
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

