begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.spi
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
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
name|Binder
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
name|ConfigurationException
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
name|util
operator|.
name|Objects
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
comment|/**  * A request to inject the instance fields and methods of an instance. Requests are created  * explicitly in a module using {@link org.elasticsearch.common.inject.Binder#requestInjection(Object)  * requestInjection()} statements:  *<pre>  *     requestInjection(serviceInstance);</pre>  *  * @author mikeward@google.com (Mike Ward)  * @since 2.0  */
end_comment

begin_class
DECL|class|InjectionRequest
specifier|public
specifier|final
class|class
name|InjectionRequest
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Element
block|{
DECL|field|source
specifier|private
specifier|final
name|Object
name|source
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
decl_stmt|;
DECL|field|instance
specifier|private
specifier|final
name|T
name|instance
decl_stmt|;
DECL|method|InjectionRequest
specifier|public
name|InjectionRequest
parameter_list|(
name|Object
name|source
parameter_list|,
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|T
name|instance
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|source
argument_list|,
literal|"source"
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|,
literal|"type"
argument_list|)
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|instance
argument_list|,
literal|"instance"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getSource
specifier|public
name|Object
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|getInstance
specifier|public
name|T
name|getInstance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
DECL|method|getType
specifier|public
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Returns the instance methods and fields of {@code instance} that will be injected to fulfill      * this request.      *      * @return a possibly empty set of injection points. The set has a specified iteration order. All      *         fields are returned and then all methods. Within the fields, supertype fields are returned      *         before subtype fields. Similarly, supertype methods are returned before subtype methods.      * @throws ConfigurationException if there is a malformed injection point on the class of {@code      *                                instance}, such as a field with multiple binding annotations. The exception's {@link      *                                ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>}      *                                of the valid injection points.      */
DECL|method|getInjectionPoints
specifier|public
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|getInjectionPoints
parameter_list|()
throws|throws
name|ConfigurationException
block|{
return|return
name|InjectionPoint
operator|.
name|forInstanceMethodsAndFields
argument_list|(
name|instance
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|acceptVisitor
specifier|public
parameter_list|<
name|R
parameter_list|>
name|R
name|acceptVisitor
parameter_list|(
name|ElementVisitor
argument_list|<
name|R
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visit
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|applyTo
specifier|public
name|void
name|applyTo
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
name|binder
operator|.
name|withSource
argument_list|(
name|getSource
argument_list|()
argument_list|)
operator|.
name|requestInjection
argument_list|(
name|type
argument_list|,
name|instance
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

