begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.gce.mock
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|gce
operator|.
name|mock
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|services
operator|.
name|compute
operator|.
name|model
operator|.
name|Instance
import|;
end_import

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
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
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
name|Collection
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|GceComputeServiceZeroNodeMock
specifier|public
class|class
name|GceComputeServiceZeroNodeMock
extends|extends
name|GceComputeServiceAbstractMock
block|{
annotation|@
name|Override
DECL|method|getTags
specifier|protected
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTags
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getZones
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getZones
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|()
return|;
block|}
DECL|field|zoneList
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|zoneList
decl_stmt|;
annotation|@
name|Override
DECL|method|instances
specifier|public
name|Collection
argument_list|<
name|Instance
argument_list|>
name|instances
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"get instances for zoneList [{}]"
argument_list|,
name|zoneList
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Instance
argument_list|>
argument_list|>
name|instanceListByZone
init|=
name|Lists
operator|.
name|transform
argument_list|(
name|zoneList
argument_list|,
operator|new
name|Function
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Instance
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Instance
argument_list|>
name|apply
parameter_list|(
name|String
name|zoneId
parameter_list|)
block|{
comment|// If we return null here we will get a trace as explained in issue 43
return|return
operator|new
name|ArrayList
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|//Collapse instances from all zones into one neat list
name|List
argument_list|<
name|Instance
argument_list|>
name|instanceList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Iterables
operator|.
name|concat
argument_list|(
name|instanceListByZone
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|instanceList
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"disabling GCE discovery. Can not get list of nodes"
argument_list|)
expr_stmt|;
block|}
return|return
name|instanceList
return|;
block|}
annotation|@
name|Inject
DECL|method|GceComputeServiceZeroNodeMock
specifier|protected
name|GceComputeServiceZeroNodeMock
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
name|String
index|[]
name|zoneList
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
name|Fields
operator|.
name|ZONE
argument_list|)
decl_stmt|;
name|this
operator|.
name|zoneList
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|zoneList
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
