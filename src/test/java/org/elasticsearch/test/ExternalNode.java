begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
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
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
import|;
end_import

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
name|client
operator|.
name|transport
operator|.
name|TransportClient
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
name|TransportAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|IOException
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
name|Random
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
name|settingsBuilder
import|;
end_import

begin_comment
comment|/**  * Simple helper class to start external nodes to be used within a test cluster  */
end_comment

begin_class
DECL|class|ExternalNode
specifier|final
class|class
name|ExternalNode
implements|implements
name|Closeable
block|{
DECL|field|path
specifier|private
specifier|final
name|File
name|path
decl_stmt|;
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|process
specifier|private
name|Process
name|process
decl_stmt|;
DECL|field|nodeInfo
specifier|private
name|NodeInfo
name|nodeInfo
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|String
name|clusterName
decl_stmt|;
DECL|field|client
specifier|private
name|TransportClient
name|client
decl_stmt|;
DECL|method|ExternalNode
name|ExternalNode
parameter_list|(
name|File
name|path
parameter_list|,
name|long
name|seed
parameter_list|)
block|{
name|this
argument_list|(
name|path
argument_list|,
literal|null
argument_list|,
name|seed
argument_list|)
expr_stmt|;
block|}
DECL|method|ExternalNode
name|ExternalNode
parameter_list|(
name|File
name|path
parameter_list|,
name|String
name|clusterName
parameter_list|,
name|long
name|seed
parameter_list|)
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"path must be a directory"
argument_list|)
throw|;
block|}
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|synchronized
name|ExternalNode
name|start
parameter_list|(
name|Client
name|localNode
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|nodeName
parameter_list|,
name|String
name|clusterName
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|ExternalNode
name|externalNode
init|=
operator|new
name|ExternalNode
argument_list|(
name|path
argument_list|,
name|clusterName
argument_list|,
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|externalNode
operator|.
name|startInternal
argument_list|(
name|localNode
argument_list|,
name|settings
argument_list|,
name|nodeName
argument_list|,
name|clusterName
argument_list|)
expr_stmt|;
return|return
name|externalNode
return|;
block|}
DECL|method|startInternal
specifier|synchronized
name|void
name|startInternal
parameter_list|(
name|Client
name|client
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|nodeName
parameter_list|,
name|String
name|clusterName
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|process
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Already started"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Constants
operator|.
name|WINDOWS
condition|)
block|{
name|params
operator|.
name|add
argument_list|(
literal|"bin/elasticsearch"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|params
operator|.
name|add
argument_list|(
literal|"bin/elasticsearch.bat"
argument_list|)
expr_stmt|;
block|}
name|params
operator|.
name|add
argument_list|(
literal|"-Des.cluster.name="
operator|+
name|clusterName
argument_list|)
expr_stmt|;
name|params
operator|.
name|add
argument_list|(
literal|"-Des.node.name="
operator|+
name|nodeName
argument_list|)
expr_stmt|;
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
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
switch|switch
condition|(
name|entry
operator|.
name|getKey
argument_list|()
condition|)
block|{
case|case
literal|"cluster.name"
case|:
case|case
literal|"node.name"
case|:
case|case
literal|"path.home"
case|:
case|case
literal|"node.mode"
case|:
case|case
literal|"gateway.type"
case|:
case|case
literal|"config.ignore_system_properties"
case|:
continue|continue;
default|default:
name|params
operator|.
name|add
argument_list|(
literal|"-Des."
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"="
operator|+
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|params
operator|.
name|add
argument_list|(
literal|"-Des.gateway.type=local"
argument_list|)
expr_stmt|;
name|params
operator|.
name|add
argument_list|(
literal|"-Des.path.home="
operator|+
operator|new
name|File
argument_list|(
literal|""
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|ProcessBuilder
name|builder
init|=
operator|new
name|ProcessBuilder
argument_list|(
name|params
argument_list|)
decl_stmt|;
name|builder
operator|.
name|directory
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|builder
operator|.
name|inheritIO
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|process
operator|=
name|builder
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|nodeInfo
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|waitForNode
argument_list|(
name|client
argument_list|,
name|nodeName
argument_list|)
condition|)
block|{
name|nodeInfo
operator|=
name|nodeInfo
argument_list|(
name|client
argument_list|,
name|nodeName
argument_list|)
expr_stmt|;
assert|assert
name|nodeInfo
operator|!=
literal|null
assert|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Node ["
operator|+
name|nodeName
operator|+
literal|"] didn't join the cluster"
argument_list|)
throw|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|waitForNode
specifier|static
name|boolean
name|waitForNode
parameter_list|(
specifier|final
name|Client
name|client
parameter_list|,
specifier|final
name|String
name|name
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
name|ElasticsearchTestCase
operator|.
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|Object
name|input
parameter_list|)
block|{
specifier|final
name|NodesInfoResponse
name|nodeInfos
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|NodeInfo
index|[]
name|nodes
init|=
name|nodeInfos
operator|.
name|getNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeInfo
name|info
range|:
name|nodes
control|)
block|{
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
return|;
block|}
DECL|method|nodeInfo
specifier|static
name|NodeInfo
name|nodeInfo
parameter_list|(
specifier|final
name|Client
name|client
parameter_list|,
specifier|final
name|String
name|nodeName
parameter_list|)
block|{
specifier|final
name|NodesInfoResponse
name|nodeInfos
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|NodeInfo
index|[]
name|nodes
init|=
name|nodeInfos
operator|.
name|getNodes
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeInfo
name|info
range|:
name|nodes
control|)
block|{
if|if
condition|(
name|nodeName
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|info
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
DECL|method|getTransportAddress
specifier|synchronized
name|TransportAddress
name|getTransportAddress
parameter_list|()
block|{
if|if
condition|(
name|nodeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Node has not started yet"
argument_list|)
throw|;
block|}
return|return
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
return|;
block|}
DECL|method|address
name|TransportAddress
name|address
parameter_list|()
block|{
if|if
condition|(
name|nodeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Node has not started yet"
argument_list|)
throw|;
block|}
return|return
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
return|;
block|}
DECL|method|getClient
specifier|synchronized
name|Client
name|getClient
parameter_list|()
block|{
if|if
condition|(
name|nodeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Node has not started yet"
argument_list|)
throw|;
block|}
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
name|TransportAddress
name|addr
init|=
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
decl_stmt|;
name|TransportClient
name|client
init|=
operator|new
name|TransportClient
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.nodes_sampler_interval"
argument_list|,
literal|"1s"
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"transport_client_"
operator|+
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|,
name|clusterName
argument_list|)
operator|.
name|put
argument_list|(
literal|"client.transport.sniff"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|addTransportAddress
argument_list|(
name|addr
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
DECL|method|reset
specifier|synchronized
name|void
name|reset
parameter_list|(
name|long
name|seed
parameter_list|)
block|{
name|this
operator|.
name|random
operator|.
name|setSeed
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
DECL|method|stop
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
name|stop
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|stop
specifier|synchronized
name|void
name|stop
parameter_list|(
name|boolean
name|forceKill
parameter_list|)
block|{
if|if
condition|(
name|running
argument_list|()
condition|)
block|{
try|try
block|{
if|if
condition|(
name|forceKill
operator|==
literal|false
operator|&&
name|nodeInfo
operator|!=
literal|null
operator|&&
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// sometimes shut down gracefully
name|getClient
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesShutdown
argument_list|(
name|this
operator|.
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|setExit
argument_list|(
name|random
operator|.
name|nextBoolean
argument_list|()
argument_list|)
operator|.
name|setDelay
argument_list|(
literal|"0s"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
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
block|}
finally|finally
block|{
name|process
operator|.
name|destroy
argument_list|()
expr_stmt|;
try|try
block|{
name|process
operator|.
name|waitFor
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
name|process
operator|=
literal|null
expr_stmt|;
name|nodeInfo
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
DECL|method|running
specifier|synchronized
name|boolean
name|running
parameter_list|()
block|{
return|return
name|process
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
DECL|method|getName
specifier|synchronized
name|String
name|getName
parameter_list|()
block|{
if|if
condition|(
name|nodeInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Node has not started yet"
argument_list|)
throw|;
block|}
return|return
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
end_class

end_unit

