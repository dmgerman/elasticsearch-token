begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.master
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|master
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ack
operator|.
name|AckedRequest
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|unit
operator|.
name|TimeValue
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_comment
comment|/**  * Abstract class that allows to mark action requests that support acknowledgements.  * Facilitates consistency across different api.  */
end_comment

begin_class
DECL|class|AcknowledgedRequest
specifier|public
specifier|abstract
class|class
name|AcknowledgedRequest
parameter_list|<
name|Request
extends|extends
name|MasterNodeRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|>
extends|extends
name|MasterNodeRequest
argument_list|<
name|Request
argument_list|>
implements|implements
name|AckedRequest
block|{
DECL|field|DEFAULT_ACK_TIMEOUT
specifier|public
specifier|static
specifier|final
name|TimeValue
name|DEFAULT_ACK_TIMEOUT
init|=
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
decl_stmt|;
DECL|field|timeout
specifier|protected
name|TimeValue
name|timeout
init|=
name|DEFAULT_ACK_TIMEOUT
decl_stmt|;
DECL|method|AcknowledgedRequest
specifier|protected
name|AcknowledgedRequest
parameter_list|()
block|{     }
comment|/**      * Allows to set the timeout      * @param timeout timeout as a string (e.g. 1s)      * @return the request itself      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|timeout
specifier|public
specifier|final
name|Request
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
name|this
operator|.
name|timeout
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".timeout"
argument_list|)
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
comment|/**      * Allows to set the timeout      * @param timeout timeout as a {@link TimeValue}      * @return the request itself      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|timeout
specifier|public
specifier|final
name|Request
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
comment|/**      * Returns the current timeout      * @return the current timeout as a {@link TimeValue}      */
DECL|method|timeout
specifier|public
specifier|final
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
comment|/**      * Reads the timeout value      */
DECL|method|readTimeout
specifier|protected
name|void
name|readTimeout
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|timeout
operator|=
operator|new
name|TimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**      * writes the timeout value      */
DECL|method|writeTimeout
specifier|protected
name|void
name|writeTimeout
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|timeout
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ackTimeout
specifier|public
name|TimeValue
name|ackTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
block|}
end_class

end_unit

