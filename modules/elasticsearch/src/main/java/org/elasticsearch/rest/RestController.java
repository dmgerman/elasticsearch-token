begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|component
operator|.
name|AbstractComponent
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
name|component
operator|.
name|Lifecycle
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
name|component
operator|.
name|LifecycleComponent
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
name|path
operator|.
name|PathTrie
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
operator|.
name|Status
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|RestController
specifier|public
class|class
name|RestController
extends|extends
name|AbstractComponent
implements|implements
name|LifecycleComponent
argument_list|<
name|RestController
argument_list|>
block|{
DECL|field|lifecycle
specifier|private
specifier|final
name|Lifecycle
name|lifecycle
init|=
operator|new
name|Lifecycle
argument_list|()
decl_stmt|;
DECL|field|getHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|getHandlers
init|=
operator|new
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|postHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|postHandlers
init|=
operator|new
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|putHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|putHandlers
init|=
operator|new
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|deleteHandlers
specifier|private
specifier|final
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
name|deleteHandlers
init|=
operator|new
name|PathTrie
argument_list|<
name|RestHandler
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|RestController
annotation|@
name|Inject
specifier|public
name|RestController
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|lifecycleState
annotation|@
name|Override
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
name|this
operator|.
name|lifecycle
operator|.
name|state
argument_list|()
return|;
block|}
DECL|method|start
annotation|@
name|Override
specifier|public
name|RestController
name|start
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStarted
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
name|this
return|;
block|}
DECL|method|stop
annotation|@
name|Override
specifier|public
name|RestController
name|stop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStopped
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
return|return
name|this
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToClosed
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
DECL|method|registerHandler
specifier|public
name|void
name|registerHandler
parameter_list|(
name|RestRequest
operator|.
name|Method
name|method
parameter_list|,
name|String
name|path
parameter_list|,
name|RestHandler
name|handler
parameter_list|)
block|{
switch|switch
condition|(
name|method
condition|)
block|{
case|case
name|GET
case|:
name|getHandlers
operator|.
name|insert
argument_list|(
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE
case|:
name|deleteHandlers
operator|.
name|insert
argument_list|(
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
break|break;
case|case
name|POST
case|:
name|postHandlers
operator|.
name|insert
argument_list|(
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
break|break;
case|case
name|PUT
case|:
name|putHandlers
operator|.
name|insert
argument_list|(
name|path
argument_list|,
name|handler
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Can't handle ["
operator|+
name|method
operator|+
literal|"] for path ["
operator|+
name|path
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|dispatchRequest
specifier|public
name|void
name|dispatchRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
block|{
specifier|final
name|RestHandler
name|handler
init|=
name|getHandler
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|==
literal|null
condition|)
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringRestResponse
argument_list|(
name|BAD_REQUEST
argument_list|,
literal|"No handler found for uri ["
operator|+
name|request
operator|.
name|uri
argument_list|()
operator|+
literal|"] and method ["
operator|+
name|request
operator|.
name|method
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|handler
operator|.
name|handleRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|JsonThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response for uri ["
operator|+
name|request
operator|.
name|uri
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|getHandler
specifier|private
name|RestHandler
name|getHandler
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|String
name|path
init|=
name|getPath
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|RestRequest
operator|.
name|Method
name|method
init|=
name|request
operator|.
name|method
argument_list|()
decl_stmt|;
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|GET
condition|)
block|{
return|return
name|getHandlers
operator|.
name|retrieve
argument_list|(
name|path
argument_list|,
name|request
operator|.
name|params
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|POST
condition|)
block|{
return|return
name|postHandlers
operator|.
name|retrieve
argument_list|(
name|path
argument_list|,
name|request
operator|.
name|params
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|PUT
condition|)
block|{
return|return
name|putHandlers
operator|.
name|retrieve
argument_list|(
name|path
argument_list|,
name|request
operator|.
name|params
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|method
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|DELETE
condition|)
block|{
return|return
name|deleteHandlers
operator|.
name|retrieve
argument_list|(
name|path
argument_list|,
name|request
operator|.
name|params
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getPath
specifier|private
name|String
name|getPath
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|String
name|uri
init|=
name|request
operator|.
name|uri
argument_list|()
decl_stmt|;
name|int
name|questionMarkIndex
init|=
name|uri
operator|.
name|indexOf
argument_list|(
literal|'?'
argument_list|)
decl_stmt|;
if|if
condition|(
name|questionMarkIndex
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|uri
return|;
block|}
return|return
name|uri
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|questionMarkIndex
argument_list|)
return|;
block|}
block|}
end_class

end_unit

