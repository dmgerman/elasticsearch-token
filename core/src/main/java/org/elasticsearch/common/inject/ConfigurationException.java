begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|spi
operator|.
name|Message
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  * Thrown when a programming error such as a misplaced annotation, illegal binding, or unsupported  * scope is found. Clients should catch this exception, log it, and stop execution.  *  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|ConfigurationException
specifier|public
specifier|final
class|class
name|ConfigurationException
extends|extends
name|RuntimeException
block|{
DECL|field|messages
specifier|private
specifier|final
name|Set
argument_list|<
name|Message
argument_list|>
name|messages
decl_stmt|;
DECL|field|partialValue
specifier|private
name|Object
name|partialValue
init|=
literal|null
decl_stmt|;
comment|/**      * Creates a ConfigurationException containing {@code messages}.      */
DECL|method|ConfigurationException
specifier|public
name|ConfigurationException
parameter_list|(
name|Iterable
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
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
name|messages
argument_list|)
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
comment|/**      * Returns a copy of this configuration exception with the specified partial value.      */
DECL|method|withPartialValue
specifier|public
name|ConfigurationException
name|withPartialValue
parameter_list|(
name|Object
name|partialValue
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|partialValue
operator|!=
literal|null
condition|)
block|{
name|String
name|message
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Can't clobber existing partial value %s with %s"
argument_list|,
name|this
operator|.
name|partialValue
argument_list|,
name|partialValue
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|message
argument_list|)
throw|;
block|}
name|ConfigurationException
name|result
init|=
operator|new
name|ConfigurationException
argument_list|(
name|messages
argument_list|)
decl_stmt|;
name|result
operator|.
name|partialValue
operator|=
name|partialValue
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**      * Returns messages for the errors that caused this exception.      */
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
comment|/**      * Returns a value that was only partially computed due to this exception. The caller can use      * this while collecting additional configuration problems.      *      * @return the partial value, or {@code null} if none was set. The type of the partial value is      *         specified by the throwing method.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// this is *extremely* unsafe. We trust the caller here.
DECL|method|getPartialValue
specifier|public
parameter_list|<
name|E
parameter_list|>
name|E
name|getPartialValue
parameter_list|()
block|{
return|return
operator|(
name|E
operator|)
name|partialValue
return|;
block|}
annotation|@
name|Override
DECL|method|getMessage
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
literal|"Guice configuration errors"
argument_list|,
name|messages
argument_list|)
return|;
block|}
block|}
end_class

end_unit

