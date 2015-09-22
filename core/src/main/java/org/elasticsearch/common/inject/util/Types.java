begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|util
package|;
end_package

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
name|Provider
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
name|internal
operator|.
name|MoreTypes
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
name|internal
operator|.
name|MoreTypes
operator|.
name|GenericArrayTypeImpl
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
name|internal
operator|.
name|MoreTypes
operator|.
name|ParameterizedTypeImpl
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
name|internal
operator|.
name|MoreTypes
operator|.
name|WildcardTypeImpl
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
name|GenericArrayType
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
name|ParameterizedType
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
name|lang
operator|.
name|reflect
operator|.
name|WildcardType
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

begin_comment
comment|/**  * Static methods for working with types.  *  * @author crazybob@google.com (Bob Lee)  * @since 2.0  */
end_comment

begin_class
DECL|class|Types
specifier|public
specifier|final
class|class
name|Types
block|{
DECL|method|Types
specifier|private
name|Types
parameter_list|()
block|{     }
comment|/**      * Returns a new parameterized type, applying {@code typeArguments} to      * {@code rawType}. The returned type does not have an owner type.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|newParameterizedType
specifier|public
specifier|static
name|ParameterizedType
name|newParameterizedType
parameter_list|(
name|Type
name|rawType
parameter_list|,
name|Type
modifier|...
name|typeArguments
parameter_list|)
block|{
return|return
name|newParameterizedTypeWithOwner
argument_list|(
literal|null
argument_list|,
name|rawType
argument_list|,
name|typeArguments
argument_list|)
return|;
block|}
comment|/**      * Returns a new parameterized type, applying {@code typeArguments} to      * {@code rawType} and enclosed by {@code ownerType}.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|newParameterizedTypeWithOwner
specifier|public
specifier|static
name|ParameterizedType
name|newParameterizedTypeWithOwner
parameter_list|(
name|Type
name|ownerType
parameter_list|,
name|Type
name|rawType
parameter_list|,
name|Type
modifier|...
name|typeArguments
parameter_list|)
block|{
return|return
operator|new
name|ParameterizedTypeImpl
argument_list|(
name|ownerType
argument_list|,
name|rawType
argument_list|,
name|typeArguments
argument_list|)
return|;
block|}
comment|/**      * Returns an array type whose elements are all instances of      * {@code componentType}.      *      * @return a {@link java.io.Serializable serializable} generic array type.      */
DECL|method|arrayOf
specifier|public
specifier|static
name|GenericArrayType
name|arrayOf
parameter_list|(
name|Type
name|componentType
parameter_list|)
block|{
return|return
operator|new
name|GenericArrayTypeImpl
argument_list|(
name|componentType
argument_list|)
return|;
block|}
comment|/**      * Returns a type that represents an unknown type that extends {@code bound}.      * For example, if {@code bound} is {@code CharSequence.class}, this returns      * {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},      * this returns {@code ?}, which is shorthand for {@code ? extends Object}.      */
DECL|method|subtypeOf
specifier|public
specifier|static
name|WildcardType
name|subtypeOf
parameter_list|(
name|Type
name|bound
parameter_list|)
block|{
return|return
operator|new
name|WildcardTypeImpl
argument_list|(
operator|new
name|Type
index|[]
block|{
name|bound
block|}
argument_list|,
name|MoreTypes
operator|.
name|EMPTY_TYPE_ARRAY
argument_list|)
return|;
block|}
comment|/**      * Returns a type that represents an unknown supertype of {@code bound}. For      * example, if {@code bound} is {@code String.class}, this returns {@code ?      * super String}.      */
DECL|method|supertypeOf
specifier|public
specifier|static
name|WildcardType
name|supertypeOf
parameter_list|(
name|Type
name|bound
parameter_list|)
block|{
return|return
operator|new
name|WildcardTypeImpl
argument_list|(
operator|new
name|Type
index|[]
block|{
name|Object
operator|.
name|class
block|}
argument_list|,
operator|new
name|Type
index|[]
block|{
name|bound
block|}
argument_list|)
return|;
block|}
comment|/**      * Returns a type modelling a {@link List} whose elements are of type      * {@code elementType}.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|listOf
specifier|public
specifier|static
name|ParameterizedType
name|listOf
parameter_list|(
name|Type
name|elementType
parameter_list|)
block|{
return|return
name|newParameterizedType
argument_list|(
name|List
operator|.
name|class
argument_list|,
name|elementType
argument_list|)
return|;
block|}
comment|/**      * Returns a type modelling a {@link Set} whose elements are of type      * {@code elementType}.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|setOf
specifier|public
specifier|static
name|ParameterizedType
name|setOf
parameter_list|(
name|Type
name|elementType
parameter_list|)
block|{
return|return
name|newParameterizedType
argument_list|(
name|Set
operator|.
name|class
argument_list|,
name|elementType
argument_list|)
return|;
block|}
comment|/**      * Returns a type modelling a {@link Map} whose keys are of type      * {@code keyType} and whose values are of type {@code valueType}.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|mapOf
specifier|public
specifier|static
name|ParameterizedType
name|mapOf
parameter_list|(
name|Type
name|keyType
parameter_list|,
name|Type
name|valueType
parameter_list|)
block|{
return|return
name|newParameterizedType
argument_list|(
name|Map
operator|.
name|class
argument_list|,
name|keyType
argument_list|,
name|valueType
argument_list|)
return|;
block|}
comment|// for other custom collections types, use newParameterizedType()
comment|/**      * Returns a type modelling a {@link Provider} that provides elements of type      * {@code elementType}.      *      * @return a {@link java.io.Serializable serializable} parameterized type.      */
DECL|method|providerOf
specifier|public
specifier|static
name|ParameterizedType
name|providerOf
parameter_list|(
name|Type
name|providedType
parameter_list|)
block|{
return|return
name|newParameterizedType
argument_list|(
name|Provider
operator|.
name|class
argument_list|,
name|providedType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

