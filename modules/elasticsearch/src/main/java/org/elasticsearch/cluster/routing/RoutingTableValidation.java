begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Lists
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
name|Streamable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|io
operator|.
name|Serializable
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RoutingTableValidation
specifier|public
class|class
name|RoutingTableValidation
implements|implements
name|Serializable
implements|,
name|Streamable
block|{
DECL|field|valid
specifier|private
name|boolean
name|valid
init|=
literal|true
decl_stmt|;
DECL|field|failures
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|failures
decl_stmt|;
DECL|field|indicesFailures
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|indicesFailures
decl_stmt|;
DECL|method|RoutingTableValidation
specifier|public
name|RoutingTableValidation
parameter_list|()
block|{     }
DECL|method|valid
specifier|public
name|boolean
name|valid
parameter_list|()
block|{
return|return
name|valid
return|;
block|}
DECL|method|allFailures
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|allFailures
parameter_list|()
block|{
if|if
condition|(
name|failures
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|&&
name|indicesFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|allFailures
init|=
name|newArrayList
argument_list|(
name|failures
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|indicesFailures
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|failure
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|allFailures
operator|.
name|add
argument_list|(
literal|"Index ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"]: "
operator|+
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|allFailures
return|;
block|}
DECL|method|failures
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|failures
parameter_list|()
block|{
if|if
condition|(
name|failures
operator|==
literal|null
condition|)
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
return|return
name|failures
return|;
block|}
DECL|method|indicesFailures
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|indicesFailures
parameter_list|()
block|{
if|if
condition|(
name|indicesFailures
operator|==
literal|null
condition|)
block|{
return|return
name|ImmutableMap
operator|.
name|of
argument_list|()
return|;
block|}
return|return
name|indicesFailures
return|;
block|}
DECL|method|indexFailures
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|indexFailures
parameter_list|(
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
name|indicesFailures
operator|==
literal|null
condition|)
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|indexFailures
init|=
name|indicesFailures
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexFailures
operator|==
literal|null
condition|)
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
return|return
name|indexFailures
return|;
block|}
DECL|method|addFailure
specifier|public
name|void
name|addFailure
parameter_list|(
name|String
name|failure
parameter_list|)
block|{
name|valid
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|failures
operator|==
literal|null
condition|)
block|{
name|failures
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|failures
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
DECL|method|addIndexFailure
specifier|public
name|void
name|addIndexFailure
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|failure
parameter_list|)
block|{
name|valid
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|indicesFailures
operator|==
literal|null
condition|)
block|{
name|indicesFailures
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|indexFailures
init|=
name|indicesFailures
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexFailures
operator|==
literal|null
condition|)
block|{
name|indexFailures
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|indicesFailures
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexFailures
argument_list|)
expr_stmt|;
block|}
name|indexFailures
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|allFailures
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|valid
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|failures
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|failures
operator|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|size
argument_list|)
expr_stmt|;
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
name|failures
operator|.
name|add
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|size
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|indicesFailures
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|indicesFailures
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
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
name|String
name|index
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|int
name|size2
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|indexFailures
init|=
name|newArrayListWithCapacity
argument_list|(
name|size2
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size2
condition|;
name|j
operator|++
control|)
block|{
name|indexFailures
operator|.
name|add
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indicesFailures
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexFailures
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|valid
argument_list|)
expr_stmt|;
if|if
condition|(
name|failures
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|failures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|failure
range|:
name|failures
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesFailures
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|indicesFailures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|indicesFailures
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|failure
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

