begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.assistedinject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|assistedinject
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
name|Lists
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
name|inject
operator|.
name|Inject
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
name|inject
operator|.
name|TypeLiteral
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|Constructor
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
name|InvocationTargetException
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
name|Type
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
comment|/**  * Internal respresentation of a constructor annotated with  * {@link AssistedInject}  *  * @author jmourits@google.com (Jerome Mourits)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|AssistedConstructor
class|class
name|AssistedConstructor
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|constructor
specifier|private
specifier|final
name|Constructor
argument_list|<
name|T
argument_list|>
name|constructor
decl_stmt|;
DECL|field|assistedParameters
specifier|private
specifier|final
name|ParameterListKey
name|assistedParameters
decl_stmt|;
DECL|field|allParameters
specifier|private
specifier|final
name|List
argument_list|<
name|Parameter
argument_list|>
name|allParameters
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|AssistedConstructor
specifier|public
name|AssistedConstructor
parameter_list|(
name|Constructor
argument_list|<
name|T
argument_list|>
name|constructor
parameter_list|,
name|List
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|parameterTypes
parameter_list|)
block|{
name|this
operator|.
name|constructor
operator|=
name|constructor
expr_stmt|;
name|Annotation
index|[]
index|[]
name|annotations
init|=
name|constructor
operator|.
name|getParameterAnnotations
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Type
argument_list|>
name|typeList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|allParameters
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
comment|// categorize params as @Assisted or @Injected
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parameterTypes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Parameter
name|parameter
init|=
operator|new
name|Parameter
argument_list|(
name|parameterTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|annotations
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|allParameters
operator|.
name|add
argument_list|(
name|parameter
argument_list|)
expr_stmt|;
if|if
condition|(
name|parameter
operator|.
name|isProvidedByFactory
argument_list|()
condition|)
block|{
name|typeList
operator|.
name|add
argument_list|(
name|parameter
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|assistedParameters
operator|=
operator|new
name|ParameterListKey
argument_list|(
name|typeList
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the {@link ParameterListKey} for this constructor.  The      * {@link ParameterListKey} is created from the ordered list of {@link Assisted}      * constructor parameters.      */
DECL|method|getAssistedParameters
specifier|public
name|ParameterListKey
name|getAssistedParameters
parameter_list|()
block|{
return|return
name|assistedParameters
return|;
block|}
comment|/**      * Returns an ordered list of all constructor parameters (both      * {@link Assisted} and {@link Inject}ed).      */
DECL|method|getAllParameters
specifier|public
name|List
argument_list|<
name|Parameter
argument_list|>
name|getAllParameters
parameter_list|()
block|{
return|return
name|allParameters
return|;
block|}
DECL|method|getDeclaredExceptions
specifier|public
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDeclaredExceptions
parameter_list|()
block|{
return|return
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|constructor
operator|.
name|getExceptionTypes
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns an instance of T, constructed using this constructor, with the      * supplied arguments.      */
DECL|method|newInstance
specifier|public
name|T
name|newInstance
parameter_list|(
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|constructor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|constructor
operator|.
name|newInstance
argument_list|(
name|args
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
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
name|constructor
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

