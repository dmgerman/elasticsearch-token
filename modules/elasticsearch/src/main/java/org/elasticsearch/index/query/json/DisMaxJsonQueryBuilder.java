begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|JsonBuilder
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
name|ArrayList
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DisMaxJsonQueryBuilder
specifier|public
class|class
name|DisMaxJsonQueryBuilder
extends|extends
name|BaseJsonQueryBuilder
block|{
DECL|field|queries
specifier|private
name|ArrayList
argument_list|<
name|JsonQueryBuilder
argument_list|>
name|queries
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|tieBreakerMultiplier
specifier|private
name|float
name|tieBreakerMultiplier
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|add
specifier|public
name|DisMaxJsonQueryBuilder
name|add
parameter_list|(
name|JsonQueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|queries
operator|.
name|add
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boost
specifier|public
name|DisMaxJsonQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|tieBreakerMultiplier
specifier|public
name|DisMaxJsonQueryBuilder
name|tieBreakerMultiplier
parameter_list|(
name|float
name|tieBreakerMultiplier
parameter_list|)
block|{
name|this
operator|.
name|tieBreakerMultiplier
operator|=
name|tieBreakerMultiplier
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doJson
annotation|@
name|Override
specifier|protected
name|void
name|doJson
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"disMax"
argument_list|)
expr_stmt|;
if|if
condition|(
name|tieBreakerMultiplier
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"tieBreakerMultiplier"
argument_list|,
name|tieBreakerMultiplier
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startArray
argument_list|(
literal|"queries"
argument_list|)
expr_stmt|;
for|for
control|(
name|JsonQueryBuilder
name|queryBuilder
range|:
name|queries
control|)
block|{
name|queryBuilder
operator|.
name|toJson
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

