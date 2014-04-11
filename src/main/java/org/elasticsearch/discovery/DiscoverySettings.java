begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
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
name|block
operator|.
name|ClusterBlock
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
name|block
operator|.
name|ClusterBlockLevel
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
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_comment
comment|/**  * Exposes common discovery settings that may be supported by all the different discovery implementations  */
end_comment

begin_class
DECL|class|DiscoverySettings
specifier|public
class|class
name|DiscoverySettings
extends|extends
name|AbstractComponent
block|{
DECL|field|PUBLISH_TIMEOUT
specifier|public
specifier|static
specifier|final
name|String
name|PUBLISH_TIMEOUT
init|=
literal|"discovery.zen.publish_timeout"
decl_stmt|;
DECL|field|DEFAULT_PUBLISH_TIMEOUT
specifier|public
specifier|static
specifier|final
name|TimeValue
name|DEFAULT_PUBLISH_TIMEOUT
init|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
decl_stmt|;
DECL|field|publishTimeout
specifier|private
specifier|volatile
name|TimeValue
name|publishTimeout
init|=
name|DEFAULT_PUBLISH_TIMEOUT
decl_stmt|;
DECL|field|NO_MASTER_BLOCK_ID
specifier|public
specifier|final
specifier|static
name|int
name|NO_MASTER_BLOCK_ID
init|=
literal|2
decl_stmt|;
DECL|field|noMasterBlock
specifier|private
specifier|final
name|ClusterBlock
name|noMasterBlock
decl_stmt|;
annotation|@
name|Inject
DECL|method|DiscoverySettings
specifier|public
name|DiscoverySettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|noMasterBlock
operator|=
operator|new
name|ClusterBlock
argument_list|(
name|NO_MASTER_BLOCK_ID
argument_list|,
literal|"no master"
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|,
name|ClusterBlockLevel
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the current publish timeout      */
DECL|method|getPublishTimeout
specifier|public
name|TimeValue
name|getPublishTimeout
parameter_list|()
block|{
return|return
name|publishTimeout
return|;
block|}
DECL|method|getNoMasterBlock
specifier|public
name|ClusterBlock
name|getNoMasterBlock
parameter_list|()
block|{
return|return
name|noMasterBlock
return|;
block|}
DECL|class|ApplySettings
specifier|private
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|TimeValue
name|newPublishTimeout
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|PUBLISH_TIMEOUT
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|newPublishTimeout
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|newPublishTimeout
operator|.
name|millis
argument_list|()
operator|!=
name|publishTimeout
operator|.
name|millis
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [{}] from [{}] to [{}]"
argument_list|,
name|PUBLISH_TIMEOUT
argument_list|,
name|publishTimeout
argument_list|,
name|newPublishTimeout
argument_list|)
expr_stmt|;
name|publishTimeout
operator|=
name|newPublishTimeout
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

