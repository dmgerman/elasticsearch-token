begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.gnu.trove.decorator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gnu
operator|.
name|trove
operator|.
name|decorator
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
name|gnu
operator|.
name|trove
operator|.
name|TShortIntHashMap
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
name|gnu
operator|.
name|trove
operator|.
name|TShortIntIterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Externalizable
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
name|ObjectInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|//////////////////////////////////////////////////
end_comment

begin_comment
comment|// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
end_comment

begin_comment
comment|//////////////////////////////////////////////////
end_comment

begin_comment
comment|/**  * Wrapper class to make a TShortIntHashMap conform to the<tt>java.util.Map</tt> API.  * This class simply decorates an underlying TShortIntHashMap and translates the Object-based  * APIs into their Trove primitive analogs.  *<p/>  *<p/>  * Note that wrapping and unwrapping primitive values is extremely inefficient.  If  * possible, users of this class should override the appropriate methods in this class  * and use a table of canonical values.  *</p>  *<p/>  * Created: Mon Sep 23 22:07:40 PDT 2002  *  * @author Eric D. Friedman  * @author Rob Eden  */
end_comment

begin_class
DECL|class|TShortIntHashMapDecorator
specifier|public
class|class
name|TShortIntHashMapDecorator
extends|extends
name|AbstractMap
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
implements|implements
name|Map
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
implements|,
name|Externalizable
implements|,
name|Cloneable
block|{
comment|/**      * the wrapped primitive map      */
DECL|field|_map
specifier|protected
name|TShortIntHashMap
name|_map
decl_stmt|;
comment|/**      * FOR EXTERNALIZATION ONLY!!      */
DECL|method|TShortIntHashMapDecorator
specifier|public
name|TShortIntHashMapDecorator
parameter_list|()
block|{     }
comment|/**      * Creates a wrapper that decorates the specified primitive map.      */
DECL|method|TShortIntHashMapDecorator
specifier|public
name|TShortIntHashMapDecorator
parameter_list|(
name|TShortIntHashMap
name|map
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|_map
operator|=
name|map
expr_stmt|;
block|}
comment|/**      * Returns a reference to the map wrapped by this decorator.      */
DECL|method|getMap
specifier|public
name|TShortIntHashMap
name|getMap
parameter_list|()
block|{
return|return
name|_map
return|;
block|}
comment|/**      * Clones the underlying trove collection and returns the clone wrapped in a new      * decorator instance.  This is a shallow clone except where primitives are      * concerned.      *      * @return a copy of the receiver      */
DECL|method|clone
specifier|public
name|TShortIntHashMapDecorator
name|clone
parameter_list|()
block|{
try|try
block|{
name|TShortIntHashMapDecorator
name|copy
init|=
operator|(
name|TShortIntHashMapDecorator
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|copy
operator|.
name|_map
operator|=
operator|(
name|TShortIntHashMap
operator|)
name|_map
operator|.
name|clone
argument_list|()
expr_stmt|;
return|return
name|copy
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
comment|// assert(false);
throw|throw
operator|new
name|InternalError
argument_list|()
throw|;
comment|// we are cloneable, so this does not happen
block|}
block|}
comment|/**      * Inserts a key/value pair into the map.      *      * @param key   an<code>Object</code> value      * @param value an<code>Object</code> value      * @return the previous value associated with<tt>key</tt>,      *         or Integer(0) if none was found.      */
DECL|method|put
specifier|public
name|Integer
name|put
parameter_list|(
name|Short
name|key
parameter_list|,
name|Integer
name|value
parameter_list|)
block|{
return|return
name|wrapValue
argument_list|(
name|_map
operator|.
name|put
argument_list|(
name|unwrapKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|unwrapValue
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Retrieves the value for<tt>key</tt>      *      * @param key an<code>Object</code> value      * @return the value of<tt>key</tt> or null if no such mapping exists.      */
DECL|method|get
specifier|public
name|Integer
name|get
parameter_list|(
name|Short
name|key
parameter_list|)
block|{
name|short
name|k
init|=
name|unwrapKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|v
init|=
name|_map
operator|.
name|get
argument_list|(
name|k
argument_list|)
decl_stmt|;
comment|// 0 may be a false positive since primitive maps
comment|// cannot return null, so we have to do an extra
comment|// check here.
if|if
condition|(
name|v
operator|==
literal|0
condition|)
block|{
return|return
name|_map
operator|.
name|containsKey
argument_list|(
name|k
argument_list|)
condition|?
name|wrapValue
argument_list|(
name|v
argument_list|)
else|:
literal|null
return|;
block|}
else|else
block|{
return|return
name|wrapValue
argument_list|(
name|v
argument_list|)
return|;
block|}
block|}
comment|/**      * Empties the map.      */
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|_map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**      * Deletes a key/value pair from the map.      *      * @param key an<code>Object</code> value      * @return the removed value, or Integer(0) if it was not found in the map      */
DECL|method|remove
specifier|public
name|Integer
name|remove
parameter_list|(
name|Short
name|key
parameter_list|)
block|{
return|return
name|wrapValue
argument_list|(
name|_map
operator|.
name|remove
argument_list|(
name|unwrapKey
argument_list|(
name|key
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a Set view on the entries of the map.      *      * @return a<code>Set</code> value      */
DECL|method|entrySet
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
operator|new
name|AbstractSet
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|_map
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|TShortIntHashMapDecorator
operator|.
name|this
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|Map
operator|.
name|Entry
condition|)
block|{
name|Object
name|k
init|=
operator|(
operator|(
name|Map
operator|.
name|Entry
operator|)
name|o
operator|)
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|v
init|=
operator|(
operator|(
name|Map
operator|.
name|Entry
operator|)
name|o
operator|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
return|return
name|TShortIntHashMapDecorator
operator|.
name|this
operator|.
name|containsKey
argument_list|(
name|k
argument_list|)
operator|&&
name|TShortIntHashMapDecorator
operator|.
name|this
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|equals
argument_list|(
name|v
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|private
specifier|final
name|TShortIntIterator
name|it
init|=
name|_map
operator|.
name|iterator
argument_list|()
decl_stmt|;
specifier|public
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
name|next
parameter_list|()
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
specifier|final
name|Short
name|key
init|=
name|wrapKey
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Integer
name|v
init|=
name|wrapValue
argument_list|(
name|it
operator|.
name|value
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|()
block|{
specifier|private
name|Integer
name|val
init|=
name|v
decl_stmt|;
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|instanceof
name|Map
operator|.
name|Entry
operator|&&
operator|(
operator|(
name|Map
operator|.
name|Entry
operator|)
name|o
operator|)
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|key
argument_list|)
operator|&&
operator|(
operator|(
name|Map
operator|.
name|Entry
operator|)
name|o
operator|)
operator|.
name|getValue
argument_list|()
operator|.
name|equals
argument_list|(
name|val
argument_list|)
return|;
block|}
specifier|public
name|Short
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
specifier|public
name|Integer
name|getValue
parameter_list|()
block|{
return|return
name|val
return|;
block|}
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|key
operator|.
name|hashCode
argument_list|()
operator|+
name|val
operator|.
name|hashCode
argument_list|()
return|;
block|}
specifier|public
name|Integer
name|setValue
parameter_list|(
name|Integer
name|value
parameter_list|)
block|{
name|val
operator|=
name|value
expr_stmt|;
return|return
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
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|it
operator|.
name|hasNext
argument_list|()
return|;
block|}
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|public
name|boolean
name|add
parameter_list|(
name|Integer
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|boolean
name|remove
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|boolean
name|addAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Map
operator|.
name|Entry
argument_list|<
name|Short
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|boolean
name|retainAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|boolean
name|removeAll
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|TShortIntHashMapDecorator
operator|.
name|this
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**      * Checks for the presence of<tt>val</tt> in the values of the map.      *      * @param val an<code>Object</code> value      * @return a<code>boolean</code> value      */
DECL|method|containsValue
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|val
parameter_list|)
block|{
return|return
name|_map
operator|.
name|containsValue
argument_list|(
name|unwrapValue
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Checks for the present of<tt>key</tt> in the keys of the map.      *      * @param key an<code>Object</code> value      * @return a<code>boolean</code> value      */
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
name|_map
operator|.
name|containsKey
argument_list|(
name|unwrapKey
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns the number of entries in the map.      *      * @return the map's size.      */
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|this
operator|.
name|_map
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * Indicates whether map has any entries.      *      * @return true if the map is empty      */
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|size
argument_list|()
operator|==
literal|0
return|;
block|}
comment|/**      * Copies the key/value mappings in<tt>map</tt> into this map.      * Note that this will be a<b>deep</b> copy, as storage is by      * primitive value.      *      * @param map a<code>Map</code> value      */
DECL|method|putAll
specifier|public
name|void
name|putAll
parameter_list|(
name|Map
argument_list|<
name|?
extends|extends
name|Short
argument_list|,
name|?
extends|extends
name|Integer
argument_list|>
name|map
parameter_list|)
block|{
name|Iterator
argument_list|<
name|?
extends|extends
name|Entry
argument_list|<
name|?
extends|extends
name|Short
argument_list|,
name|?
extends|extends
name|Integer
argument_list|>
argument_list|>
name|it
init|=
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|map
operator|.
name|size
argument_list|()
init|;
name|i
operator|--
operator|>
literal|0
condition|;
control|)
block|{
name|Entry
argument_list|<
name|?
extends|extends
name|Short
argument_list|,
name|?
extends|extends
name|Integer
argument_list|>
name|e
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|this
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Wraps a key      *      * @param k key in the underlying map      * @return an Object representation of the key      */
DECL|method|wrapKey
specifier|protected
name|Short
name|wrapKey
parameter_list|(
name|short
name|k
parameter_list|)
block|{
return|return
name|Short
operator|.
name|valueOf
argument_list|(
name|k
argument_list|)
return|;
block|}
comment|/**      * Unwraps a key      *      * @param key wrapped key      * @return an unwrapped representation of the key      */
DECL|method|unwrapKey
specifier|protected
name|short
name|unwrapKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
operator|(
operator|(
name|Short
operator|)
name|key
operator|)
operator|.
name|shortValue
argument_list|()
return|;
block|}
comment|/**      * Wraps a value      *      * @param k value in the underlying map      * @return an Object representation of the value      */
DECL|method|wrapValue
specifier|protected
name|Integer
name|wrapValue
parameter_list|(
name|int
name|k
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|k
argument_list|)
return|;
block|}
comment|/**      * Unwraps a value      *      * @param value wrapped value      * @return an unwrapped representation of the value      */
DECL|method|unwrapValue
specifier|protected
name|int
name|unwrapValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
operator|(
operator|(
name|Integer
operator|)
name|value
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
comment|// Implements Externalizable
DECL|method|readExternal
specifier|public
name|void
name|readExternal
parameter_list|(
name|ObjectInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
comment|// VERSION
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
comment|// MAP
name|_map
operator|=
operator|(
name|TShortIntHashMap
operator|)
name|in
operator|.
name|readObject
argument_list|()
expr_stmt|;
block|}
comment|// Implements Externalizable
DECL|method|writeExternal
specifier|public
name|void
name|writeExternal
parameter_list|(
name|ObjectOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// VERSION
name|out
operator|.
name|writeByte
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// MAP
name|out
operator|.
name|writeObject
argument_list|(
name|_map
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_comment
comment|// TShortIntHashMapDecorator
end_comment

end_unit

