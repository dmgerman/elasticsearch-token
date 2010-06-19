begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.controller
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|controller
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FieldDoc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ShardFieldDoc
specifier|public
class|class
name|ShardFieldDoc
extends|extends
name|FieldDoc
implements|implements
name|ShardDoc
block|{
DECL|field|shardTarget
specifier|private
specifier|final
name|SearchShardTarget
name|shardTarget
decl_stmt|;
DECL|method|ShardFieldDoc
specifier|public
name|ShardFieldDoc
parameter_list|(
name|SearchShardTarget
name|shardTarget
parameter_list|,
name|int
name|doc
parameter_list|,
name|float
name|score
parameter_list|)
block|{
name|super
argument_list|(
name|doc
argument_list|,
name|score
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardTarget
operator|=
name|shardTarget
expr_stmt|;
block|}
DECL|method|ShardFieldDoc
specifier|public
name|ShardFieldDoc
parameter_list|(
name|SearchShardTarget
name|shardTarget
parameter_list|,
name|int
name|doc
parameter_list|,
name|float
name|score
parameter_list|,
name|Comparable
index|[]
name|fields
parameter_list|)
block|{
name|super
argument_list|(
name|doc
argument_list|,
name|score
argument_list|,
name|fields
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardTarget
operator|=
name|shardTarget
expr_stmt|;
block|}
DECL|method|shardTarget
annotation|@
name|Override
specifier|public
name|SearchShardTarget
name|shardTarget
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardTarget
return|;
block|}
DECL|method|docId
annotation|@
name|Override
specifier|public
name|int
name|docId
parameter_list|()
block|{
return|return
name|this
operator|.
name|doc
return|;
block|}
DECL|method|score
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
block|{
return|return
name|score
return|;
block|}
block|}
end_class

end_unit

