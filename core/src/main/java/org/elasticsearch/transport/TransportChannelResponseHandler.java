begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
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
name|threadpool
operator|.
name|ThreadPool
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

begin_comment
comment|/**  * Base class for delegating transport response to a transport channel  */
end_comment

begin_class
DECL|class|TransportChannelResponseHandler
specifier|public
specifier|abstract
class|class
name|TransportChannelResponseHandler
parameter_list|<
name|T
extends|extends
name|TransportResponse
parameter_list|>
implements|implements
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
block|{
comment|/**      * Convenience method for delegating an empty response to the provided changed      */
DECL|method|emptyResponseHandler
specifier|public
specifier|static
name|TransportChannelResponseHandler
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
name|emptyResponseHandler
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|TransportChannel
name|channel
parameter_list|,
name|String
name|extraInfoOnError
parameter_list|)
block|{
return|return
operator|new
name|TransportChannelResponseHandler
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
argument_list|(
name|logger
argument_list|,
name|channel
argument_list|,
name|extraInfoOnError
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|TransportResponse
operator|.
name|Empty
name|newInstance
parameter_list|()
block|{
return|return
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
return|;
block|}
block|}
return|;
block|}
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|channel
specifier|private
specifier|final
name|TransportChannel
name|channel
decl_stmt|;
DECL|field|extraInfoOnError
specifier|private
specifier|final
name|String
name|extraInfoOnError
decl_stmt|;
DECL|method|TransportChannelResponseHandler
specifier|protected
name|TransportChannelResponseHandler
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|TransportChannel
name|channel
parameter_list|,
name|String
name|extraInfoOnError
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
name|this
operator|.
name|extraInfoOnError
operator|=
name|extraInfoOnError
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleResponse
specifier|public
name|void
name|handleResponse
parameter_list|(
name|T
name|response
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
operator|new
name|TransportException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|handleException
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to send failure {}"
argument_list|,
name|e
argument_list|,
name|extraInfoOnError
operator|==
literal|null
condition|?
literal|""
else|:
literal|"("
operator|+
name|extraInfoOnError
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|executor
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
block|}
end_class

end_unit

