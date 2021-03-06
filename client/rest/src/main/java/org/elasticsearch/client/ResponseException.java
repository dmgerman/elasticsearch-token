begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|BufferedHttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|util
operator|.
name|EntityUtils
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
comment|/**  * Exception thrown when an elasticsearch node responds to a request with a status code that indicates an error.  * Holds the response that was returned.  */
end_comment

begin_class
DECL|class|ResponseException
specifier|public
specifier|final
class|class
name|ResponseException
extends|extends
name|IOException
block|{
DECL|field|response
specifier|private
name|Response
name|response
decl_stmt|;
DECL|method|ResponseException
specifier|public
name|ResponseException
parameter_list|(
name|Response
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|buildMessage
argument_list|(
name|response
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
block|}
DECL|method|buildMessage
specifier|private
specifier|static
name|String
name|buildMessage
parameter_list|(
name|Response
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|message
init|=
name|response
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getMethod
argument_list|()
operator|+
literal|" "
operator|+
name|response
operator|.
name|getHost
argument_list|()
operator|+
name|response
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|": "
operator|+
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|HttpEntity
name|entity
init|=
name|response
operator|.
name|getEntity
argument_list|()
decl_stmt|;
if|if
condition|(
name|entity
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|entity
operator|.
name|isRepeatable
argument_list|()
operator|==
literal|false
condition|)
block|{
name|entity
operator|=
operator|new
name|BufferedHttpEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
name|response
operator|.
name|getHttpResponse
argument_list|()
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
block|}
name|message
operator|+=
literal|"\n"
operator|+
name|EntityUtils
operator|.
name|toString
argument_list|(
name|entity
argument_list|)
expr_stmt|;
block|}
return|return
name|message
return|;
block|}
comment|/**      * Returns the {@link Response} that caused this exception to be thrown.      */
DECL|method|getResponse
specifier|public
name|Response
name|getResponse
parameter_list|()
block|{
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

