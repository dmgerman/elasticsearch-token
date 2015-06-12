begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Scope
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

begin_comment
comment|/**  * No-op visitor for subclassing. All interface methods simply delegate to  * {@link #visitOther()}, returning its result.  *  * @param<V> any type to be returned by the visit method. Use {@link Void} with  *            {@code return null} if no return type is needed.  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|DefaultBindingScopingVisitor
specifier|public
class|class
name|DefaultBindingScopingVisitor
parameter_list|<
name|V
parameter_list|>
implements|implements
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
block|{
comment|/**      * Default visit implementation. Returns {@code null}.      */
DECL|method|visitOther
specifier|protected
name|V
name|visitOther
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|visitEagerSingleton
specifier|public
name|V
name|visitEagerSingleton
parameter_list|()
block|{
return|return
name|visitOther
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|visitScope
specifier|public
name|V
name|visitScope
parameter_list|(
name|Scope
name|scope
parameter_list|)
block|{
return|return
name|visitOther
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|visitScopeAnnotation
specifier|public
name|V
name|visitScopeAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopeAnnotation
parameter_list|)
block|{
return|return
name|visitOther
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|visitNoScoping
specifier|public
name|V
name|visitNoScoping
parameter_list|()
block|{
return|return
name|visitOther
argument_list|()
return|;
block|}
block|}
end_class

end_unit
