begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.mustache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|mustache
package|;
end_package

begin_import
import|import
name|com
operator|.
name|github
operator|.
name|mustachejava
operator|.
name|reflect
operator|.
name|ReflectionObjectHandler
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
name|util
operator|.
name|iterable
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
import|;
end_import

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
name|Set
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_class
DECL|class|CustomReflectionObjectHandler
specifier|final
class|class
name|CustomReflectionObjectHandler
extends|extends
name|ReflectionObjectHandler
block|{
annotation|@
name|Override
DECL|method|coerce
specifier|public
name|Object
name|coerce
parameter_list|(
name|Object
name|object
parameter_list|)
block|{
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|object
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
condition|)
block|{
return|return
operator|new
name|ArrayMap
argument_list|(
name|object
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|object
operator|instanceof
name|Collection
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Collection
argument_list|<
name|Object
argument_list|>
name|collection
init|=
operator|(
name|Collection
argument_list|<
name|Object
argument_list|>
operator|)
name|object
decl_stmt|;
return|return
operator|new
name|CollectionMap
argument_list|(
name|collection
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|coerce
argument_list|(
name|object
argument_list|)
return|;
block|}
block|}
DECL|class|ArrayMap
specifier|static
specifier|final
class|class
name|ArrayMap
extends|extends
name|AbstractMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
implements|implements
name|Iterable
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|array
specifier|private
specifier|final
name|Object
name|array
decl_stmt|;
DECL|field|length
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
DECL|method|ArrayMap
specifier|public
name|ArrayMap
parameter_list|(
name|Object
name|array
parameter_list|)
block|{
name|this
operator|.
name|array
operator|=
name|array
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|Array
operator|.
name|getLength
argument_list|(
name|array
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
literal|"size"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|key
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Array
operator|.
name|get
argument_list|(
name|array
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|key
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
return|;
block|}
try|try
block|{
name|int
name|index
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Array
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|index
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
comment|// if it's not a number it is as if the key doesn't exist
return|return
literal|null
return|;
block|}
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
name|get
argument_list|(
name|key
argument_list|)
operator|!=
literal|null
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
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|Array
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|index
operator|<
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|next
parameter_list|()
block|{
return|return
name|Array
operator|.
name|get
argument_list|(
name|array
argument_list|,
name|index
operator|++
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
DECL|class|CollectionMap
specifier|static
specifier|final
class|class
name|CollectionMap
extends|extends
name|AbstractMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
implements|implements
name|Iterable
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|col
specifier|private
specifier|final
name|Collection
argument_list|<
name|Object
argument_list|>
name|col
decl_stmt|;
DECL|method|CollectionMap
specifier|public
name|CollectionMap
parameter_list|(
name|Collection
argument_list|<
name|Object
argument_list|>
name|col
parameter_list|)
block|{
name|this
operator|.
name|col
operator|=
name|col
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
if|if
condition|(
literal|"size"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|col
operator|.
name|size
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|key
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Iterables
operator|.
name|get
argument_list|(
name|col
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|key
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
return|;
block|}
try|try
block|{
name|int
name|index
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Iterables
operator|.
name|get
argument_list|(
name|col
argument_list|,
name|index
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
comment|// if it's not a number it is as if the key doesn't exist
return|return
literal|null
return|;
block|}
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
name|get
argument_list|(
name|key
argument_list|)
operator|!=
literal|null
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
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|col
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Object
name|item
range|:
name|col
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|i
operator|++
argument_list|,
name|item
argument_list|)
expr_stmt|;
block|}
return|return
name|map
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|col
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

