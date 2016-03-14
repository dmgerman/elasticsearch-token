begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.fd
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|fd
package|;
end_package

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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|common
operator|.
name|settings
operator|.
name|Setting
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
name|Setting
operator|.
name|Property
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportConnectionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_comment
comment|/**  * A base class for {@link org.elasticsearch.discovery.zen.fd.MasterFaultDetection}&amp; {@link org.elasticsearch.discovery.zen.fd.NodesFaultDetection},  * making sure both use the same setting.  */
end_comment

begin_class
DECL|class|FaultDetection
specifier|public
specifier|abstract
class|class
name|FaultDetection
extends|extends
name|AbstractComponent
block|{
DECL|field|CONNECT_ON_NETWORK_DISCONNECT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|CONNECT_ON_NETWORK_DISCONNECT_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"discovery.zen.fd.connect_on_network_disconnect"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PING_INTERVAL_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|PING_INTERVAL_SETTING
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"discovery.zen.fd.ping_interval"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PING_TIMEOUT_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|PING_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"discovery.zen.fd.ping_timeout"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PING_RETRIES_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Integer
argument_list|>
name|PING_RETRIES_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"discovery.zen.fd.ping_retries"
argument_list|,
literal|3
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|REGISTER_CONNECTION_LISTENER_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|REGISTER_CONNECTION_LISTENER_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"discovery.zen.fd.register_connection_listener"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterName
specifier|protected
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|transportService
specifier|protected
specifier|final
name|TransportService
name|transportService
decl_stmt|;
comment|// used mainly for testing, should always be true
DECL|field|registerConnectionListener
specifier|protected
specifier|final
name|boolean
name|registerConnectionListener
decl_stmt|;
DECL|field|connectionListener
specifier|protected
specifier|final
name|FDConnectionListener
name|connectionListener
decl_stmt|;
DECL|field|connectOnNetworkDisconnect
specifier|protected
specifier|final
name|boolean
name|connectOnNetworkDisconnect
decl_stmt|;
DECL|field|pingInterval
specifier|protected
specifier|final
name|TimeValue
name|pingInterval
decl_stmt|;
DECL|field|pingRetryTimeout
specifier|protected
specifier|final
name|TimeValue
name|pingRetryTimeout
decl_stmt|;
DECL|field|pingRetryCount
specifier|protected
specifier|final
name|int
name|pingRetryCount
decl_stmt|;
DECL|method|FaultDetection
specifier|public
name|FaultDetection
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|connectOnNetworkDisconnect
operator|=
name|CONNECT_ON_NETWORK_DISCONNECT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingInterval
operator|=
name|PING_INTERVAL_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingRetryTimeout
operator|=
name|PING_TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingRetryCount
operator|=
name|PING_RETRIES_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|registerConnectionListener
operator|=
name|REGISTER_CONNECTION_LISTENER_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectionListener
operator|=
operator|new
name|FDConnectionListener
argument_list|()
expr_stmt|;
if|if
condition|(
name|registerConnectionListener
condition|)
block|{
name|transportService
operator|.
name|addConnectionListener
argument_list|(
name|connectionListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|transportService
operator|.
name|removeConnectionListener
argument_list|(
name|connectionListener
argument_list|)
expr_stmt|;
block|}
comment|/**      * This method will be called when the {@link org.elasticsearch.transport.TransportService} raised a node disconnected event      */
DECL|method|handleTransportDisconnect
specifier|abstract
name|void
name|handleTransportDisconnect
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
function_decl|;
DECL|class|FDConnectionListener
specifier|private
class|class
name|FDConnectionListener
implements|implements
name|TransportConnectionListener
block|{
annotation|@
name|Override
DECL|method|onNodeConnected
specifier|public
name|void
name|onNodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|onNodeDisconnected
specifier|public
name|void
name|onNodeDisconnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|handleTransportDisconnect
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

