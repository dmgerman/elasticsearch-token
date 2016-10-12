begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_class
DECL|class|ConcurrentHashMapLong
specifier|public
class|class
name|ConcurrentHashMapLong
parameter_list|<
name|T
parameter_list|>
implements|implements
name|ConcurrentMapLong
argument_list|<
name|T
argument_list|>
block|{
DECL|field|map
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|Long
argument_list|,
name|T
argument_list|>
name|map
decl_stmt|;
DECL|method|ConcurrentHashMapLong
specifier|public
name|ConcurrentHashMapLong
parameter_list|(
name|ConcurrentMap
argument_list|<
name|Long
argument_list|,
name|T
argument_list|>
name|map
parameter_list|)
block|{
name|this
operator|.
name|map
operator|=
name|map
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|T
name|get
parameter_list|(
name|long
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
annotation|@
name|Override
DECL|method|remove
specifier|public
name|T
name|remove
parameter_list|(
name|long
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|T
name|put
parameter_list|(
name|long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|putIfAbsent
specifier|public
name|T
name|putIfAbsent
parameter_list|(
name|long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|// MAP DELEGATION
annotation|@
name|Override
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|map
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|map
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|T
name|get
parameter_list|(
name|Object
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
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
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
annotation|@
name|Override
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|containsValue
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|put
specifier|public
name|T
name|put
parameter_list|(
name|Long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|putIfAbsent
specifier|public
name|T
name|putIfAbsent
parameter_list|(
name|Long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
argument_list|<
name|?
extends|extends
name|Long
argument_list|,
name|?
extends|extends
name|T
argument_list|>
name|m
parameter_list|)
block|{
name|map
operator|.
name|putAll
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|T
name|remove
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|remove
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|replace
specifier|public
name|boolean
name|replace
parameter_list|(
name|Long
name|key
parameter_list|,
name|T
name|oldValue
parameter_list|,
name|T
name|newValue
parameter_list|)
block|{
return|return
name|map
operator|.
name|replace
argument_list|(
name|key
argument_list|,
name|oldValue
argument_list|,
name|newValue
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|replace
specifier|public
name|T
name|replace
parameter_list|(
name|Long
name|key
parameter_list|,
name|T
name|value
parameter_list|)
block|{
return|return
name|map
operator|.
name|replace
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|keySet
specifier|public
name|Set
argument_list|<
name|Long
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|keySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|map
operator|.
name|values
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|entrySet
specifier|public
name|Set
argument_list|<
name|Entry
argument_list|<
name|Long
argument_list|,
name|T
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|map
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|map
operator|.
name|equals
argument_list|(
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|map
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|map
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

