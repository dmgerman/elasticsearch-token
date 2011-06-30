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
name|logging
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Handles {@link Binder#addError} commands.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|MessageProcessor
class|class
name|MessageProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|Guice
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|MessageProcessor
name|MessageProcessor
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
DECL|method|visit
annotation|@
name|Override
specifier|public
name|Boolean
name|visit
parameter_list|(
name|Message
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|String
name|rootMessage
init|=
name|getRootMessage
argument_list|(
name|message
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|log
argument_list|(
name|Level
operator|.
name|INFO
argument_list|,
literal|"An exception was caught and reported. Message: "
operator|+
name|rootMessage
argument_list|,
name|message
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|errors
operator|.
name|addMessage
argument_list|(
name|message
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|getRootMessage
specifier|public
specifier|static
name|String
name|getRootMessage
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|t
operator|.
name|getCause
argument_list|()
decl_stmt|;
return|return
name|cause
operator|==
literal|null
condition|?
name|t
operator|.
name|toString
argument_list|()
else|:
name|getRootMessage
argument_list|(
name|cause
argument_list|)
return|;
block|}
block|}
end_class

end_unit

