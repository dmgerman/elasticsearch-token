begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|routing
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|routing
operator|.
name|hash
operator|.
name|HashFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|routing
operator|.
name|hash
operator|.
name|djb
operator|.
name|DjbHashFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|routing
operator|.
name|plain
operator|.
name|PlainOperationRoutingModule
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
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|ModulesFactory
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|OperationRoutingModule
specifier|public
class|class
name|OperationRoutingModule
extends|extends
name|AbstractModule
block|{
DECL|field|indexSettings
specifier|private
specifier|final
name|Settings
name|indexSettings
decl_stmt|;
DECL|method|OperationRoutingModule
specifier|public
name|OperationRoutingModule
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
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
name|HashFunction
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|indexSettings
operator|.
name|getAsClass
argument_list|(
literal|"index.routing.hash.type"
argument_list|,
name|DjbHashFunction
operator|.
name|class
argument_list|,
literal|"org.elasticsearch.index.routing.hash."
argument_list|,
literal|"HashFunction"
argument_list|)
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|createModule
argument_list|(
name|indexSettings
operator|.
name|getAsClass
argument_list|(
literal|"index.routing.type"
argument_list|,
name|PlainOperationRoutingModule
operator|.
name|class
argument_list|,
literal|"org.elasticsearch.index.routing."
argument_list|,
literal|"OperationRoutingModule"
argument_list|)
argument_list|,
name|indexSettings
argument_list|)
operator|.
name|configure
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

