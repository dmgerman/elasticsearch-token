begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|CorruptIndexException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexFormatTooNewException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexFormatTooOldException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ShardOperationFailedException
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
name|Nullable
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
name|index
operator|.
name|Index
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

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
name|Logger
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
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|RuntimeException
condition|)
block|{
return|return
operator|(
name|RuntimeException
operator|)
name|e
return|;
block|}
return|return
operator|new
name|ElasticsearchException
argument_list|(
name|e
argument_list|)
return|;
block|}
DECL|method|convertToElastic
specifier|public
specifier|static
name|ElasticsearchException
name|convertToElastic
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticsearchException
condition|)
block|{
return|return
operator|(
name|ElasticsearchException
operator|)
name|e
return|;
block|}
return|return
operator|new
name|ElasticsearchException
argument_list|(
name|e
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
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ElasticsearchException
condition|)
block|{
return|return
operator|(
operator|(
name|ElasticsearchException
operator|)
name|t
operator|)
operator|.
name|status
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|IllegalArgumentException
condition|)
block|{
return|return
name|RestStatus
operator|.
name|BAD_REQUEST
return|;
block|}
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
name|ElasticsearchWrapperException
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
comment|/**      * @deprecated Don't swallow exceptions, allow them to propagate.      */
annotation|@
name|Deprecated
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
name|sb
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
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
name|sb
operator|.
name|append
argument_list|(
literal|"nested: "
argument_list|)
expr_stmt|;
block|}
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
DECL|method|stackTrace
specifier|public
specifier|static
name|String
name|stackTrace
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|StringWriter
name|stackTraceStringWriter
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|printWriter
init|=
operator|new
name|PrintWriter
argument_list|(
name|stackTraceStringWriter
argument_list|)
decl_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|(
name|printWriter
argument_list|)
expr_stmt|;
return|return
name|stackTraceStringWriter
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Rethrows the first exception in the list and adds all remaining to the suppressed list.      * If the given list is empty no exception is thrown      *      */
DECL|method|rethrowAndSuppress
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Throwable
parameter_list|>
name|void
name|rethrowAndSuppress
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|exceptions
parameter_list|)
throws|throws
name|T
block|{
name|T
name|main
init|=
literal|null
decl_stmt|;
for|for
control|(
name|T
name|ex
range|:
name|exceptions
control|)
block|{
name|main
operator|=
name|useOrSuppress
argument_list|(
name|main
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|main
operator|!=
literal|null
condition|)
block|{
throw|throw
name|main
throw|;
block|}
block|}
comment|/**      * Throws a runtime exception with all given exceptions added as suppressed.      * If the given list is empty no exception is thrown      */
DECL|method|maybeThrowRuntimeAndSuppress
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Throwable
parameter_list|>
name|void
name|maybeThrowRuntimeAndSuppress
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|exceptions
parameter_list|)
block|{
name|T
name|main
init|=
literal|null
decl_stmt|;
for|for
control|(
name|T
name|ex
range|:
name|exceptions
control|)
block|{
name|main
operator|=
name|useOrSuppress
argument_list|(
name|main
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|main
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|main
argument_list|)
throw|;
block|}
block|}
DECL|method|useOrSuppress
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Throwable
parameter_list|>
name|T
name|useOrSuppress
parameter_list|(
name|T
name|first
parameter_list|,
name|T
name|second
parameter_list|)
block|{
if|if
condition|(
name|first
operator|==
literal|null
condition|)
block|{
return|return
name|second
return|;
block|}
else|else
block|{
name|first
operator|.
name|addSuppressed
argument_list|(
name|second
argument_list|)
expr_stmt|;
block|}
return|return
name|first
return|;
block|}
DECL|method|unwrapCorruption
specifier|public
specifier|static
name|IOException
name|unwrapCorruption
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return
operator|(
name|IOException
operator|)
name|unwrap
argument_list|(
name|t
argument_list|,
name|CorruptIndexException
operator|.
name|class
argument_list|,
name|IndexFormatTooOldException
operator|.
name|class
argument_list|,
name|IndexFormatTooNewException
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|unwrap
specifier|public
specifier|static
name|Throwable
name|unwrap
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|clazzes
parameter_list|)
block|{
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
do|do
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
range|:
name|clazzes
control|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|t
argument_list|)
condition|)
block|{
return|return
name|t
return|;
block|}
block|}
block|}
do|while
condition|(
operator|(
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
operator|)
operator|!=
literal|null
condition|)
do|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Throws the specified exception. If null if specified then<code>true</code> is returned.      */
DECL|method|reThrowIfNotNull
specifier|public
specifier|static
name|boolean
name|reThrowIfNotNull
parameter_list|(
annotation|@
name|Nullable
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|RuntimeException
condition|)
block|{
throw|throw
operator|(
name|RuntimeException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Deduplicate the failures by exception message and index.      */
DECL|method|groupBy
specifier|public
specifier|static
name|ShardOperationFailedException
index|[]
name|groupBy
parameter_list|(
name|ShardOperationFailedException
index|[]
name|failures
parameter_list|)
block|{
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|uniqueFailures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|GroupBy
argument_list|>
name|reasons
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|failure
range|:
name|failures
control|)
block|{
name|GroupBy
name|reason
init|=
operator|new
name|GroupBy
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|reasons
operator|.
name|contains
argument_list|(
name|reason
argument_list|)
operator|==
literal|false
condition|)
block|{
name|reasons
operator|.
name|add
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|uniqueFailures
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|uniqueFailures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardOperationFailedException
index|[
literal|0
index|]
argument_list|)
return|;
block|}
DECL|class|GroupBy
specifier|static
class|class
name|GroupBy
block|{
DECL|field|reason
specifier|final
name|String
name|reason
decl_stmt|;
DECL|field|index
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|causeType
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|causeType
decl_stmt|;
DECL|method|GroupBy
specifier|public
name|GroupBy
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ElasticsearchException
condition|)
block|{
specifier|final
name|Index
name|index
init|=
operator|(
operator|(
name|ElasticsearchException
operator|)
name|t
operator|)
operator|.
name|getIndex
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|index
operator|=
name|index
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|index
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|index
operator|=
literal|null
expr_stmt|;
block|}
name|reason
operator|=
name|t
operator|.
name|getMessage
argument_list|()
expr_stmt|;
name|causeType
operator|=
name|t
operator|.
name|getClass
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|GroupBy
name|groupBy
init|=
operator|(
name|GroupBy
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|causeType
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|causeType
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|?
operator|!
name|index
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|index
argument_list|)
else|:
name|groupBy
operator|.
name|index
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|?
operator|!
name|reason
operator|.
name|equals
argument_list|(
name|groupBy
operator|.
name|reason
argument_list|)
else|:
name|groupBy
operator|.
name|reason
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|reason
operator|!=
literal|null
condition|?
name|reason
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|index
operator|!=
literal|null
condition|?
name|index
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|causeType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

