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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_class
DECL|class|EmptyTransportResponseHandler
specifier|public
class|class
name|EmptyTransportResponseHandler
implements|implements
name|TransportResponseHandler
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
block|{
DECL|field|INSTANCE_SAME
specifier|public
specifier|static
specifier|final
name|EmptyTransportResponseHandler
name|INSTANCE_SAME
init|=
operator|new
name|EmptyTransportResponseHandler
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
decl_stmt|;
DECL|field|executor
specifier|private
specifier|final
name|String
name|executor
decl_stmt|;
DECL|method|EmptyTransportResponseHandler
specifier|public
name|EmptyTransportResponseHandler
parameter_list|(
name|String
name|executor
parameter_list|)
block|{
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newInstance
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
annotation|@
name|Override
DECL|method|handleResponse
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
operator|.
name|Empty
name|response
parameter_list|)
block|{     }
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
block|{     }
annotation|@
name|Override
DECL|method|executor
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|executor
return|;
block|}
block|}
end_class

end_unit

