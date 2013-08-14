begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch
package|package
name|org
operator|.
name|elasticsearch
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ExceptionsHelper
specifier|public
specifier|final
class|class
name|ExceptionsHelper
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|ExceptionsHelper
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|convertToRuntime
specifier|public
specifier|static
name|RuntimeException
name|convertToRuntime
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|RuntimeException
condition|)
block|{
return|return
operator|(
name|RuntimeException
operator|)
name|t
return|;
block|}
return|return
operator|new
name|ElasticSearchException
argument_list|(
name|t
operator|.
name|getMessage
argument_list|()
argument_list|,
name|t
argument_list|)
return|;
block|}
DECL|method|convertToElastic
specifier|public
specifier|static
name|ElasticSearchException
name|convertToElastic
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ElasticSearchException
condition|)
block|{
return|return
operator|(
name|ElasticSearchException
operator|)
name|t
return|;
block|}
return|return
operator|new
name|ElasticSearchException
argument_list|(
name|t
operator|.
name|getMessage
argument_list|()
argument_list|,
name|t
argument_list|)
return|;
block|}
DECL|method|status
specifier|public
specifier|static
name|RestStatus
name|status
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ElasticSearchException
condition|)
block|{
return|return
operator|(
operator|(
name|ElasticSearchException
operator|)
name|t
operator|)
operator|.
name|status
argument_list|()
return|;
block|}
return|return
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
return|;
block|}
DECL|method|unwrapCause
specifier|public
specifier|static
name|Throwable
name|unwrapCause
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|Throwable
name|result
init|=
name|t
decl_stmt|;
while|while
condition|(
name|result
operator|instanceof
name|ElasticSearchWrapperException
condition|)
block|{
if|if
condition|(
name|result
operator|.
name|getCause
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|result
operator|.
name|getCause
argument_list|()
operator|==
name|result
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|counter
operator|++
operator|>
literal|10
condition|)
block|{
comment|// dear god, if we got more than 10 levels down, WTF? just bail
name|logger
operator|.
name|warn
argument_list|(
literal|"Exception cause unwrapping ran for 10 levels..."
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
name|result
operator|=
name|result
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|method|detailedMessage
specifier|public
specifier|static
name|String
name|detailedMessage
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return
name|detailedMessage
argument_list|(
name|t
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|)
return|;
block|}
DECL|method|detailedMessage
specifier|public
specifier|static
name|String
name|detailedMessage
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|newLines
parameter_list|,
name|int
name|initialCounter
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
return|return
literal|"Unknown"
return|;
block|}
name|int
name|counter
init|=
name|initialCounter
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|.
name|getMessage
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|newLines
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
expr_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|newLines
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|counter
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\t"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"nested: "
argument_list|)
expr_stmt|;
block|}
block|}
name|counter
operator|++
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"["
operator|+
name|t
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
block|}
end_class

end_unit

