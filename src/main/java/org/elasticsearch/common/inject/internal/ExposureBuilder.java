begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2009 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|internal
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
name|Key
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
name|binder
operator|.
name|AnnotatedElementBuilder
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
comment|/**  * For private binder's expose() method.  */
end_comment

begin_class
DECL|class|ExposureBuilder
specifier|public
class|class
name|ExposureBuilder
parameter_list|<
name|T
parameter_list|>
implements|implements
name|AnnotatedElementBuilder
block|{
DECL|field|binder
specifier|private
specifier|final
name|Binder
name|binder
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|Object
name|source
decl_stmt|;
DECL|field|key
specifier|private
name|Key
argument_list|<
name|T
argument_list|>
name|key
decl_stmt|;
DECL|method|ExposureBuilder
specifier|public
name|ExposureBuilder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|Object
name|source
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
DECL|method|checkNotAnnotated
specifier|protected
name|void
name|checkNotAnnotated
parameter_list|()
block|{
if|if
condition|(
name|key
operator|.
name|getAnnotationType
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|binder
operator|.
name|addError
argument_list|(
name|AbstractBindingBuilder
operator|.
name|ANNOTATION_ALREADY_SPECIFIED
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|annotatedWith
specifier|public
name|void
name|annotatedWith
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|annotationType
argument_list|,
literal|"annotationType"
argument_list|)
expr_stmt|;
name|checkNotAnnotated
argument_list|()
expr_stmt|;
name|key
operator|=
name|Key
operator|.
name|get
argument_list|(
name|key
operator|.
name|getTypeLiteral
argument_list|()
argument_list|,
name|annotationType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|annotatedWith
specifier|public
name|void
name|annotatedWith
parameter_list|(
name|Annotation
name|annotation
parameter_list|)
block|{
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|annotation
argument_list|,
literal|"annotation"
argument_list|)
expr_stmt|;
name|checkNotAnnotated
argument_list|()
expr_stmt|;
name|key
operator|=
name|Key
operator|.
name|get
argument_list|(
name|key
operator|.
name|getTypeLiteral
argument_list|()
argument_list|,
name|annotation
argument_list|)
expr_stmt|;
block|}
DECL|method|getKey
specifier|public
name|Key
argument_list|<
name|?
argument_list|>
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
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
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AnnotatedElementBuilder"
return|;
block|}
block|}
end_class

end_unit

