begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|internal
operator|.
name|Errors
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|internal
operator|.
name|Preconditions
operator|.
name|checkArgument
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
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|Message
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
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  * Thrown when errors occur while creating a {@link Injector}. Includes a list of encountered  * errors. Clients should catch this exception, log it, and stop execution.  *  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|CreationException
specifier|public
class|class
name|CreationException
extends|extends
name|RuntimeException
block|{
DECL|field|messages
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Message
argument_list|>
name|messages
decl_stmt|;
comment|/** Creates a CreationException containing {@code messages}. */
DECL|method|CreationException
specifier|public
name|CreationException
parameter_list|(
name|Collection
argument_list|<
name|Message
argument_list|>
name|messages
parameter_list|)
block|{
name|this
operator|.
name|messages
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|messages
argument_list|)
expr_stmt|;
name|checkArgument
argument_list|(
operator|!
name|this
operator|.
name|messages
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|initCause
argument_list|(
name|Errors
operator|.
name|getOnlyCause
argument_list|(
name|this
operator|.
name|messages
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Returns messages for the errors that caused this exception. */
DECL|method|getErrorMessages
specifier|public
name|Collection
argument_list|<
name|Message
argument_list|>
name|getErrorMessages
parameter_list|()
block|{
return|return
name|messages
return|;
block|}
DECL|method|getMessage
annotation|@
name|Override
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
name|Errors
operator|.
name|format
argument_list|(
literal|"Guice creation errors"
argument_list|,
name|messages
argument_list|)
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

