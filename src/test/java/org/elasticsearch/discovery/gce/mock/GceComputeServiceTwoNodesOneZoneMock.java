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
name|Arrays
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
DECL|class|GceComputeServiceTwoNodesOneZoneMock
specifier|public
class|class
name|GceComputeServiceTwoNodesOneZoneMock
extends|extends
name|GceComputeServiceAbstractMock
block|{
DECL|field|zones
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|zones
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"us-central1-a"
argument_list|,
literal|"us-central1-a"
argument_list|)
decl_stmt|;
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
argument_list|<>
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
name|zones
return|;
block|}
annotation|@
name|Inject
DECL|method|GceComputeServiceTwoNodesOneZoneMock
specifier|protected
name|GceComputeServiceTwoNodesOneZoneMock
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
block|}
end_class

end_unit

