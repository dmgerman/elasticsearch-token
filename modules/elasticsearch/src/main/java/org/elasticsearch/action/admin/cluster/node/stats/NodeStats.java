begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|stats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|nodes
operator|.
name|NodeOperationResponse
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
name|monitor
operator|.
name|jvm
operator|.
name|JvmStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|network
operator|.
name|NetworkStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|os
operator|.
name|OsStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|process
operator|.
name|ProcessStats
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

begin_comment
comment|/**  * Node statistics (static, does not change over time).  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|NodeStats
specifier|public
class|class
name|NodeStats
extends|extends
name|NodeOperationResponse
block|{
DECL|field|os
specifier|private
name|OsStats
name|os
decl_stmt|;
DECL|field|process
specifier|private
name|ProcessStats
name|process
decl_stmt|;
DECL|field|jvm
specifier|private
name|JvmStats
name|jvm
decl_stmt|;
DECL|field|network
specifier|private
name|NetworkStats
name|network
decl_stmt|;
DECL|method|NodeStats
name|NodeStats
parameter_list|()
block|{     }
DECL|method|NodeStats
specifier|public
name|NodeStats
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|OsStats
name|os
parameter_list|,
name|ProcessStats
name|process
parameter_list|,
name|JvmStats
name|jvm
parameter_list|,
name|NetworkStats
name|network
parameter_list|)
block|{
name|super
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|this
operator|.
name|os
operator|=
name|os
expr_stmt|;
name|this
operator|.
name|process
operator|=
name|process
expr_stmt|;
name|this
operator|.
name|jvm
operator|=
name|jvm
expr_stmt|;
name|this
operator|.
name|network
operator|=
name|network
expr_stmt|;
block|}
comment|/**      * Operating System level statistics.      */
DECL|method|os
specifier|public
name|OsStats
name|os
parameter_list|()
block|{
return|return
name|this
operator|.
name|os
return|;
block|}
comment|/**      * Operating System level statistics.      */
DECL|method|getOs
specifier|public
name|OsStats
name|getOs
parameter_list|()
block|{
return|return
name|os
argument_list|()
return|;
block|}
comment|/**      * Process level statistics.      */
DECL|method|process
specifier|public
name|ProcessStats
name|process
parameter_list|()
block|{
return|return
name|process
return|;
block|}
comment|/**      * Process level statistics.      */
DECL|method|getProcess
specifier|public
name|ProcessStats
name|getProcess
parameter_list|()
block|{
return|return
name|process
argument_list|()
return|;
block|}
comment|/**      * JVM level statistics.      */
DECL|method|jvm
specifier|public
name|JvmStats
name|jvm
parameter_list|()
block|{
return|return
name|jvm
return|;
block|}
comment|/**      * JVM level statistics.      */
DECL|method|getJvm
specifier|public
name|JvmStats
name|getJvm
parameter_list|()
block|{
return|return
name|jvm
argument_list|()
return|;
block|}
comment|/**      * Network level statistics.      */
DECL|method|network
specifier|public
name|NetworkStats
name|network
parameter_list|()
block|{
return|return
name|network
return|;
block|}
comment|/**      * Network level statistics.      */
DECL|method|getNetwork
specifier|public
name|NetworkStats
name|getNetwork
parameter_list|()
block|{
return|return
name|network
argument_list|()
return|;
block|}
DECL|method|readNodeStats
specifier|public
specifier|static
name|NodeStats
name|readNodeStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|NodeStats
name|nodeInfo
init|=
operator|new
name|NodeStats
argument_list|()
decl_stmt|;
name|nodeInfo
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|nodeInfo
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|os
operator|=
name|OsStats
operator|.
name|readOsStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|process
operator|=
name|ProcessStats
operator|.
name|readProcessStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|jvm
operator|=
name|JvmStats
operator|.
name|readJvmStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|network
operator|=
name|NetworkStats
operator|.
name|readNetworkStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|os
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|process
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|process
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jvm
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|jvm
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|network
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|network
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

