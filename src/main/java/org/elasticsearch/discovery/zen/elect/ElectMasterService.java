begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.elect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|elect
package|;
end_package

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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CollectionUtil
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
DECL|class|ElectMasterService
specifier|public
class|class
name|ElectMasterService
extends|extends
name|AbstractComponent
block|{
DECL|field|DISCOVERY_ZEN_MINIMUM_MASTER_NODES
specifier|public
specifier|static
specifier|final
name|String
name|DISCOVERY_ZEN_MINIMUM_MASTER_NODES
init|=
literal|"discovery.zen.minimum_master_nodes"
decl_stmt|;
DECL|field|nodeComparator
specifier|private
specifier|final
name|NodeComparator
name|nodeComparator
init|=
operator|new
name|NodeComparator
argument_list|()
decl_stmt|;
DECL|field|minimumMasterNodes
specifier|private
specifier|volatile
name|int
name|minimumMasterNodes
decl_stmt|;
DECL|method|ElectMasterService
specifier|public
name|ElectMasterService
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
name|this
operator|.
name|minimumMasterNodes
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
name|DISCOVERY_ZEN_MINIMUM_MASTER_NODES
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using minimum_master_nodes [{}]"
argument_list|,
name|minimumMasterNodes
argument_list|)
expr_stmt|;
block|}
DECL|method|minimumMasterNodes
specifier|public
name|void
name|minimumMasterNodes
parameter_list|(
name|int
name|minimumMasterNodes
parameter_list|)
block|{
name|this
operator|.
name|minimumMasterNodes
operator|=
name|minimumMasterNodes
expr_stmt|;
block|}
DECL|method|minimumMasterNodes
specifier|public
name|int
name|minimumMasterNodes
parameter_list|()
block|{
return|return
name|minimumMasterNodes
return|;
block|}
DECL|method|hasEnoughMasterNodes
specifier|public
name|boolean
name|hasEnoughMasterNodes
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
if|if
condition|(
name|minimumMasterNodes
operator|<
literal|1
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
if|if
condition|(
name|node
operator|.
name|masterNode
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
return|return
name|count
operator|>=
name|minimumMasterNodes
return|;
block|}
comment|/**      * Returns a list of the next possible masters.      */
DECL|method|nextPossibleMasters
specifier|public
name|DiscoveryNode
index|[]
name|nextPossibleMasters
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|,
name|int
name|numberOfPossibleMasters
parameter_list|)
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|sortedNodes
init|=
name|sortedMasterNodes
argument_list|(
name|nodes
argument_list|)
decl_stmt|;
if|if
condition|(
name|sortedNodes
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|DiscoveryNode
index|[
literal|0
index|]
return|;
block|}
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nextPossibleMasters
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|numberOfPossibleMasters
argument_list|)
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|nextPossibleMaster
range|:
name|sortedNodes
control|)
block|{
if|if
condition|(
operator|++
name|counter
operator|>=
name|numberOfPossibleMasters
condition|)
block|{
break|break;
block|}
name|nextPossibleMasters
operator|.
name|add
argument_list|(
name|nextPossibleMaster
argument_list|)
expr_stmt|;
block|}
return|return
name|nextPossibleMasters
operator|.
name|toArray
argument_list|(
operator|new
name|DiscoveryNode
index|[
name|nextPossibleMasters
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**      * Elects a new master out of the possible nodes, returning it. Returns<tt>null</tt>      * if no master has been elected.      */
DECL|method|electMaster
specifier|public
name|DiscoveryNode
name|electMaster
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|sortedNodes
init|=
name|sortedMasterNodes
argument_list|(
name|nodes
argument_list|)
decl_stmt|;
if|if
condition|(
name|sortedNodes
operator|==
literal|null
operator|||
name|sortedNodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|sortedNodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
DECL|method|sortedMasterNodes
specifier|private
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|sortedMasterNodes
parameter_list|(
name|Iterable
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|)
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|possibleNodes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|nodes
argument_list|)
decl_stmt|;
if|if
condition|(
name|possibleNodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// clean non master nodes
for|for
control|(
name|Iterator
argument_list|<
name|DiscoveryNode
argument_list|>
name|it
init|=
name|possibleNodes
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|node
operator|.
name|masterNode
argument_list|()
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|possibleNodes
argument_list|,
name|nodeComparator
argument_list|)
expr_stmt|;
return|return
name|possibleNodes
return|;
block|}
DECL|class|NodeComparator
specifier|private
specifier|static
class|class
name|NodeComparator
implements|implements
name|Comparator
argument_list|<
name|DiscoveryNode
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|DiscoveryNode
name|o1
parameter_list|,
name|DiscoveryNode
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|id
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|id
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

