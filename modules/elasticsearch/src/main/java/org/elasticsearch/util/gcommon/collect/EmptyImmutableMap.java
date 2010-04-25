begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.gcommon.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
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
name|gcommon
operator|.
name|annotations
operator|.
name|GwtCompatible
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * An empty immutable map.  *   * @author Jesse Wilson  * @author Kevin Bourrillion  */
end_comment

begin_class
annotation|@
name|GwtCompatible
argument_list|(
name|serializable
operator|=
literal|true
argument_list|)
DECL|class|EmptyImmutableMap
specifier|final
class|class
name|EmptyImmutableMap
extends|extends
name|ImmutableMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
block|{
DECL|field|INSTANCE
specifier|static
specifier|final
name|EmptyImmutableMap
name|INSTANCE
init|=
operator|new
name|EmptyImmutableMap
argument_list|()
decl_stmt|;
DECL|method|EmptyImmutableMap
specifier|private
name|EmptyImmutableMap
parameter_list|()
block|{}
DECL|method|get
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
DECL|method|isEmpty
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|containsKey
annotation|@
name|Override
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|containsValue
annotation|@
name|Override
specifier|public
name|boolean
name|containsValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|entrySet
annotation|@
name|Override
specifier|public
name|ImmutableSet
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
return|return
name|ImmutableSet
operator|.
name|of
argument_list|()
return|;
block|}
DECL|method|keySet
annotation|@
name|Override
specifier|public
name|ImmutableSet
argument_list|<
name|Object
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
name|ImmutableSet
operator|.
name|of
argument_list|()
return|;
block|}
DECL|method|values
annotation|@
name|Override
specifier|public
name|ImmutableCollection
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|ImmutableCollection
operator|.
name|EMPTY_IMMUTABLE_COLLECTION
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
annotation|@
name|Nullable
name|Object
name|object
parameter_list|)
block|{
if|if
condition|(
name|object
operator|instanceof
name|Map
condition|)
block|{
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|that
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|object
decl_stmt|;
return|return
name|that
operator|.
name|isEmpty
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|0
return|;
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
literal|"{}"
return|;
block|}
DECL|method|readResolve
name|Object
name|readResolve
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
comment|// preserve singleton property
block|}
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|0
decl_stmt|;
block|}
end_class

end_unit

