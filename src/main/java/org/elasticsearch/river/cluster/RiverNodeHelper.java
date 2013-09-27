begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
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
name|river
operator|.
name|RiverName
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RiverNodeHelper
specifier|public
class|class
name|RiverNodeHelper
block|{
DECL|method|isRiverNode
specifier|public
specifier|static
name|boolean
name|isRiverNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
comment|// we don't allocate rivers on client nodes
if|if
condition|(
name|node
operator|.
name|clientNode
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|river
init|=
name|node
operator|.
name|attributes
argument_list|()
operator|.
name|get
argument_list|(
literal|"river"
argument_list|)
decl_stmt|;
comment|// by default, if not set, it's a river node (better OOB exp)
if|if
condition|(
name|river
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
literal|"_none_"
operator|.
name|equals
argument_list|(
name|river
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// there is at least one river settings, we need it
return|return
literal|true
return|;
block|}
DECL|method|isRiverNode
specifier|public
specifier|static
name|boolean
name|isRiverNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|RiverName
name|riverName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isRiverNode
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|river
init|=
name|node
operator|.
name|attributes
argument_list|()
operator|.
name|get
argument_list|(
literal|"river"
argument_list|)
decl_stmt|;
comment|// by default, if not set, its an river node (better OOB exp)
return|return
name|river
operator|==
literal|null
operator|||
name|river
operator|.
name|contains
argument_list|(
name|riverName
operator|.
name|type
argument_list|()
argument_list|)
operator|||
name|river
operator|.
name|contains
argument_list|(
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

