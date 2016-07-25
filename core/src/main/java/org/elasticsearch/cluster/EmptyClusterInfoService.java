begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * ClusterInfoService that provides empty maps for disk usage and shard sizes  */
end_comment

begin_class
DECL|class|EmptyClusterInfoService
specifier|public
class|class
name|EmptyClusterInfoService
extends|extends
name|AbstractComponent
implements|implements
name|ClusterInfoService
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|EmptyClusterInfoService
name|INSTANCE
init|=
operator|new
name|EmptyClusterInfoService
argument_list|()
decl_stmt|;
DECL|method|EmptyClusterInfoService
specifier|private
name|EmptyClusterInfoService
parameter_list|()
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getClusterInfo
specifier|public
name|ClusterInfo
name|getClusterInfo
parameter_list|()
block|{
return|return
name|ClusterInfo
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
comment|// no-op, no new info is ever gathered, so adding listeners is useless
block|}
block|}
end_class

end_unit

