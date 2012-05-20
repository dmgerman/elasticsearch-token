begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
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
name|ImmutableMap
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
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MapBuilder
specifier|public
class|class
name|MapBuilder
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
DECL|method|newMapBuilder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBuilder
parameter_list|()
block|{
return|return
operator|new
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|()
return|;
block|}
DECL|method|newMapBuilder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBuilder
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
parameter_list|)
block|{
return|return
operator|new
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|map
argument_list|)
return|;
block|}
DECL|field|map
specifier|private
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|MapBuilder
specifier|public
name|MapBuilder
parameter_list|()
block|{
name|this
operator|.
name|map
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
block|}
DECL|method|MapBuilder
specifier|public
name|MapBuilder
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
parameter_list|)
block|{
name|this
operator|.
name|map
operator|=
name|newHashMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
DECL|method|putAll
specifier|public
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|putAll
parameter_list|(
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
parameter_list|)
block|{
name|this
operator|.
name|map
operator|.
name|putAll
argument_list|(
name|map
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|put
specifier|public
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|put
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|this
operator|.
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|remove
specifier|public
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|remove
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|this
operator|.
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|clear
specifier|public
name|MapBuilder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|clear
parameter_list|()
block|{
name|this
operator|.
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|get
specifier|public
name|V
name|get
parameter_list|(
name|K
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|K
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
DECL|method|map
specifier|public
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
parameter_list|()
block|{
return|return
name|this
operator|.
name|map
return|;
block|}
DECL|method|immutableMap
specifier|public
name|ImmutableMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|immutableMap
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|map
argument_list|)
return|;
block|}
block|}
end_class

end_unit

