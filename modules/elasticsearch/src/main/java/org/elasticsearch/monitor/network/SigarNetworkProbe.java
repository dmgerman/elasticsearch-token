begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|monitor
operator|.
name|sigar
operator|.
name|SigarService
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
name|guice
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
name|hyperic
operator|.
name|sigar
operator|.
name|NetInterfaceConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hyperic
operator|.
name|sigar
operator|.
name|Sigar
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hyperic
operator|.
name|sigar
operator|.
name|SigarException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hyperic
operator|.
name|sigar
operator|.
name|Tcp
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SigarNetworkProbe
specifier|public
class|class
name|SigarNetworkProbe
extends|extends
name|AbstractComponent
implements|implements
name|NetworkProbe
block|{
DECL|field|sigarService
specifier|private
specifier|final
name|SigarService
name|sigarService
decl_stmt|;
DECL|method|SigarNetworkProbe
annotation|@
name|Inject
specifier|public
name|SigarNetworkProbe
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|SigarService
name|sigarService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|sigarService
operator|=
name|sigarService
expr_stmt|;
block|}
DECL|method|networkInfo
annotation|@
name|Override
specifier|public
name|NetworkInfo
name|networkInfo
parameter_list|()
block|{
name|Sigar
name|sigar
init|=
name|sigarService
operator|.
name|sigar
argument_list|()
decl_stmt|;
name|NetworkInfo
name|networkInfo
init|=
operator|new
name|NetworkInfo
argument_list|()
decl_stmt|;
try|try
block|{
name|NetInterfaceConfig
name|netInterfaceConfig
init|=
name|sigar
operator|.
name|getNetInterfaceConfig
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|networkInfo
operator|.
name|primary
operator|=
operator|new
name|NetworkInfo
operator|.
name|Interface
argument_list|(
name|netInterfaceConfig
operator|.
name|getName
argument_list|()
argument_list|,
name|netInterfaceConfig
operator|.
name|getAddress
argument_list|()
argument_list|,
name|netInterfaceConfig
operator|.
name|getHwaddr
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SigarException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
name|networkInfo
return|;
block|}
DECL|method|networkStats
annotation|@
name|Override
specifier|public
specifier|synchronized
name|NetworkStats
name|networkStats
parameter_list|()
block|{
name|Sigar
name|sigar
init|=
name|sigarService
operator|.
name|sigar
argument_list|()
decl_stmt|;
name|NetworkStats
name|stats
init|=
operator|new
name|NetworkStats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|timestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
try|try
block|{
name|Tcp
name|tcp
init|=
name|sigar
operator|.
name|getTcp
argument_list|()
decl_stmt|;
name|stats
operator|.
name|tcp
operator|=
operator|new
name|NetworkStats
operator|.
name|Tcp
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|activeOpens
operator|=
name|tcp
operator|.
name|getActiveOpens
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|passiveOpens
operator|=
name|tcp
operator|.
name|getPassiveOpens
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|attemptFails
operator|=
name|tcp
operator|.
name|getAttemptFails
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|estabResets
operator|=
name|tcp
operator|.
name|getEstabResets
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|currEstab
operator|=
name|tcp
operator|.
name|getCurrEstab
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|inSegs
operator|=
name|tcp
operator|.
name|getInSegs
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|outSegs
operator|=
name|tcp
operator|.
name|getOutSegs
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|retransSegs
operator|=
name|tcp
operator|.
name|getRetransSegs
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|inErrs
operator|=
name|tcp
operator|.
name|getInErrs
argument_list|()
expr_stmt|;
name|stats
operator|.
name|tcp
operator|.
name|outRsts
operator|=
name|tcp
operator|.
name|getOutRsts
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SigarException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
name|stats
return|;
block|}
block|}
end_class

end_unit

