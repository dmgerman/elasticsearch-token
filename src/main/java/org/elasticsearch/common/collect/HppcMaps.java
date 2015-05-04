begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectLookupContainer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|HppcMaps
specifier|public
specifier|final
class|class
name|HppcMaps
block|{
DECL|method|HppcMaps
specifier|private
name|HppcMaps
parameter_list|()
block|{     }
comment|/**      * Returns a new map with the given initial capacity      */
DECL|method|newMap
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMap
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
return|return
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
argument_list|(
name|capacity
argument_list|)
return|;
block|}
comment|/**      * Returns a new map with a default initial capacity of      * {@value com.carrotsearch.hppc.HashContainerUtils#DEFAULT_CAPACITY}      */
DECL|method|newMap
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMap
parameter_list|()
block|{
return|return
name|newMap
argument_list|(
literal|16
argument_list|)
return|;
block|}
comment|/**      * Returns a map like {@link #newMap()} that does not accept<code>null</code> keys      */
DECL|method|newNoNullKeysMap
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newNoNullKeysMap
parameter_list|()
block|{
return|return
name|ensureNoNullKeys
argument_list|(
literal|16
argument_list|)
return|;
block|}
comment|/**      * Returns a map like {@link #newMap(int)} that does not accept<code>null</code> keys      */
DECL|method|newNoNullKeysMap
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newNoNullKeysMap
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
return|return
name|ensureNoNullKeys
argument_list|(
name|capacity
argument_list|)
return|;
block|}
comment|/**      * Wraps the given map and prevent adding of<code>null</code> keys.      */
DECL|method|ensureNoNullKeys
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|ensureNoNullKeys
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
return|return
operator|new
name|ObjectObjectOpenHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|capacity
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|V
name|put
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Map key must not be null"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/**      * @return an intersection view over the two specified containers (which can be KeyContainer or ObjectOpenHashSet).      */
comment|// Hppc has forEach, but this means we need to build an intermediate set, with this method we just iterate
comment|// over each unique value without creating a third set.
DECL|method|intersection
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Iterable
argument_list|<
name|T
argument_list|>
name|intersection
parameter_list|(
name|ObjectLookupContainer
argument_list|<
name|T
argument_list|>
name|container1
parameter_list|,
specifier|final
name|ObjectLookupContainer
argument_list|<
name|T
argument_list|>
name|container2
parameter_list|)
block|{
assert|assert
name|container1
operator|!=
literal|null
operator|&&
name|container2
operator|!=
literal|null
assert|;
specifier|final
name|Iterator
argument_list|<
name|ObjectCursor
argument_list|<
name|T
argument_list|>
argument_list|>
name|iterator
init|=
name|container1
operator|.
name|iterator
argument_list|()
decl_stmt|;
specifier|final
name|Iterator
argument_list|<
name|T
argument_list|>
name|intersection
init|=
operator|new
name|Iterator
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
name|T
name|current
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
do|do
block|{
name|T
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|container2
operator|.
name|contains
argument_list|(
name|next
argument_list|)
condition|)
block|{
name|current
operator|=
name|next
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
do|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
do|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|next
parameter_list|()
block|{
return|return
name|current
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
decl_stmt|;
return|return
operator|new
name|Iterable
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|T
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|intersection
return|;
block|}
block|}
return|;
block|}
DECL|class|Object
specifier|public
specifier|final
specifier|static
class|class
name|Object
block|{
DECL|class|Integer
specifier|public
specifier|final
specifier|static
class|class
name|Integer
block|{
DECL|method|ensureNoNullKeys
specifier|public
specifier|static
parameter_list|<
name|V
parameter_list|>
name|ObjectIntOpenHashMap
argument_list|<
name|V
argument_list|>
name|ensureNoNullKeys
parameter_list|(
name|int
name|capacity
parameter_list|,
name|float
name|loadFactor
parameter_list|)
block|{
return|return
operator|new
name|ObjectIntOpenHashMap
argument_list|<
name|V
argument_list|>
argument_list|(
name|capacity
argument_list|,
name|loadFactor
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|int
name|put
parameter_list|(
name|V
name|key
parameter_list|,
name|int
name|value
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Map key must not be null"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

