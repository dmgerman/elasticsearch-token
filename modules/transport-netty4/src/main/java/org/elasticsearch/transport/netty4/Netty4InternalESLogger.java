begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty4
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty4
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|internal
operator|.
name|logging
operator|.
name|AbstractInternalLogger
import|;
end_import

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
name|elasticsearch
operator|.
name|common
operator|.
name|SuppressLoggerChecks
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

begin_class
annotation|@
name|SuppressLoggerChecks
argument_list|(
name|reason
operator|=
literal|"safely delegates to logger"
argument_list|)
DECL|class|Netty4InternalESLogger
class|class
name|Netty4InternalESLogger
extends|extends
name|AbstractInternalLogger
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|method|Netty4InternalESLogger
name|Netty4InternalESLogger
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isTraceEnabled
specifier|public
name|boolean
name|isTraceEnabled
parameter_list|()
block|{
return|return
name|logger
operator|.
name|isTraceEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|trace
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|trace
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|trace
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|argA
parameter_list|,
name|Object
name|argB
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|format
argument_list|,
name|argA
argument_list|,
name|argB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|trace
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|format
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|trace
specifier|public
name|void
name|trace
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isDebugEnabled
specifier|public
name|boolean
name|isDebugEnabled
parameter_list|()
block|{
return|return
name|logger
operator|.
name|isDebugEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|debug
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|debug
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|debug
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|argA
parameter_list|,
name|Object
name|argB
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|format
argument_list|,
name|argA
argument_list|,
name|argB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|debug
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|format
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|debug
specifier|public
name|void
name|debug
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isInfoEnabled
specifier|public
name|boolean
name|isInfoEnabled
parameter_list|()
block|{
return|return
name|logger
operator|.
name|isInfoEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|void
name|info
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
literal|"Your platform does not provide complete low-level API for accessing direct buffers reliably. "
operator|+
literal|"Unless explicitly requested, heap buffer will always be preferred to avoid potential system "
operator|+
literal|"instability."
operator|)
operator|.
name|equals
argument_list|(
name|msg
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|argA
parameter_list|,
name|Object
name|argB
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|format
argument_list|,
name|argA
argument_list|,
name|argB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|void
name|info
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|format
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|void
name|info
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isWarnEnabled
specifier|public
name|boolean
name|isWarnEnabled
parameter_list|()
block|{
return|return
name|logger
operator|.
name|isWarnEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|warn
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|warn
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|warn
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|format
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|warn
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|argA
parameter_list|,
name|Object
name|argB
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|format
argument_list|,
name|argA
argument_list|,
name|argB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|warn
specifier|public
name|void
name|warn
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isErrorEnabled
specifier|public
name|boolean
name|isErrorEnabled
parameter_list|()
block|{
return|return
name|logger
operator|.
name|isErrorEnabled
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|error
specifier|public
name|void
name|error
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|error
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|arg
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|format
argument_list|,
name|arg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|error
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
name|argA
parameter_list|,
name|Object
name|argB
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|format
argument_list|,
name|argA
argument_list|,
name|argB
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|error
specifier|public
name|void
name|error
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|format
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|error
specifier|public
name|void
name|error
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

