begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|internal
operator|.
name|ErrorsException
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
name|InternalContext
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
name|InternalFactory
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
name|ToStringBuilder
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
name|Dependency
import|;
end_import

begin_comment
comment|/**  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|ConstantFactory
class|class
name|ConstantFactory
parameter_list|<
name|T
parameter_list|>
implements|implements
name|InternalFactory
argument_list|<
name|T
argument_list|>
block|{
DECL|field|initializable
specifier|private
specifier|final
name|Initializable
argument_list|<
name|T
argument_list|>
name|initializable
decl_stmt|;
DECL|method|ConstantFactory
name|ConstantFactory
parameter_list|(
name|Initializable
argument_list|<
name|T
argument_list|>
name|initializable
parameter_list|)
block|{
name|this
operator|.
name|initializable
operator|=
name|initializable
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|T
name|get
parameter_list|(
name|Errors
name|errors
parameter_list|,
name|InternalContext
name|context
parameter_list|,
name|Dependency
name|dependency
parameter_list|)
throws|throws
name|ErrorsException
block|{
return|return
name|initializable
operator|.
name|get
argument_list|(
name|errors
argument_list|)
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
operator|new
name|ToStringBuilder
argument_list|(
name|ConstantFactory
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
literal|"value"
argument_list|,
name|initializable
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

