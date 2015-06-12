begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.network
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|network
package|;
end_package

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
name|common
operator|.
name|util
operator|.
name|SingleObjectCache
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
name|NetworkInterface
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NetworkService
specifier|public
specifier|final
class|class
name|NetworkService
extends|extends
name|AbstractComponent
block|{
DECL|field|probe
specifier|private
specifier|final
name|NetworkProbe
name|probe
decl_stmt|;
DECL|field|info
specifier|private
specifier|final
name|NetworkInfo
name|info
decl_stmt|;
DECL|field|networkStatsCache
specifier|private
specifier|final
name|SingleObjectCache
argument_list|<
name|NetworkStats
argument_list|>
name|networkStatsCache
decl_stmt|;
annotation|@
name|Inject
DECL|method|NetworkService
specifier|public
name|NetworkService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NetworkProbe
name|probe
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|probe
operator|=
name|probe
expr_stmt|;
name|TimeValue
name|refreshInterval
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"monitor.network.refresh_interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using probe [{}] with refresh_interval [{}]"
argument_list|,
name|probe
argument_list|,
name|refreshInterval
argument_list|)
expr_stmt|;
name|this
operator|.
name|info
operator|=
name|probe
operator|.
name|networkInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|info
operator|.
name|refreshInterval
operator|=
name|refreshInterval
operator|.
name|millis
argument_list|()
expr_stmt|;
name|networkStatsCache
operator|=
operator|new
name|NetworkStatsCache
argument_list|(
name|refreshInterval
argument_list|,
name|probe
operator|.
name|networkStats
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|netDebug
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"net_info"
argument_list|)
decl_stmt|;
try|try
block|{
name|Enumeration
argument_list|<
name|NetworkInterface
argument_list|>
name|interfaces
init|=
name|NetworkInterface
operator|.
name|getNetworkInterfaces
argument_list|()
decl_stmt|;
name|String
name|hostName
init|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
decl_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\nhost ["
argument_list|)
operator|.
name|append
argument_list|(
name|hostName
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
while|while
condition|(
name|interfaces
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|NetworkInterface
name|net
init|=
name|interfaces
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
name|net
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\t'
argument_list|)
operator|.
name|append
argument_list|(
literal|"display_name ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|getDisplayName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
name|Enumeration
argument_list|<
name|InetAddress
argument_list|>
name|addresses
init|=
name|net
operator|.
name|getInetAddresses
argument_list|()
decl_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\taddress "
argument_list|)
expr_stmt|;
while|while
condition|(
name|addresses
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|netDebug
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|addresses
operator|.
name|nextElement
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
block|}
name|netDebug
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\tmtu ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|getMTU
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] multicast ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|supportsMulticast
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] ptp ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isPointToPoint
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] loopback ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isLoopback
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] up ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isUp
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] virtual ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isVirtual
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|Enumeration
argument_list|<
name|NetworkInterface
argument_list|>
name|subInterfaces
init|=
name|net
operator|.
name|getSubInterfaces
argument_list|()
decl_stmt|;
if|if
condition|(
name|subInterfaces
operator|!=
literal|null
operator|&&
name|subInterfaces
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\t\tsub interfaces:\n"
argument_list|)
expr_stmt|;
while|while
condition|(
name|subInterfaces
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|net
operator|=
name|subInterfaces
operator|.
name|nextElement
argument_list|()
expr_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\t\t"
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"\t"
argument_list|)
operator|.
name|append
argument_list|(
literal|"display_name ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|getDisplayName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
name|addresses
operator|=
name|net
operator|.
name|getInetAddresses
argument_list|()
expr_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\t\t\t\taddress "
argument_list|)
expr_stmt|;
while|while
condition|(
name|addresses
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|netDebug
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|addresses
operator|.
name|nextElement
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
expr_stmt|;
block|}
name|netDebug
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|netDebug
operator|.
name|append
argument_list|(
literal|"\t\t\t\t\tmtu ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|getMTU
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] multicast ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|supportsMulticast
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] ptp ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isPointToPoint
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] loopback ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isLoopback
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] up ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isUp
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] virtual ["
argument_list|)
operator|.
name|append
argument_list|(
name|net
operator|.
name|isVirtual
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|netDebug
operator|.
name|append
argument_list|(
literal|"failed to get Network Interface Info ["
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
name|netDebug
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"ifconfig\n\n"
operator|+
name|ifconfig
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|stats
argument_list|()
expr_stmt|;
comment|// pull the stats one time
block|}
DECL|method|info
specifier|public
name|NetworkInfo
name|info
parameter_list|()
block|{
return|return
name|this
operator|.
name|info
return|;
block|}
DECL|method|stats
specifier|public
name|NetworkStats
name|stats
parameter_list|()
block|{
return|return
name|networkStatsCache
operator|.
name|getOrRefresh
argument_list|()
return|;
block|}
DECL|class|NetworkStatsCache
specifier|private
class|class
name|NetworkStatsCache
extends|extends
name|SingleObjectCache
argument_list|<
name|NetworkStats
argument_list|>
block|{
DECL|method|NetworkStatsCache
specifier|public
name|NetworkStatsCache
parameter_list|(
name|TimeValue
name|interval
parameter_list|,
name|NetworkStats
name|initValue
parameter_list|)
block|{
name|super
argument_list|(
name|interval
argument_list|,
name|initValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|refresh
specifier|protected
name|NetworkStats
name|refresh
parameter_list|()
block|{
return|return
name|probe
operator|.
name|networkStats
argument_list|()
return|;
block|}
block|}
DECL|method|ifconfig
specifier|public
name|String
name|ifconfig
parameter_list|()
block|{
return|return
name|probe
operator|.
name|ifconfig
argument_list|()
return|;
block|}
block|}
end_class

end_unit
