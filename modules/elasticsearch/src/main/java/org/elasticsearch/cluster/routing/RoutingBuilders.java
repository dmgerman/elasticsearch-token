begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
package|;
end_package

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|RoutingBuilders
specifier|public
specifier|final
class|class
name|RoutingBuilders
block|{
DECL|method|RoutingBuilders
specifier|private
name|RoutingBuilders
parameter_list|()
block|{      }
DECL|method|routingTable
specifier|public
specifier|static
name|RoutingTable
operator|.
name|Builder
name|routingTable
parameter_list|()
block|{
return|return
operator|new
name|RoutingTable
operator|.
name|Builder
argument_list|()
return|;
block|}
DECL|method|indexRoutingTable
specifier|public
specifier|static
name|IndexRoutingTable
operator|.
name|Builder
name|indexRoutingTable
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
operator|new
name|IndexRoutingTable
operator|.
name|Builder
argument_list|(
name|index
argument_list|)
return|;
block|}
block|}
end_class

end_unit

