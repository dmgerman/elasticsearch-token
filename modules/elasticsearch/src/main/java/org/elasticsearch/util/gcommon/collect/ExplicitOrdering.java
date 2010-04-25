begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/** An ordering that compares objects according to a given order. */
end_comment

begin_class
annotation|@
name|GwtCompatible
argument_list|(
name|serializable
operator|=
literal|true
argument_list|)
DECL|class|ExplicitOrdering
specifier|final
class|class
name|ExplicitOrdering
parameter_list|<
name|T
parameter_list|>
extends|extends
name|Ordering
argument_list|<
name|T
argument_list|>
implements|implements
name|Serializable
block|{
DECL|field|rankMap
specifier|final
name|ImmutableMap
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|rankMap
decl_stmt|;
DECL|method|ExplicitOrdering
name|ExplicitOrdering
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|valuesInOrder
parameter_list|)
block|{
name|this
argument_list|(
name|buildRankMap
argument_list|(
name|valuesInOrder
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|ExplicitOrdering
name|ExplicitOrdering
parameter_list|(
name|ImmutableMap
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|rankMap
parameter_list|)
block|{
name|this
operator|.
name|rankMap
operator|=
name|rankMap
expr_stmt|;
block|}
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|T
name|left
parameter_list|,
name|T
name|right
parameter_list|)
block|{
return|return
name|rank
argument_list|(
name|left
argument_list|)
operator|-
name|rank
argument_list|(
name|right
argument_list|)
return|;
comment|// safe because both are nonnegative
block|}
DECL|method|rank
specifier|private
name|int
name|rank
parameter_list|(
name|T
name|value
parameter_list|)
block|{
name|Integer
name|rank
init|=
name|rankMap
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|rank
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IncomparableValueException
argument_list|(
name|value
argument_list|)
throw|;
block|}
return|return
name|rank
return|;
block|}
DECL|method|buildRankMap
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|ImmutableMap
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|buildRankMap
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|valuesInOrder
parameter_list|)
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|int
name|rank
init|=
literal|0
decl_stmt|;
for|for
control|(
name|T
name|value
range|:
name|valuesInOrder
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|value
argument_list|,
name|rank
operator|++
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
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
name|ExplicitOrdering
condition|)
block|{
name|ExplicitOrdering
argument_list|<
name|?
argument_list|>
name|that
init|=
operator|(
name|ExplicitOrdering
argument_list|<
name|?
argument_list|>
operator|)
name|object
decl_stmt|;
return|return
name|this
operator|.
name|rankMap
operator|.
name|equals
argument_list|(
name|that
operator|.
name|rankMap
argument_list|)
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
name|rankMap
operator|.
name|hashCode
argument_list|()
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
literal|"Ordering.explicit("
operator|+
name|rankMap
operator|.
name|keySet
argument_list|()
operator|+
literal|")"
return|;
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

