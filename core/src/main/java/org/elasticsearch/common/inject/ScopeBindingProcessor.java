begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
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
name|internal
operator|.
name|Annotations
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
name|Errors
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
name|spi
operator|.
name|ScopeBinding
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
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Handles {@link Binder#bindScope} commands.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|ScopeBindingProcessor
class|class
name|ScopeBindingProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|method|ScopeBindingProcessor
name|ScopeBindingProcessor
parameter_list|(
name|Errors
name|errors
parameter_list|)
block|{
name|super
argument_list|(
name|errors
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|ScopeBinding
name|command
parameter_list|)
block|{
name|Scope
name|scope
init|=
name|command
operator|.
name|getScope
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
init|=
name|command
operator|.
name|getAnnotationType
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Annotations
operator|.
name|isScopeAnnotation
argument_list|(
name|annotationType
argument_list|)
condition|)
block|{
name|errors
operator|.
name|withSource
argument_list|(
name|annotationType
argument_list|)
operator|.
name|missingScopeAnnotation
argument_list|()
expr_stmt|;
comment|// Go ahead and bind anyway so we don't get collateral errors.
block|}
if|if
condition|(
operator|!
name|Annotations
operator|.
name|isRetainedAtRuntime
argument_list|(
name|annotationType
argument_list|)
condition|)
block|{
name|errors
operator|.
name|withSource
argument_list|(
name|annotationType
argument_list|)
operator|.
name|missingRuntimeRetention
argument_list|(
name|command
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
comment|// Go ahead and bind anyway so we don't get collateral errors.
block|}
name|Scope
name|existing
init|=
name|injector
operator|.
name|state
operator|.
name|getScope
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|annotationType
argument_list|,
literal|"annotation type"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
name|errors
operator|.
name|duplicateScopes
argument_list|(
name|existing
argument_list|,
name|annotationType
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|injector
operator|.
name|state
operator|.
name|putAnnotation
argument_list|(
name|annotationType
argument_list|,
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|scope
argument_list|,
literal|"scope"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

