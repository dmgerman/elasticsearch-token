begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
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
name|ImmutableList
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
name|ImmutableMap
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
name|UnmodifiableIterator
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
name|Nullable
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Nodes
specifier|public
class|class
name|Nodes
implements|implements
name|Iterable
argument_list|<
name|Node
argument_list|>
block|{
DECL|field|EMPTY_NODES
specifier|public
specifier|static
name|Nodes
name|EMPTY_NODES
init|=
name|newNodesBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodes
decl_stmt|;
DECL|field|dataNodes
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|dataNodes
decl_stmt|;
DECL|field|masterNodeId
specifier|private
specifier|final
name|String
name|masterNodeId
decl_stmt|;
DECL|field|localNodeId
specifier|private
specifier|final
name|String
name|localNodeId
decl_stmt|;
DECL|method|Nodes
specifier|private
name|Nodes
parameter_list|(
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodes
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|dataNodes
parameter_list|,
name|String
name|masterNodeId
parameter_list|,
name|String
name|localNodeId
parameter_list|)
block|{
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
name|this
operator|.
name|dataNodes
operator|=
name|dataNodes
expr_stmt|;
name|this
operator|.
name|masterNodeId
operator|=
name|masterNodeId
expr_stmt|;
name|this
operator|.
name|localNodeId
operator|=
name|localNodeId
expr_stmt|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|Node
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|nodes
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**      * Is this a valid nodes that has the minimal information set. The minimal set is defined      * by the localNodeId being set.      */
DECL|method|valid
specifier|public
name|boolean
name|valid
parameter_list|()
block|{
return|return
name|localNodeId
operator|!=
literal|null
return|;
block|}
comment|/**      * Returns<tt>true</tt> if the local node is the master node.      */
DECL|method|localNodeMaster
specifier|public
name|boolean
name|localNodeMaster
parameter_list|()
block|{
if|if
condition|(
name|localNodeId
operator|==
literal|null
condition|)
block|{
comment|// we don't know yet the local node id, return false
return|return
literal|false
return|;
block|}
return|return
name|localNodeId
operator|.
name|equals
argument_list|(
name|masterNodeId
argument_list|)
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|nodes
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|nodes
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodes
return|;
block|}
DECL|method|dataNodes
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|dataNodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|dataNodes
return|;
block|}
DECL|method|get
specifier|public
name|Node
name|get
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|nodes
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
return|;
block|}
DECL|method|nodeExists
specifier|public
name|boolean
name|nodeExists
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|nodes
operator|.
name|containsKey
argument_list|(
name|nodeId
argument_list|)
return|;
block|}
DECL|method|masterNodeId
specifier|public
name|String
name|masterNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|masterNodeId
return|;
block|}
DECL|method|localNodeId
specifier|public
name|String
name|localNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|localNodeId
return|;
block|}
DECL|method|localNode
specifier|public
name|Node
name|localNode
parameter_list|()
block|{
return|return
name|nodes
operator|.
name|get
argument_list|(
name|localNodeId
argument_list|)
return|;
block|}
DECL|method|masterNode
specifier|public
name|Node
name|masterNode
parameter_list|()
block|{
return|return
name|nodes
operator|.
name|get
argument_list|(
name|masterNodeId
argument_list|)
return|;
block|}
DECL|method|removeDeadMembers
specifier|public
name|Nodes
name|removeDeadMembers
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|newNodes
parameter_list|,
name|String
name|masterNodeId
parameter_list|)
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|()
operator|.
name|masterNodeId
argument_list|(
name|masterNodeId
argument_list|)
operator|.
name|localNodeId
argument_list|(
name|localNodeId
argument_list|)
decl_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|this
control|)
block|{
if|if
condition|(
name|newNodes
operator|.
name|contains
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|newNode
specifier|public
name|Nodes
name|newNode
parameter_list|(
name|Node
name|node
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|()
operator|.
name|putAll
argument_list|(
name|this
argument_list|)
operator|.
name|put
argument_list|(
name|node
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * Returns the changes comparing this nodes to the provided nodes.      */
DECL|method|delta
specifier|public
name|Delta
name|delta
parameter_list|(
name|Nodes
name|other
parameter_list|)
block|{
name|List
argument_list|<
name|Node
argument_list|>
name|removed
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|added
init|=
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|other
control|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|nodeExists
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
name|removed
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Node
name|node
range|:
name|this
control|)
block|{
if|if
condition|(
operator|!
name|other
operator|.
name|nodeExists
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
name|added
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
name|Node
name|previousMasterNode
init|=
literal|null
decl_stmt|;
name|Node
name|newMasterNode
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|masterNodeId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|masterNodeId
operator|==
literal|null
operator|||
operator|!
name|other
operator|.
name|masterNodeId
operator|.
name|equals
argument_list|(
name|masterNodeId
argument_list|)
condition|)
block|{
name|previousMasterNode
operator|=
name|other
operator|.
name|masterNode
argument_list|()
expr_stmt|;
name|newMasterNode
operator|=
name|masterNode
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Delta
argument_list|(
name|previousMasterNode
argument_list|,
name|newMasterNode
argument_list|,
name|localNodeId
argument_list|,
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|removed
argument_list|)
argument_list|,
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|added
argument_list|)
argument_list|)
return|;
block|}
DECL|method|prettyPrint
specifier|public
name|String
name|prettyPrint
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Nodes: \n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|this
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"   "
argument_list|)
operator|.
name|append
argument_list|(
name|node
argument_list|)
expr_stmt|;
if|if
condition|(
name|node
operator|==
name|localNode
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", local"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|node
operator|==
name|masterNode
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", master"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|emptyDelta
specifier|public
name|Delta
name|emptyDelta
parameter_list|()
block|{
return|return
operator|new
name|Delta
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|localNodeId
argument_list|,
name|Node
operator|.
name|EMPTY_LIST
argument_list|,
name|Node
operator|.
name|EMPTY_LIST
argument_list|)
return|;
block|}
DECL|class|Delta
specifier|public
specifier|static
class|class
name|Delta
block|{
DECL|field|localNodeId
specifier|private
specifier|final
name|String
name|localNodeId
decl_stmt|;
DECL|field|previousMasterNode
specifier|private
specifier|final
name|Node
name|previousMasterNode
decl_stmt|;
DECL|field|newMasterNode
specifier|private
specifier|final
name|Node
name|newMasterNode
decl_stmt|;
DECL|field|removed
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|removed
decl_stmt|;
DECL|field|added
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|added
decl_stmt|;
DECL|method|Delta
specifier|public
name|Delta
parameter_list|(
name|String
name|localNodeId
parameter_list|,
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|removed
parameter_list|,
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|added
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|localNodeId
argument_list|,
name|removed
argument_list|,
name|added
argument_list|)
expr_stmt|;
block|}
DECL|method|Delta
specifier|public
name|Delta
parameter_list|(
annotation|@
name|Nullable
name|Node
name|previousMasterNode
parameter_list|,
annotation|@
name|Nullable
name|Node
name|newMasterNode
parameter_list|,
name|String
name|localNodeId
parameter_list|,
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|removed
parameter_list|,
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|added
parameter_list|)
block|{
name|this
operator|.
name|previousMasterNode
operator|=
name|previousMasterNode
expr_stmt|;
name|this
operator|.
name|newMasterNode
operator|=
name|newMasterNode
expr_stmt|;
name|this
operator|.
name|localNodeId
operator|=
name|localNodeId
expr_stmt|;
name|this
operator|.
name|removed
operator|=
name|removed
expr_stmt|;
name|this
operator|.
name|added
operator|=
name|added
expr_stmt|;
block|}
DECL|method|hasChanges
specifier|public
name|boolean
name|hasChanges
parameter_list|()
block|{
return|return
name|masterNodeChanged
argument_list|()
operator|||
operator|!
name|removed
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|added
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|masterNodeChanged
specifier|public
name|boolean
name|masterNodeChanged
parameter_list|()
block|{
return|return
name|newMasterNode
operator|!=
literal|null
return|;
block|}
DECL|method|previousMasterNode
specifier|public
name|Node
name|previousMasterNode
parameter_list|()
block|{
return|return
name|previousMasterNode
return|;
block|}
DECL|method|newMasterNode
specifier|public
name|Node
name|newMasterNode
parameter_list|()
block|{
return|return
name|newMasterNode
return|;
block|}
DECL|method|removed
specifier|public
name|boolean
name|removed
parameter_list|()
block|{
return|return
operator|!
name|removed
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|removedNodes
specifier|public
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|removedNodes
parameter_list|()
block|{
return|return
name|removed
return|;
block|}
DECL|method|added
specifier|public
name|boolean
name|added
parameter_list|()
block|{
return|return
operator|!
name|added
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|addedNodes
specifier|public
name|ImmutableList
argument_list|<
name|Node
argument_list|>
name|addedNodes
parameter_list|()
block|{
return|return
name|added
return|;
block|}
DECL|method|shortSummary
specifier|public
name|String
name|shortSummary
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|removed
argument_list|()
operator|&&
name|masterNodeChanged
argument_list|()
condition|)
block|{
if|if
condition|(
name|newMasterNode
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|localNodeId
argument_list|)
condition|)
block|{
comment|// we are the master, no nodes we removed, we are actually the first master
name|sb
operator|.
name|append
argument_list|(
literal|"New Master "
argument_list|)
operator|.
name|append
argument_list|(
name|newMasterNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we are not the master, so we just got this event. No nodes were removed, so its not a *new* master
name|sb
operator|.
name|append
argument_list|(
literal|"Detected Master "
argument_list|)
operator|.
name|append
argument_list|(
name|newMasterNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|masterNodeChanged
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Master {New "
argument_list|)
operator|.
name|append
argument_list|(
name|newMasterNode
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|previousMasterNode
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", Previous "
argument_list|)
operator|.
name|append
argument_list|(
name|previousMasterNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|removed
argument_list|()
condition|)
block|{
if|if
condition|(
name|masterNodeChanged
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"Removed {"
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|removedNodes
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|node
argument_list|)
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|added
argument_list|()
condition|)
block|{
comment|// don't print if there is one added, and it is us
if|if
condition|(
operator|!
operator|(
name|addedNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|addedNodes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|localNodeId
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|removed
argument_list|()
operator|||
name|masterNodeChanged
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"Added {"
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|addedNodes
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|localNodeId
argument_list|)
condition|)
block|{
comment|// don't print ourself
name|sb
operator|.
name|append
argument_list|(
name|node
argument_list|)
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
DECL|method|newNodesBuilder
specifier|public
specifier|static
name|Builder
name|newNodesBuilder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|nodes
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodes
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|masterNodeId
specifier|private
name|String
name|masterNodeId
decl_stmt|;
DECL|field|localNodeId
specifier|private
name|String
name|localNodeId
decl_stmt|;
DECL|method|putAll
specifier|public
name|Builder
name|putAll
parameter_list|(
name|Nodes
name|nodes
parameter_list|)
block|{
name|this
operator|.
name|masterNodeId
operator|=
name|nodes
operator|.
name|masterNodeId
argument_list|()
expr_stmt|;
name|this
operator|.
name|localNodeId
operator|=
name|nodes
operator|.
name|localNodeId
argument_list|()
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|put
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|put
specifier|public
name|Builder
name|put
parameter_list|(
name|Node
name|node
parameter_list|)
block|{
name|nodes
operator|.
name|put
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|,
name|node
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|putAll
specifier|public
name|Builder
name|putAll
parameter_list|(
name|Iterable
argument_list|<
name|Node
argument_list|>
name|nodes
parameter_list|)
block|{
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|put
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|remove
specifier|public
name|Builder
name|remove
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|nodes
operator|.
name|remove
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|masterNodeId
specifier|public
name|Builder
name|masterNodeId
parameter_list|(
name|String
name|masterNodeId
parameter_list|)
block|{
name|this
operator|.
name|masterNodeId
operator|=
name|masterNodeId
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|localNodeId
specifier|public
name|Builder
name|localNodeId
parameter_list|(
name|String
name|localNodeId
parameter_list|)
block|{
name|this
operator|.
name|localNodeId
operator|=
name|localNodeId
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|Nodes
name|build
parameter_list|()
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|dataNodesBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodeEntry
range|:
name|nodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|nodeEntry
operator|.
name|getValue
argument_list|()
operator|.
name|dataNode
argument_list|()
condition|)
block|{
name|dataNodesBuilder
operator|.
name|put
argument_list|(
name|nodeEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|nodeEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Nodes
argument_list|(
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|nodes
argument_list|)
argument_list|,
name|dataNodesBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|masterNodeId
argument_list|,
name|localNodeId
argument_list|)
return|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|Nodes
name|nodes
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|nodes
operator|.
name|masterNodeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
control|)
block|{
name|node
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|Nodes
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|,
annotation|@
name|Nullable
name|Node
name|localNode
parameter_list|)
throws|throws
name|IOException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|masterNodeId
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|localNode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|localNodeId
argument_list|(
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Node
name|node
init|=
name|Node
operator|.
name|readNode
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|localNode
operator|!=
literal|null
operator|&&
name|node
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
comment|// reuse the same instance of our address and local node id for faster equality
name|node
operator|=
name|localNode
expr_stmt|;
block|}
name|builder
operator|.
name|put
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

