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
name|GwtIncompatible
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
name|List
import|;
end_import

begin_comment
comment|/**  * Methods factored out so that they can be emulated differently in GWT.  *  * @author Hayward Chan  */
end_comment

begin_class
annotation|@
name|GwtCompatible
argument_list|(
name|emulated
operator|=
literal|true
argument_list|)
DECL|class|Platform
class|class
name|Platform
block|{
comment|/**    * Calls {@link List#subList(int, int)}.  Factored out so that it can be    * emulated in GWT.    *    *<p>This method is not supported in GWT yet.  See<a    * href="http://code.google.com/p/google-web-toolkit/issues/detail?id=1791">    * GWT issue 1791</a>    */
annotation|@
name|GwtIncompatible
argument_list|(
literal|"List.subList"
argument_list|)
DECL|method|subList
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|subList
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|list
parameter_list|,
name|int
name|fromIndex
parameter_list|,
name|int
name|toIndex
parameter_list|)
block|{
return|return
name|list
operator|.
name|subList
argument_list|(
name|fromIndex
argument_list|,
name|toIndex
argument_list|)
return|;
block|}
comment|/**    * Calls {@link Class#isInstance(Object)}.  Factored out so that it can be    * emulated in GWT.    */
annotation|@
name|GwtIncompatible
argument_list|(
literal|"Class.isInstance"
argument_list|)
DECL|method|isInstance
specifier|static
name|boolean
name|isInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|Object
name|obj
parameter_list|)
block|{
return|return
name|clazz
operator|.
name|isInstance
argument_list|(
name|obj
argument_list|)
return|;
block|}
comment|/**    * Clone the given array using {@link Object#clone()}.  It is factored out so    * that it can be emulated in GWT.    */
DECL|method|clone
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|clone
parameter_list|(
name|T
index|[]
name|array
parameter_list|)
block|{
return|return
name|array
operator|.
name|clone
argument_list|()
return|;
block|}
comment|/**    * Returns a new array of the given length with the specified component type.    *    * @param type the component type    * @param length the length of the new array    */
annotation|@
name|GwtIncompatible
argument_list|(
literal|"Array.newInstance(Class, int)"
argument_list|)
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|newArray
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|newArray
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
operator|(
name|T
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|type
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|/**    * Returns a new array of the given length with the same type as a reference    * array.    *    * @param reference any array of the desired type    * @param length the length of the new array    */
DECL|method|newArray
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|newArray
parameter_list|(
name|T
index|[]
name|reference
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|type
init|=
name|reference
operator|.
name|getClass
argument_list|()
operator|.
name|getComponentType
argument_list|()
decl_stmt|;
comment|// the cast is safe because
comment|// result.getClass() == reference.getClass().getComponentType()
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|T
index|[]
name|result
init|=
operator|(
name|T
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|type
argument_list|,
name|length
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
DECL|method|Platform
specifier|private
name|Platform
parameter_list|()
block|{}
block|}
end_class

end_unit

