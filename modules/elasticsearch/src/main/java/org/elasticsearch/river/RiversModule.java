begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
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
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|Maps
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
name|AbstractModule
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
name|Module
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
name|river
operator|.
name|cluster
operator|.
name|RiverClusterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|routing
operator|.
name|RiversRouter
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RiversModule
specifier|public
class|class
name|RiversModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|riverTypes
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
argument_list|>
name|riverTypes
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|RiversModule
specifier|public
name|RiversModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
comment|/**      * Registers a custom river type name against a module.      *      * @param type   The type      * @param module The module      */
DECL|method|registerRiver
specifier|public
name|void
name|registerRiver
parameter_list|(
name|String
name|type
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|module
parameter_list|)
block|{
name|riverTypes
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|module
argument_list|)
expr_stmt|;
block|}
DECL|method|configure
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|String
operator|.
name|class
argument_list|)
operator|.
name|annotatedWith
argument_list|(
name|RiverIndexName
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|RiverIndexName
operator|.
name|Conf
operator|.
name|indexName
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|RiversService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RiverClusterService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RiversRouter
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RiversManager
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RiversTypesRegistry
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
operator|new
name|RiversTypesRegistry
argument_list|(
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|riverTypes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

