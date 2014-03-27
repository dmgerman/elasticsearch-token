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
name|*
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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
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
name|predicates
operator|.
name|ObjectPredicate
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
name|procedures
operator|.
name|ObjectObjectProcedure
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
name|Map
import|;
end_import

begin_comment
comment|/**  * An immutable map implementation based on open hash map.  *<p/>  * Can be constructed using a {@link #builder()}, or using {@link #builder(ImmutableOpenMap)} (which is an optimized  * option to copy over existing content and modify it).  */
end_comment

begin_class
DECL|class|ImmutableOpenMap
specifier|public
specifier|final
class|class
name|ImmutableOpenMap
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
implements|implements
name|Iterable
argument_list|<
name|ObjectObjectCursor
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
argument_list|>
block|{
DECL|field|map
specifier|private
specifier|final
name|ObjectObjectOpenHashMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
decl_stmt|;
DECL|method|ImmutableOpenMap
specifier|private
name|ImmutableOpenMap
parameter_list|(
name|ObjectObjectOpenHashMap
argument_list|<
name|KType
argument_list|,
name|VType
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
comment|/**      * @return Returns the value associated with the given key or the default value      * for the key type, if the key is not associated with any value.      *<p/>      *<b>Important note:</b> For primitive type values, the value returned for a non-existing      * key may not be the default value of the primitive type (it may be any value previously      * assigned to that slot).      */
DECL|method|get
specifier|public
name|VType
name|get
parameter_list|(
name|KType
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
comment|/**      * @return Returns the value associated with the given key or the provided default value if the      * key is not associated with any value.      */
DECL|method|getOrDefault
specifier|public
name|VType
name|getOrDefault
parameter_list|(
name|KType
name|key
parameter_list|,
name|VType
name|defaultValue
parameter_list|)
block|{
return|return
name|map
operator|.
name|getOrDefault
argument_list|(
name|key
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
comment|/**      * Returns<code>true</code> if this container has an association to a value for      * the given key.      */
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|KType
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
comment|/**      * @return Returns the current size (number of assigned keys) in the container.      */
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
comment|/**      * @return Return<code>true</code> if this hash map contains no assigned keys.      */
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
comment|/**      * Returns a cursor over the entries (key-value pairs) in this map. The iterator is      * implemented as a cursor and it returns<b>the same cursor instance</b> on every      * call to {@link Iterator#next()}. To read the current key and value use the cursor's      * public fields. An example is shown below.      *<pre>      * for (IntShortCursor c : intShortMap)      * {      *     System.out.println(&quot;index=&quot; + c.index      *       +&quot; key=&quot; + c.key      *       +&quot; value=&quot; + c.value);      * }      *</pre>      *<p/>      *<p>The<code>index</code> field inside the cursor gives the internal index inside      * the container's implementation. The interpretation of this index depends on      * to the container.      */
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|ObjectObjectCursor
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|map
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**      * Returns a specialized view of the keys of this associated container.      * The view additionally implements {@link ObjectLookupContainer}.      */
DECL|method|keys
specifier|public
name|ObjectLookupContainer
argument_list|<
name|KType
argument_list|>
name|keys
parameter_list|()
block|{
return|return
name|map
operator|.
name|keys
argument_list|()
return|;
block|}
comment|/**      * Returns a direct iterator over the keys.      */
DECL|method|keysIt
specifier|public
name|UnmodifiableIterator
argument_list|<
name|KType
argument_list|>
name|keysIt
parameter_list|()
block|{
specifier|final
name|Iterator
argument_list|<
name|ObjectCursor
argument_list|<
name|KType
argument_list|>
argument_list|>
name|iterator
init|=
name|map
operator|.
name|keys
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|new
name|UnmodifiableIterator
argument_list|<
name|KType
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|KType
name|next
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|value
return|;
block|}
block|}
return|;
block|}
comment|/**      * @return Returns a container with all values stored in this map.      */
DECL|method|values
specifier|public
name|ObjectContainer
argument_list|<
name|VType
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
comment|/**      * Returns a direct iterator over the keys.      */
DECL|method|valuesIt
specifier|public
name|UnmodifiableIterator
argument_list|<
name|VType
argument_list|>
name|valuesIt
parameter_list|()
block|{
specifier|final
name|Iterator
argument_list|<
name|ObjectCursor
argument_list|<
name|VType
argument_list|>
argument_list|>
name|iterator
init|=
name|map
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|new
name|UnmodifiableIterator
argument_list|<
name|VType
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VType
name|next
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|value
return|;
block|}
block|}
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
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|ImmutableOpenMap
name|that
init|=
operator|(
name|ImmutableOpenMap
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|map
operator|.
name|equals
argument_list|(
name|that
operator|.
name|map
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|field|EMPTY
specifier|private
specifier|static
specifier|final
name|ImmutableOpenMap
name|EMPTY
init|=
operator|new
name|ImmutableOpenMap
argument_list|(
operator|new
name|ObjectObjectOpenHashMap
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|of
specifier|public
specifier|static
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
name|ImmutableOpenMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|of
parameter_list|()
block|{
return|return
name|EMPTY
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|builder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|()
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|builder
parameter_list|(
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|builder
parameter_list|(
name|ImmutableOpenMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|<>
argument_list|(
name|map
argument_list|)
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
parameter_list|<
name|KType
parameter_list|,
name|VType
parameter_list|>
implements|implements
name|ObjectObjectMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
block|{
DECL|field|map
specifier|private
name|ObjectObjectOpenHashMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
comment|//noinspection unchecked
name|this
argument_list|(
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|map
operator|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|ImmutableOpenMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
parameter_list|)
block|{
name|this
operator|.
name|map
operator|=
name|map
operator|.
name|map
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
comment|/**          * Builds a new instance of the          */
DECL|method|build
specifier|public
name|ImmutableOpenMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|build
parameter_list|()
block|{
name|ObjectObjectOpenHashMap
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
init|=
name|this
operator|.
name|map
decl_stmt|;
name|this
operator|.
name|map
operator|=
literal|null
expr_stmt|;
comment|// nullify the map, so any operation post build will fail! (hackish, but safest)
return|return
operator|new
name|ImmutableOpenMap
argument_list|<>
argument_list|(
name|map
argument_list|)
return|;
block|}
comment|/**          * Puts all the entries in the map to the builder.          */
DECL|method|putAll
specifier|public
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|putAll
parameter_list|(
name|Map
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|map
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|map
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**          * A put operation that can be used in the fluent pattern.          */
DECL|method|fPut
specifier|public
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|fPut
parameter_list|(
name|KType
name|key
parameter_list|,
name|VType
name|value
parameter_list|)
block|{
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
annotation|@
name|Override
DECL|method|put
specifier|public
name|VType
name|put
parameter_list|(
name|KType
name|key
parameter_list|,
name|VType
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
DECL|method|get
specifier|public
name|VType
name|get
parameter_list|(
name|KType
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
DECL|method|getOrDefault
specifier|public
name|VType
name|getOrDefault
parameter_list|(
name|KType
name|kType
parameter_list|,
name|VType
name|vType
parameter_list|)
block|{
return|return
name|map
operator|.
name|getOrDefault
argument_list|(
name|kType
argument_list|,
name|vType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|int
name|putAll
parameter_list|(
name|ObjectObjectAssociativeContainer
argument_list|<
name|?
extends|extends
name|KType
argument_list|,
name|?
extends|extends
name|VType
argument_list|>
name|container
parameter_list|)
block|{
return|return
name|map
operator|.
name|putAll
argument_list|(
name|container
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|putAll
specifier|public
name|int
name|putAll
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|ObjectObjectCursor
argument_list|<
name|?
extends|extends
name|KType
argument_list|,
name|?
extends|extends
name|VType
argument_list|>
argument_list|>
name|iterable
parameter_list|)
block|{
return|return
name|map
operator|.
name|putAll
argument_list|(
name|iterable
argument_list|)
return|;
block|}
comment|/**          * Remove that can be used in the fluent pattern.          */
DECL|method|fRemove
specifier|public
name|Builder
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
name|fRemove
parameter_list|(
name|KType
name|key
parameter_list|)
block|{
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
annotation|@
name|Override
DECL|method|remove
specifier|public
name|VType
name|remove
parameter_list|(
name|KType
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
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|ObjectObjectCursor
argument_list|<
name|KType
argument_list|,
name|VType
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|map
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|containsKey
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|KType
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
DECL|method|removeAll
specifier|public
name|int
name|removeAll
parameter_list|(
name|ObjectContainer
argument_list|<
name|?
extends|extends
name|KType
argument_list|>
name|container
parameter_list|)
block|{
return|return
name|map
operator|.
name|removeAll
argument_list|(
name|container
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|removeAll
specifier|public
name|int
name|removeAll
parameter_list|(
name|ObjectPredicate
argument_list|<
name|?
super|super
name|KType
argument_list|>
name|predicate
parameter_list|)
block|{
return|return
name|map
operator|.
name|removeAll
argument_list|(
name|predicate
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|forEach
specifier|public
parameter_list|<
name|T
extends|extends
name|ObjectObjectProcedure
argument_list|<
name|?
super|super
name|KType
argument_list|,
name|?
super|super
name|VType
argument_list|>
parameter_list|>
name|T
name|forEach
parameter_list|(
name|T
name|procedure
parameter_list|)
block|{
return|return
name|map
operator|.
name|forEach
argument_list|(
name|procedure
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
DECL|method|keys
specifier|public
name|ObjectCollection
argument_list|<
name|KType
argument_list|>
name|keys
parameter_list|()
block|{
return|return
name|map
operator|.
name|keys
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|values
specifier|public
name|ObjectContainer
argument_list|<
name|VType
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
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|cast
specifier|public
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|Builder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|cast
parameter_list|()
block|{
return|return
operator|(
name|Builder
operator|)
name|this
return|;
block|}
block|}
block|}
end_class

end_unit

