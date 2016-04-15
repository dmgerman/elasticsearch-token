begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.command
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|command
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
name|ParseField
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
name|ParseFieldMatcherSupplier
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
name|common
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ObjectParser
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
name|xcontent
operator|.
name|XContentBuilder
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
comment|/**  * Abstract base class for allocating an unassigned primary shard to a node  */
end_comment

begin_class
DECL|class|BasePrimaryAllocationCommand
specifier|public
specifier|abstract
class|class
name|BasePrimaryAllocationCommand
extends|extends
name|AbstractAllocateAllocationCommand
block|{
DECL|field|ACCEPT_DATA_LOSS_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|ACCEPT_DATA_LOSS_FIELD
init|=
literal|"accept_data_loss"
decl_stmt|;
DECL|method|createAllocatePrimaryParser
specifier|protected
specifier|static
parameter_list|<
name|T
extends|extends
name|Builder
argument_list|<
name|?
argument_list|>
parameter_list|>
name|ObjectParser
argument_list|<
name|T
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|createAllocatePrimaryParser
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|ObjectParser
argument_list|<
name|T
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|parser
init|=
name|AbstractAllocateAllocationCommand
operator|.
name|createAllocateParser
argument_list|(
name|command
argument_list|)
decl_stmt|;
name|parser
operator|.
name|declareBoolean
argument_list|(
name|Builder
operator|::
name|setAcceptDataLoss
argument_list|,
operator|new
name|ParseField
argument_list|(
name|ACCEPT_DATA_LOSS_FIELD
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|parser
return|;
block|}
DECL|field|acceptDataLoss
specifier|protected
specifier|final
name|boolean
name|acceptDataLoss
decl_stmt|;
DECL|method|BasePrimaryAllocationCommand
specifier|protected
name|BasePrimaryAllocationCommand
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|String
name|node
parameter_list|,
name|boolean
name|acceptDataLoss
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|this
operator|.
name|acceptDataLoss
operator|=
name|acceptDataLoss
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|BasePrimaryAllocationCommand
specifier|protected
name|BasePrimaryAllocationCommand
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|acceptDataLoss
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|acceptDataLoss
argument_list|)
expr_stmt|;
block|}
comment|/**      * The operation only executes if the user explicitly agrees to possible data loss      *      * @return whether data loss is acceptable      */
DECL|method|acceptDataLoss
specifier|public
name|boolean
name|acceptDataLoss
parameter_list|()
block|{
return|return
name|acceptDataLoss
return|;
block|}
DECL|class|Builder
specifier|protected
specifier|static
specifier|abstract
class|class
name|Builder
parameter_list|<
name|T
extends|extends
name|BasePrimaryAllocationCommand
parameter_list|>
extends|extends
name|AbstractAllocateAllocationCommand
operator|.
name|Builder
argument_list|<
name|T
argument_list|>
block|{
DECL|field|acceptDataLoss
specifier|protected
name|boolean
name|acceptDataLoss
decl_stmt|;
DECL|method|setAcceptDataLoss
specifier|public
name|void
name|setAcceptDataLoss
parameter_list|(
name|boolean
name|acceptDataLoss
parameter_list|)
block|{
name|this
operator|.
name|acceptDataLoss
operator|=
name|acceptDataLoss
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|extraXContent
specifier|protected
name|void
name|extraXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|ACCEPT_DATA_LOSS_FIELD
argument_list|,
name|acceptDataLoss
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

